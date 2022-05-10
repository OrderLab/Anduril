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
package org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.ResourceRequest;
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainer;
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainerEventType;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerUtils;
import org.apache.hadoop.yarn.util.resource.Resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;

/**
 * Thread that handles FairScheduler preemption.
 */
class FSPreemptionThread extends Thread {
  private static final Log LOG = LogFactory.getLog(FSPreemptionThread.class);
  protected final FSContext context;
  private final FairScheduler scheduler;
  private final long warnTimeBeforeKill;
  private final long delayBeforeNextStarvationCheck;
  private final Timer preemptionTimer;
  private final Lock schedulerReadLock;

  FSPreemptionThread(FairScheduler scheduler) {
    setDaemon(true);
    setName("FSPreemptionThread");
    this.scheduler = scheduler;
    this.context = scheduler.getContext();
    FairSchedulerConfiguration fsConf = scheduler.getConf();
    context.setPreemptionEnabled();
    context.setPreemptionUtilizationThreshold(
        fsConf.getPreemptionUtilizationThreshold());
    preemptionTimer = new Timer("Preemption Timer", true);

    warnTimeBeforeKill = fsConf.getWaitTimeBeforeKill();
    long allocDelay = (fsConf.isContinuousSchedulingEnabled()
        ? 10 * fsConf.getContinuousSchedulingSleepMs() // 10 runs
        : 4 * scheduler.getNMHeartbeatInterval()); // 4 heartbeats
    delayBeforeNextStarvationCheck = warnTimeBeforeKill + allocDelay +
        fsConf.getWaitTimeBeforeNextStarvationCheck();
    schedulerReadLock = scheduler.getSchedulerReadLock();
  }

  @Override
  public void run() {
    while (!Thread.interrupted()) {
      try {
        FSAppAttempt starvedApp = context.getStarvedApps().take();
        // Hold the scheduler readlock so this is not concurrent with the
        // update thread.
        schedulerReadLock.lock();
        try {
          preemptContainers(identifyContainersToPreempt(starvedApp));
        } finally {
          schedulerReadLock.unlock();
        }
        starvedApp.preemptionTriggered(delayBeforeNextStarvationCheck);
      } catch (InterruptedException e) {
        LOG.info("Preemption thread interrupted! Exiting.");
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * Given an app, identify containers to preempt to satisfy the app's
   * starvation.
   *
   * Mechanics:
   * 1. Fetch all {@link ResourceRequest}s corresponding to the amount of
   * starvation.
   * 2. For each {@link ResourceRequest}, iterate through matching
   * nodes and identify containers to preempt all on one node, also
   * optimizing for least number of AM container preemptions.
   *
   * @param starvedApp starved application for which we are identifying
   *                   preemption targets
   * @return list of containers to preempt to satisfy starvedApp
   */
  private List<RMContainer> identifyContainersToPreempt(
      FSAppAttempt starvedApp) {
    List<RMContainer> containersToPreempt = new ArrayList<>();

    // Iterate through enough RRs to address app's starvation
    for (ResourceRequest rr : starvedApp.getStarvedResourceRequests()) {
      for (int i = 0; i < rr.getNumContainers(); i++) {
        PreemptableContainers bestContainers = null;
        List<FSSchedulerNode> potentialNodes = scheduler.getNodeTracker()
            .getNodesByResourceName(rr.getResourceName());
        int maxAMContainers = Integer.MAX_VALUE;

        for (FSSchedulerNode node : potentialNodes) {
          PreemptableContainers preemptableContainers =
              identifyContainersToPreemptOnNode(
                  rr.getCapability(), node, maxAMContainers);

          if (preemptableContainers != null) {
            // This set is better than any previously identified set.
            bestContainers = preemptableContainers;
            maxAMContainers = bestContainers.numAMContainers;

            if (maxAMContainers == 0) {
              break;
            }
          }
        } // End of iteration through nodes for one RR

        if (bestContainers != null && bestContainers.containers.size() > 0) {
          containersToPreempt.addAll(bestContainers.containers);
          // Reserve the containers for the starved app
          trackPreemptionsAgainstNode(bestContainers.containers, starvedApp);
        }
      }
    } // End of iteration over RRs
    return containersToPreempt;
  }

  /**
   * Identify containers to preempt on a given node. Try to find a list with
   * least AM containers to avoid preempting AM containers. This method returns
   * a non-null set of containers only if the number of AM containers is less
   * than maxAMContainers.
   *
   * @param request resource requested
   * @param node the node to check
   * @param maxAMContainers max allowed AM containers in the set
   * @return list of preemptable containers with fewer AM containers than
   *         maxAMContainers if such a list exists; null otherwise.
   */
  private PreemptableContainers identifyContainersToPreemptOnNode(
      Resource request, FSSchedulerNode node, int maxAMContainers) {
    PreemptableContainers preemptableContainers =
        new PreemptableContainers(maxAMContainers);

    // Figure out list of containers to consider
    List<RMContainer> containersToCheck =
        node.getRunningContainersWithAMsAtTheEnd();
    containersToCheck.removeAll(node.getContainersForPreemption());

    // Initialize potential with unallocated but not reserved resources
    Resource potential = Resources.subtractFromNonNegative(
        Resources.clone(node.getUnallocatedResource()),
        node.getTotalReserved());

    for (RMContainer container : containersToCheck) {
      FSAppAttempt app =
          scheduler.getSchedulerApp(container.getApplicationAttemptId());

      if (app.canContainerBePreempted(container)) {
        // Flag container for preemption
        if (!preemptableContainers.addContainer(container)) {
          return null;
        }

        Resources.addTo(potential, container.getAllocatedResource());
      }

      // Check if we have already identified enough containers
      if (Resources.fitsIn(request, potential)) {
        return preemptableContainers;
      }
    }

    // Return null if the sum of all preemptable containers' resources
    // isn't enough to satisfy the starved request.
    return null;
  }

  private void trackPreemptionsAgainstNode(List<RMContainer> containers,
                                           FSAppAttempt app) {
    FSSchedulerNode node = (FSSchedulerNode) scheduler.getNodeTracker()
        .getNode(containers.get(0).getNodeId());
    node.addContainersForPreemption(containers, app);
  }

  private void preemptContainers(List<RMContainer> containers) {
    // Warn application about containers to be killed
    for (RMContainer container : containers) {
      ApplicationAttemptId appAttemptId = container.getApplicationAttemptId();
      FSAppAttempt app = scheduler.getSchedulerApp(appAttemptId);
      LOG.info("Preempting container " + container +
          " from queue " + app.getQueueName());
      app.trackContainerForPreemption(container);
    }

    // Schedule timer task to kill containers
    preemptionTimer.schedule(
        new PreemptContainersTask(containers), warnTimeBeforeKill);
  }

  private class PreemptContainersTask extends TimerTask {
    private final List<RMContainer> containers;

    PreemptContainersTask(List<RMContainer> containers) {
      this.containers = containers;
    }

    @Override
    public void run() {
      for (RMContainer container : containers) {
        ContainerStatus status = SchedulerUtils.createPreemptedContainerStatus(
            container.getContainerId(), SchedulerUtils.PREEMPTED_CONTAINER);

        LOG.info("Killing container " + container);
        scheduler.completedContainer(
            container, status, RMContainerEventType.KILL);
      }
    }
  }

  /**
   * A class to track preemptable containers.
   */
  private static class PreemptableContainers {
    List<RMContainer> containers;
    int numAMContainers;
    int maxAMContainers;

    PreemptableContainers(int maxAMContainers) {
      containers = new ArrayList<>();
      numAMContainers = 0;
      this.maxAMContainers = maxAMContainers;
    }

    /**
     * Add a container if the number of AM containers is less than
     * maxAMContainers.
     *
     * @param container the container to add
     * @return true if success; false otherwise
     */
    private boolean addContainer(RMContainer container) {
      if (container.isAMContainer()) {
        numAMContainers++;
        if (numAMContainers >= maxAMContainers) {
          return false;
        }
      }

      containers.add(container);
      return true;
    }
  }
}
