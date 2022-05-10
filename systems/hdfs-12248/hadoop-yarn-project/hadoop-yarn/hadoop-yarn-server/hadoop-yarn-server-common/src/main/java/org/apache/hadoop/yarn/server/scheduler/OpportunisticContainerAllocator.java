/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.yarn.server.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.ExecutionType;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.ResourceBlacklistRequest;
import org.apache.hadoop.yarn.api.records.ResourceRequest;
import org.apache.hadoop.yarn.api.records.Token;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.nodelabels.CommonNodeLabelsManager;
import org.apache.hadoop.yarn.security.ContainerTokenIdentifier;
import org.apache.hadoop.yarn.server.api.ContainerType;

import org.apache.hadoop.yarn.server.api.protocolrecords.RemoteNode;
import org.apache.hadoop.yarn.server.security.BaseContainerTokenSecretManager;
import org.apache.hadoop.yarn.server.utils.BuilderUtils;
import org.apache.hadoop.yarn.util.resource.DominantResourceCalculator;
import org.apache.hadoop.yarn.util.resource.ResourceCalculator;
import org.apache.hadoop.yarn.util.resource.Resources;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>
 * The OpportunisticContainerAllocator allocates containers on a given list of
 * nodes, after modifying the container sizes to respect the limits set by the
 * ResourceManager. It tries to distribute the containers as evenly as possible.
 * </p>
 */
public class OpportunisticContainerAllocator {

  /**
   * This class encapsulates application specific parameters used to build a
   * Container.
   */
  public static class AllocationParams {
    private Resource maxResource;
    private Resource minResource;
    private Resource incrementResource;
    private int containerTokenExpiryInterval;

    /**
     * Return Max Resource.
     * @return Resource
     */
    public Resource getMaxResource() {
      return maxResource;
    }

    /**
     * Set Max Resource.
     * @param maxResource Resource
     */
    public void setMaxResource(Resource maxResource) {
      this.maxResource = maxResource;
    }

    /**
     * Get Min Resource.
     * @return Resource
     */
    public Resource getMinResource() {
      return minResource;
    }

    /**
     * Set Min Resource.
     * @param minResource Resource
     */
    public void setMinResource(Resource minResource) {
      this.minResource = minResource;
    }

    /**
     * Get Incremental Resource.
     * @return Incremental Resource
     */
    public Resource getIncrementResource() {
      return incrementResource;
    }

    /**
     * Set Incremental resource.
     * @param incrementResource Resource
     */
    public void setIncrementResource(Resource incrementResource) {
      this.incrementResource = incrementResource;
    }

    /**
     * Get Container Token Expiry interval.
     * @return Container Token Expiry interval
     */
    public int getContainerTokenExpiryInterval() {
      return containerTokenExpiryInterval;
    }

    /**
     * Set Container Token Expiry time in ms.
     * @param containerTokenExpiryInterval Container Token Expiry in ms
     */
    public void setContainerTokenExpiryInterval(
        int containerTokenExpiryInterval) {
      this.containerTokenExpiryInterval = containerTokenExpiryInterval;
    }
  }

  /**
   * A Container Id Generator.
   */
  public static class ContainerIdGenerator {

    protected volatile AtomicLong containerIdCounter = new AtomicLong(1);

    /**
     * This method can reset the generator to a specific value.
     * @param containerIdStart containerId
     */
    public void resetContainerIdCounter(long containerIdStart) {
      this.containerIdCounter.set(containerIdStart);
    }

    /**
     * Generates a new long value. Default implementation increments the
     * underlying AtomicLong. Sub classes are encouraged to over-ride this
     * behaviour.
     * @return Counter.
     */
    public long generateContainerId() {
      return this.containerIdCounter.incrementAndGet();
    }
  }

  /**
   * Class that includes two lists of {@link ResourceRequest}s: one for
   * GUARANTEED and one for OPPORTUNISTIC {@link ResourceRequest}s.
   */
  public static class PartitionedResourceRequests {
    private List<ResourceRequest> guaranteed = new ArrayList<>();
    private List<ResourceRequest> opportunistic = new ArrayList<>();

    public List<ResourceRequest> getGuaranteed() {
      return guaranteed;
    }

    public List<ResourceRequest> getOpportunistic() {
      return opportunistic;
    }
  }

  private static final Log LOG =
      LogFactory.getLog(OpportunisticContainerAllocator.class);

  private static final ResourceCalculator RESOURCE_CALCULATOR =
      new DominantResourceCalculator();

  private final BaseContainerTokenSecretManager tokenSecretManager;

  /**
   * Create a new Opportunistic Container Allocator.
   * @param tokenSecretManager TokenSecretManager
   */
  public OpportunisticContainerAllocator(
      BaseContainerTokenSecretManager tokenSecretManager) {
    this.tokenSecretManager = tokenSecretManager;
  }

  /**
   * Allocate OPPORTUNISTIC containers.
   * @param blackList Resource BlackList Request
   * @param oppResourceReqs Opportunistic Resource Requests
   * @param applicationAttemptId ApplicationAttemptId
   * @param opportContext App specific OpportunisticContainerContext
   * @param rmIdentifier RM Identifier
   * @param appSubmitter App Submitter
   * @return List of Containers.
   * @throws YarnException YarnException
   */
  public List<Container> allocateContainers(ResourceBlacklistRequest blackList,
      List<ResourceRequest> oppResourceReqs,
      ApplicationAttemptId applicationAttemptId,
      OpportunisticContainerContext opportContext, long rmIdentifier,
      String appSubmitter) throws YarnException {

    // Update black list.
    if (blackList != null) {
      opportContext.getBlacklist().removeAll(blackList.getBlacklistRemovals());
      opportContext.getBlacklist().addAll(blackList.getBlacklistAdditions());
    }

    // Add OPPORTUNISTIC requests to the outstanding ones.
    opportContext.addToOutstandingReqs(oppResourceReqs);

    // Satisfy the outstanding OPPORTUNISTIC requests.
    List<Container> allocatedContainers = new ArrayList<>();
    for (SchedulerRequestKey schedulerKey :
        opportContext.getOutstandingOpReqs().descendingKeySet()) {
      // Allocated containers :
      //  Key = Requested Capability,
      //  Value = List of Containers of given cap (the actual container size
      //          might be different than what is requested, which is why
      //          we need the requested capability (key) to match against
      //          the outstanding reqs)
      Map<Resource, List<Container>> allocated = allocate(rmIdentifier,
          opportContext, schedulerKey, applicationAttemptId, appSubmitter);
      for (Map.Entry<Resource, List<Container>> e : allocated.entrySet()) {
        opportContext.matchAllocationToOutstandingRequest(
            e.getKey(), e.getValue());
        allocatedContainers.addAll(e.getValue());
      }
    }

    return allocatedContainers;
  }

  private Map<Resource, List<Container>> allocate(long rmIdentifier,
      OpportunisticContainerContext appContext, SchedulerRequestKey schedKey,
      ApplicationAttemptId appAttId, String userName) throws YarnException {
    Map<Resource, List<Container>> containers = new HashMap<>();
    for (ResourceRequest anyAsk :
        appContext.getOutstandingOpReqs().get(schedKey).values()) {
      allocateContainersInternal(rmIdentifier, appContext.getAppParams(),
          appContext.getContainerIdGenerator(), appContext.getBlacklist(),
          appAttId, appContext.getNodeMap(), userName, containers, anyAsk);
      if (!containers.isEmpty()) {
        LOG.info("Opportunistic allocation requested for ["
            + "priority=" + anyAsk.getPriority()
            + ", allocationRequestId=" + anyAsk.getAllocationRequestId()
            + ", num_containers=" + anyAsk.getNumContainers()
            + ", capability=" + anyAsk.getCapability() + "]"
            + " allocated = " + containers.keySet());
      }
    }
    return containers;
  }

  private void allocateContainersInternal(long rmIdentifier,
      AllocationParams appParams, ContainerIdGenerator idCounter,
      Set<String> blacklist, ApplicationAttemptId id,
      Map<String, RemoteNode> allNodes, String userName,
      Map<Resource, List<Container>> containers, ResourceRequest anyAsk)
      throws YarnException {
    int toAllocate = anyAsk.getNumContainers()
        - (containers.isEmpty() ? 0 :
            containers.get(anyAsk.getCapability()).size());

    List<RemoteNode> nodesForScheduling = new ArrayList<>();
    for (Entry<String, RemoteNode> nodeEntry : allNodes.entrySet()) {
      // Do not use blacklisted nodes for scheduling.
      if (blacklist.contains(nodeEntry.getKey())) {
        continue;
      }
      nodesForScheduling.add(nodeEntry.getValue());
    }
    if (nodesForScheduling.isEmpty()) {
      LOG.warn("No nodes available for allocating opportunistic containers. [" +
          "allNodes=" + allNodes + ", " +
          "blacklist=" + blacklist + "]");
      return;
    }
    int numAllocated = 0;
    int nextNodeToSchedule = 0;
    for (int numCont = 0; numCont < toAllocate; numCont++) {
      nextNodeToSchedule++;
      nextNodeToSchedule %= nodesForScheduling.size();
      RemoteNode node = nodesForScheduling.get(nextNodeToSchedule);
      Container container = buildContainer(rmIdentifier, appParams, idCounter,
          anyAsk, id, userName, node);
      List<Container> cList = containers.get(anyAsk.getCapability());
      if (cList == null) {
        cList = new ArrayList<>();
        containers.put(anyAsk.getCapability(), cList);
      }
      cList.add(container);
      numAllocated++;
      LOG.info("Allocated [" + container.getId() + "] as opportunistic.");
    }
    LOG.info("Allocated " + numAllocated + " opportunistic containers.");
  }

  private Container buildContainer(long rmIdentifier,
      AllocationParams appParams, ContainerIdGenerator idCounter,
      ResourceRequest rr, ApplicationAttemptId id, String userName,
      RemoteNode node) throws YarnException {
    ContainerId cId =
        ContainerId.newContainerId(id, idCounter.generateContainerId());

    // Normalize the resource asks (Similar to what the the RM scheduler does
    // before accepting an ask)
    Resource capability = normalizeCapability(appParams, rr);

    return createContainer(
        rmIdentifier, appParams.getContainerTokenExpiryInterval(),
        SchedulerRequestKey.create(rr), userName, node, cId, capability);
  }

  private Container createContainer(long rmIdentifier, long tokenExpiry,
      SchedulerRequestKey schedulerKey, String userName, RemoteNode node,
      ContainerId cId, Resource capability) {
    long currTime = System.currentTimeMillis();
    ContainerTokenIdentifier containerTokenIdentifier =
        new ContainerTokenIdentifier(
            cId, 0, node.getNodeId().toString(), userName,
            capability, currTime + tokenExpiry,
            tokenSecretManager.getCurrentKey().getKeyId(), rmIdentifier,
            schedulerKey.getPriority(), currTime,
            null, CommonNodeLabelsManager.NO_LABEL, ContainerType.TASK,
            ExecutionType.OPPORTUNISTIC);
    byte[] pwd =
        tokenSecretManager.createPassword(containerTokenIdentifier);
    Token containerToken = newContainerToken(node.getNodeId(), pwd,
        containerTokenIdentifier);
    Container container = BuilderUtils.newContainer(
        cId, node.getNodeId(), node.getHttpAddress(),
        capability, schedulerKey.getPriority(), containerToken,
        containerTokenIdentifier.getExecutionType(),
        schedulerKey.getAllocationRequestId());
    return container;
  }

  private Resource normalizeCapability(AllocationParams appParams,
      ResourceRequest ask) {
    return Resources.normalize(RESOURCE_CALCULATOR,
        ask.getCapability(), appParams.minResource, appParams.maxResource,
        appParams.incrementResource);
  }

  private static Token newContainerToken(NodeId nodeId, byte[] password,
      ContainerTokenIdentifier tokenIdentifier) {
    // RPC layer client expects ip:port as service for tokens
    InetSocketAddress addr = NetUtils.createSocketAddrForHost(nodeId.getHost(),
        nodeId.getPort());
    // NOTE: use SecurityUtil.setTokenService if this becomes a "real" token
    Token containerToken = Token.newInstance(tokenIdentifier.getBytes(),
        ContainerTokenIdentifier.KIND.toString(), password, SecurityUtil
            .buildTokenService(addr).toString());
    return containerToken;
  }

  /**
   * Partitions a list of ResourceRequest to two separate lists, one for
   * GUARANTEED and one for OPPORTUNISTIC ResourceRequests.
   * @param askList the list of ResourceRequests to be partitioned
   * @return the partitioned ResourceRequests
   */
  public PartitionedResourceRequests partitionAskList(
      List<ResourceRequest> askList) {
    PartitionedResourceRequests partitionedRequests =
        new PartitionedResourceRequests();
    for (ResourceRequest rr : askList) {
      if (rr.getExecutionTypeRequest().getExecutionType() ==
          ExecutionType.OPPORTUNISTIC) {
        partitionedRequests.getOpportunistic().add(rr);
      } else {
        partitionedRequests.getGuaranteed().add(rr);
      }
    }
    return partitionedRequests;
  }
}
