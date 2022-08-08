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

package org.apache.hadoop.yarn.server.resourcemanager.monitor.capacity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.server.resourcemanager.monitor.capacity.ProportionalCapacityPreemptionPolicy.IntraQueuePreemptionOrderPolicy;
import org.apache.hadoop.yarn.server.resourcemanager.nodelabels.RMNodeLabelsManager;
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainer;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.LeafQueue;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.common.fica.FiCaSchedulerApp;
import org.apache.hadoop.yarn.util.resource.Resources;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Identifies over utilized resources within a queue and tries to normalize
 * them to resolve resource allocation anomalies w.r.t priority and user-limit.
 */
public class IntraQueueCandidatesSelector extends PreemptionCandidatesSelector {

  @SuppressWarnings("serial")
  static class TAPriorityComparator
      implements
        Serializable,
        Comparator<TempAppPerPartition> {

    @Override
    public int compare(TempAppPerPartition ta1, TempAppPerPartition ta2) {
      Priority p1 = Priority.newInstance(ta1.getPriority());
      Priority p2 = Priority.newInstance(ta2.getPriority());

      if (!p1.equals(p2)) {
        return p1.compareTo(p2);
      }
      return ta1.getApplicationId().compareTo(ta2.getApplicationId());
    }
  }

  IntraQueuePreemptionComputePlugin fifoPreemptionComputePlugin = null;
  final CapacitySchedulerPreemptionContext context;

  private static final Log LOG =
      LogFactory.getLog(IntraQueueCandidatesSelector.class);

  IntraQueueCandidatesSelector(
      CapacitySchedulerPreemptionContext preemptionContext) {
    super(preemptionContext);
    fifoPreemptionComputePlugin = new FifoIntraQueuePreemptionPlugin(rc,
        preemptionContext);
    context = preemptionContext;
  }

  @Override
  public Map<ApplicationAttemptId, Set<RMContainer>> selectCandidates(
      Map<ApplicationAttemptId, Set<RMContainer>> selectedCandidates,
      Resource clusterResource, Resource totalPreemptedResourceAllowed) {

    // 1. Calculate the abnormality within each queue one by one.
    computeIntraQueuePreemptionDemand(
        clusterResource, totalPreemptedResourceAllowed, selectedCandidates);

    // 2. Previous selectors (with higher priority) could have already
    // selected containers. We need to deduct pre-emptable resources
    // based on already selected candidates.
    CapacitySchedulerPreemptionUtils
        .deductPreemptableResourcesBasedSelectedCandidates(preemptionContext,
            selectedCandidates);

    // 3. Loop through all partitions to select containers for preemption.
    for (String partition : preemptionContext.getAllPartitions()) {
      LinkedHashSet<String> queueNames = preemptionContext
          .getUnderServedQueuesPerPartition(partition);

      // Error check to handle non-mapped labels to queue.
      if (null == queueNames) {
        continue;
      }

      // 4. Iterate from most under-served queue in order.
      for (String queueName : queueNames) {
        LeafQueue leafQueue = preemptionContext.getQueueByPartition(queueName,
            RMNodeLabelsManager.NO_LABEL).leafQueue;

        // skip if not a leafqueue
        if (null == leafQueue) {
          continue;
        }

        // Don't preempt if disabled for this queue.
        if (leafQueue.getPreemptionDisabled()) {
          continue;
        }

        // 5. Calculate the resource to obtain per partition
        Map<String, Resource> resToObtainByPartition = fifoPreemptionComputePlugin
            .getResourceDemandFromAppsPerQueue(queueName, partition);

        // Default preemption iterator considers only FIFO+priority. For
        // userlimit preemption, its possible that some lower priority apps
        // needs from high priority app of another user. Hence use apps
        // ordered by userlimit starvation as well.
        Collection<FiCaSchedulerApp> apps = fifoPreemptionComputePlugin
            .getPreemptableApps(queueName, partition);

        // 6. Get user-limit to ensure that we do not preempt resources which
        // will force user's resource to come under its UL.
        Map<String, Resource> rollingResourceUsagePerUser = new HashMap<>();
        initializeUsageAndUserLimitForCompute(clusterResource, partition,
            leafQueue, rollingResourceUsagePerUser);

        // 7. Based on the selected resource demand per partition, select
        // containers with known policy from inter-queue preemption.
        try {
          leafQueue.getReadLock().lock();
          for (FiCaSchedulerApp app : apps) {
            preemptFromLeastStarvedApp(leafQueue, app, selectedCandidates,
                clusterResource, totalPreemptedResourceAllowed,
                resToObtainByPartition, rollingResourceUsagePerUser);
          }
        } finally {
          leafQueue.getReadLock().unlock();
        }
      }
    }

    return selectedCandidates;
  }

  private void initializeUsageAndUserLimitForCompute(Resource clusterResource,
      String partition, LeafQueue leafQueue,
      Map<String, Resource> rollingResourceUsagePerUser) {
    for (String user : leafQueue.getAllUsers()) {
      // Initialize used resource of a given user for rolling computation.
      rollingResourceUsagePerUser.put(user, Resources.clone(
          leafQueue.getUser(user).getResourceUsage().getUsed(partition)));
      if (LOG.isDebugEnabled()) {
        LOG.debug("Rolling resource usage for user:" + user + " is : "
            + rollingResourceUsagePerUser.get(user));
      }
    }
  }

  private void preemptFromLeastStarvedApp(LeafQueue leafQueue,
      FiCaSchedulerApp app,
      Map<ApplicationAttemptId, Set<RMContainer>> selectedCandidates,
      Resource clusterResource, Resource totalPreemptedResourceAllowed,
      Map<String, Resource> resToObtainByPartition,
      Map<String, Resource> rollingResourceUsagePerUser) {

    // ToDo: Reuse reservation selector here.

    List<RMContainer> liveContainers = new ArrayList<>(app.getLiveContainers());
    sortContainers(liveContainers);

    if (LOG.isDebugEnabled()) {
      LOG.debug(
          "totalPreemptedResourceAllowed for preemption at this round is :"
              + totalPreemptedResourceAllowed);
    }

    Resource rollingUsedResourcePerUser = rollingResourceUsagePerUser
        .get(app.getUser());
    for (RMContainer c : liveContainers) {

      // if there are no demand, return.
      if (resToObtainByPartition.isEmpty()) {
        return;
      }

      // skip preselected containers.
      if (CapacitySchedulerPreemptionUtils.isContainerAlreadySelected(c,
          selectedCandidates)) {
        continue;
      }

      // Skip already marked to killable containers
      if (null != preemptionContext.getKillableContainers() && preemptionContext
          .getKillableContainers().contains(c.getContainerId())) {
        continue;
      }

      // Skip AM Container from preemption for now.
      if (c.isAMContainer()) {
        continue;
      }

      // If selected container brings down resource usage under its user's
      // UserLimit (or equals to), we must skip such containers.
      if (fifoPreemptionComputePlugin.skipContainerBasedOnIntraQueuePolicy(app,
          clusterResource, rollingUsedResourcePerUser, c)) {
        if (LOG.isDebugEnabled()) {
          LOG.debug(
              "Skipping container: " + c.getContainerId() + " with resource:"
                  + c.getAllocatedResource() + " as UserLimit for user:"
                  + app.getUser() + " with resource usage: "
                  + rollingUsedResourcePerUser + " is going under UL");
        }
        break;
      }

      // Try to preempt this container
      boolean ret = CapacitySchedulerPreemptionUtils
          .tryPreemptContainerAndDeductResToObtain(rc, preemptionContext,
              resToObtainByPartition, c, clusterResource, selectedCandidates,
              totalPreemptedResourceAllowed);

      // Subtract from respective user's resource usage once a container is
      // selected for preemption.
      if (ret && preemptionContext.getIntraQueuePreemptionOrderPolicy()
          .equals(IntraQueuePreemptionOrderPolicy.USERLIMIT_FIRST)) {
        Resources.subtractFrom(rollingUsedResourcePerUser,
            c.getAllocatedResource());
      }
    }
  }

  private void computeIntraQueuePreemptionDemand(Resource clusterResource,
      Resource totalPreemptedResourceAllowed,
      Map<ApplicationAttemptId, Set<RMContainer>> selectedCandidates) {

    // 1. Iterate through all partition to calculate demand within a partition.
    for (String partition : context.getAllPartitions()) {
      LinkedHashSet<String> queueNames = context
          .getUnderServedQueuesPerPartition(partition);

      if (null == queueNames) {
        continue;
      }

      // 2. loop through all queues corresponding to a partition.
      for (String queueName : queueNames) {
        TempQueuePerPartition tq = context.getQueueByPartition(queueName,
            partition);
        LeafQueue leafQueue = tq.leafQueue;

        // skip if its parent queue
        if (null == leafQueue) {
          continue;
        }

        // 3. Consider reassignableResource as (used - actuallyToBePreempted).
        // This provides as upper limit to split apps quota in a queue.
        Resource queueReassignableResource = Resources.subtract(tq.getUsed(),
            tq.getActuallyToBePreempted());

        // 4. Check queue's used capacity. Make sure that the used capacity is
        // above certain limit to consider for intra queue preemption.
        if (leafQueue.getQueueCapacities().getUsedCapacity(partition) < context
            .getMinimumThresholdForIntraQueuePreemption()) {
          continue;
        }

        // 5. compute the allocation of all apps based on queue's unallocated
        // capacity
        fifoPreemptionComputePlugin.computeAppsIdealAllocation(clusterResource,
            tq, selectedCandidates, totalPreemptedResourceAllowed,
            queueReassignableResource,
            context.getMaxAllowableLimitForIntraQueuePreemption());
      }
    }
  }
}
