/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.util.Sets;
import org.apache.hadoop.yarn.server.resourcemanager.nodelabels.RMNodeLabelsManager;
import static org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacitySchedulerConfiguration.ROOT;
import java.io.IOException;
import java.util.Set;

/**
 * This class determines accessible node labels, configured node labels and the default node
 * label expression based on the {@link CapacitySchedulerConfiguration} object and other queue
 * properties.
 */
public class QueueNodeLabelsSettings {
  private final CSQueue parent;
  private final String queuePath;
  private final CapacitySchedulerContext csContext;
  private Set<String> accessibleLabels;
  private Set<String> configuredNodeLabels;
  private String defaultLabelExpression;

  public QueueNodeLabelsSettings(CapacitySchedulerConfiguration configuration,
      CSQueue parent,
      String queuePath,
      CapacitySchedulerContext csContext) throws IOException {
    this.parent = parent;
    this.queuePath = queuePath;
    this.csContext = csContext;
    initializeNodeLabels(configuration);
  }

  private void initializeNodeLabels(CapacitySchedulerConfiguration configuration)
      throws IOException {
    initializeAccessibleLabels(configuration);
    initializeDefaultLabelExpression(configuration);
    initializeConfiguredNodeLabels();
    validateNodeLabels();
  }

  private void initializeAccessibleLabels(CapacitySchedulerConfiguration configuration) {
    this.accessibleLabels = configuration.getAccessibleNodeLabels(queuePath);
    // Inherit labels from parent if not set
    if (this.accessibleLabels == null && parent != null) {
      this.accessibleLabels = parent.getAccessibleNodeLabels();
    }
  }

  private void initializeDefaultLabelExpression(CapacitySchedulerConfiguration configuration) {
    this.defaultLabelExpression = configuration.getDefaultNodeLabelExpression(queuePath);
    // If the accessible labels is not null and the queue has a parent with a
    // similar set of labels copy the defaultNodeLabelExpression from the parent
    if (this.accessibleLabels != null && parent != null
        && this.defaultLabelExpression == null &&
        this.accessibleLabels.containsAll(parent.getAccessibleNodeLabels())) {
      this.defaultLabelExpression = parent.getDefaultNodeLabelExpression();
    }
  }

  private void initializeConfiguredNodeLabels() {
    if (csContext.getCapacitySchedulerQueueManager() != null
        && csContext.getCapacitySchedulerQueueManager().getConfiguredNodeLabels() != null) {
      if (queuePath.equals(ROOT)) {
        this.configuredNodeLabels = csContext.getCapacitySchedulerQueueManager()
            .getConfiguredNodeLabels().getAllConfiguredLabels();
      } else {
        this.configuredNodeLabels = csContext.getCapacitySchedulerQueueManager()
            .getConfiguredNodeLabels().getLabelsByQueue(queuePath);
      }
    } else {
      // Fallback to suboptimal but correct logic
      this.configuredNodeLabels = csContext.getConfiguration().getConfiguredNodeLabels(queuePath);
    }
  }

  private void validateNodeLabels() throws IOException {
    // Check if labels of this queue is a subset of parent queue, only do this
    // when the queue in question is not root
    if (isNotRoot()) {
      if (parent.getAccessibleNodeLabels() != null && !parent
          .getAccessibleNodeLabels().contains(RMNodeLabelsManager.ANY)) {
        // If parent isn't "*", child shouldn't be "*" too
        if (this.getAccessibleNodeLabels().contains(RMNodeLabelsManager.ANY)) {
          throw new IOException("Parent's accessible queue is not ANY(*), "
              + "but child's accessible queue is " + RMNodeLabelsManager.ANY);
        } else {
          Set<String> diff = Sets.difference(this.getAccessibleNodeLabels(),
              parent.getAccessibleNodeLabels());
          if (!diff.isEmpty()) {
            throw new IOException(String.format(
                "Some labels of child queue is not a subset of parent queue, these labels=[%s]",
                StringUtils.join(diff, ",")));
          }
        }
      }
    }
  }

  private boolean isNotRoot() {
    return parent != null && parent.getParent() != null;
  }

  public boolean isAccessibleToPartition(String nodePartition) {
    // if queue's label is *, it can access any node
    if (accessibleLabels != null && accessibleLabels.contains(RMNodeLabelsManager.ANY)) {
      return true;
    }
    // any queue can access to a node without label
    if (nodePartition == null || nodePartition.equals(RMNodeLabelsManager.NO_LABEL)) {
      return true;
    }
    // a queue can access to a node only if it contains any label of the node
    if (accessibleLabels != null && accessibleLabels.contains(nodePartition)) {
      return true;
    }
    // The partition cannot be accessed
    return false;
  }

  public Set<String> getAccessibleNodeLabels() {
    return accessibleLabels;
  }

  public Set<String> getConfiguredNodeLabels() {
    return configuredNodeLabels;
  }

  public String getDefaultLabelExpression() {
    return defaultLabelExpression;
  }
}
