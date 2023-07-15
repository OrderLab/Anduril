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

package org.apache.hadoop.yarn.server.resourcemanager.placement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.hadoop.classification.InterfaceAudience.Private;
import org.apache.hadoop.security.Groups;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.server.resourcemanager.placement.UserGroupMappingPlacementRule.QueueMapping.MappingType;

import com.google.common.annotations.VisibleForTesting;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.ResourceScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.AutoCreatedLeafQueue;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CSQueue;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacityScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacitySchedulerConfiguration;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacitySchedulerContext;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacitySchedulerQueueManager;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.LeafQueue;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.ManagedParentQueue;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.ParentQueue;

public class UserGroupMappingPlacementRule extends PlacementRule {
  private static final Logger LOG = LoggerFactory
      .getLogger(UserGroupMappingPlacementRule.class);

  public static final String CURRENT_USER_MAPPING = "%user";

  public static final String PRIMARY_GROUP_MAPPING = "%primary_group";

  public static final String SECONDARY_GROUP_MAPPING = "%secondary_group";

  private boolean overrideWithQueueMappings = false;
  private List<QueueMapping> mappings = null;
  private Groups groups;
  private CapacitySchedulerQueueManager queueManager;

  @Private
  public static class QueueMapping {

    public enum MappingType {

      USER("u"), GROUP("g");
      private final String type;

      private MappingType(String type) {
        this.type = type;
      }

      public String toString() {
        return type;
      }

    };

    MappingType type;
    String source;
    String queue;
    String parentQueue;

    public final static String DELIMITER = ":";

    public QueueMapping(MappingType type, String source, String queue) {
      this.type = type;
      this.source = source;
      this.queue = queue;
      this.parentQueue = null;
    }

    public QueueMapping(MappingType type, String source,
        String queue, String parentQueue) {
      this.type = type;
      this.source = source;
      this.queue = queue;
      this.parentQueue = parentQueue;
    }

    public String getQueue() {
      return queue;
    }

    public String getParentQueue() {
      return parentQueue;
    }

    public boolean hasParentQueue() {
      return parentQueue != null;
    }

    public MappingType getType() {
      return type;
    }

    public String getSource() {
      return source;
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof QueueMapping) {
        QueueMapping other = (QueueMapping) obj;
        return (other.type.equals(type) && 
            other.source.equals(source) && 
            other.queue.equals(queue));
      } else {
        return false;
      }
    }

    public String toString() {
      return type.toString() + DELIMITER + source + DELIMITER +
        (parentQueue != null ?
        parentQueue + "." + queue :
        queue);
    }
  }

  public UserGroupMappingPlacementRule(){
    this(false, null, null);
  }

  public UserGroupMappingPlacementRule(boolean overrideWithQueueMappings,
      List<QueueMapping> newMappings, Groups groups) {
    this.mappings = newMappings;
    this.overrideWithQueueMappings = overrideWithQueueMappings;
    this.groups = groups;
  }

  private String getSecondaryGroup(String user) throws IOException {
    List<String> groupsList = groups.getGroups(user);
    String secondaryGroup = null;
    // Traverse all secondary groups (as there could be more than one
    // and position is not guaranteed) and ensure there is queue with
    // the same name
    for (int i = 1; i < groupsList.size(); i++) {
      if (this.queueManager.getQueue(groupsList.get(i)) != null) {
        secondaryGroup = groupsList.get(i);
        break;
      }
    }
    return secondaryGroup;
  }

  private ApplicationPlacementContext getPlacementForUser(String user)
      throws IOException {
    for (QueueMapping mapping : mappings) {
      if (mapping.type == MappingType.USER) {
        if (mapping.source.equals(CURRENT_USER_MAPPING)) {
          if (mapping.getParentQueue() != null
              && mapping.getParentQueue().equals(PRIMARY_GROUP_MAPPING)
              && mapping.getQueue().equals(CURRENT_USER_MAPPING)) {
            QueueMapping queueMapping =
                new QueueMapping(mapping.getType(), mapping.getSource(),
                    user, groups.getGroups(user).get(0));
            validateQueueMapping(queueMapping);
            return getPlacementContext(queueMapping, user);
          } else if (mapping.getParentQueue() != null
              && mapping.getParentQueue().equals(SECONDARY_GROUP_MAPPING)
              && mapping.getQueue().equals(CURRENT_USER_MAPPING)) {
            String secondaryGroup = getSecondaryGroup(user);
            if (secondaryGroup != null) {
              QueueMapping queueMapping = new QueueMapping(mapping.getType(),
                  mapping.getSource(), user, secondaryGroup);
              validateQueueMapping(queueMapping);
              return getPlacementContext(queueMapping, user);
            } else {
              if (LOG.isDebugEnabled()) {
                LOG.debug("User {} is not associated with any Secondary Group. "
                    + "Hence it may use the 'default' queue", user);
              }
              return null;
            }
          } else if (mapping.queue.equals(CURRENT_USER_MAPPING)) {
            return getPlacementContext(mapping, user);
          } else if (mapping.queue.equals(PRIMARY_GROUP_MAPPING)) {
            return getPlacementContext(mapping, groups.getGroups(user).get(0));
          } else if (mapping.queue.equals(SECONDARY_GROUP_MAPPING)) {
            String secondaryGroup = getSecondaryGroup(user);
            if (secondaryGroup != null) {
              return getPlacementContext(mapping, secondaryGroup);
            } else {
              if (LOG.isDebugEnabled()) {
                LOG.debug("User {} is not associated with any Secondary "
                    + "Group. Hence it may use the 'default' queue", user);
              }
              return null;
            }
          } else {
            return getPlacementContext(mapping);
          }
        }
        if (user.equals(mapping.source)) {
          return getPlacementContext(mapping);
        }
      }
      if (mapping.type == MappingType.GROUP) {
        for (String userGroups : groups.getGroups(user)) {
          if (userGroups.equals(mapping.source)) {
            if (mapping.queue.equals(CURRENT_USER_MAPPING)) {
              return getPlacementContext(mapping, user);
            }
            return getPlacementContext(mapping);
          }
        }
      }
    }
    return null;
  }

  @Override
  public ApplicationPlacementContext getPlacementForApp(
      ApplicationSubmissionContext asc, String user)
      throws YarnException {
    String queueName = asc.getQueue();
    ApplicationId applicationId = asc.getApplicationId();
    if (mappings != null && mappings.size() > 0) {
      try {
        ApplicationPlacementContext mappedQueue = getPlacementForUser(user);
        if (mappedQueue != null) {
          // We have a mapping, should we use it?
          if (queueName.equals(YarnConfiguration.DEFAULT_QUEUE_NAME)
              //queueName will be same as mapped queue name in case of recovery
              || queueName.equals(mappedQueue.getQueue())
              || overrideWithQueueMappings) {
            LOG.info("Application {} user {} mapping [{}] to [{}] override {}",
                applicationId, user, queueName, mappedQueue.getQueue(),
                overrideWithQueueMappings);
            return mappedQueue;
          }
        }
      } catch (IOException ioex) {
        String message = "Failed to submit application " + applicationId +
            " submitted by user " + user + " reason: " + ioex.getMessage();
        throw new YarnException(message);
      }
    }
    return null;
  }

  private ApplicationPlacementContext getPlacementContext(
      QueueMapping mapping) {
    return getPlacementContext(mapping, mapping.getQueue());
  }

  private ApplicationPlacementContext getPlacementContext(QueueMapping mapping,
      String leafQueueName) {
    if (!StringUtils.isEmpty(mapping.parentQueue)) {
      return new ApplicationPlacementContext(leafQueueName,
          mapping.getParentQueue());
    } else{
      return new ApplicationPlacementContext(leafQueueName);
    }
  }

  @VisibleForTesting
  @Override
  public boolean initialize(ResourceScheduler scheduler)
      throws IOException {
    if (!(scheduler instanceof CapacityScheduler)) {
      throw new IOException(
          "UserGroupMappingPlacementRule can be configured only for "
              + "CapacityScheduler");
    }
    CapacitySchedulerContext schedulerContext =
        (CapacitySchedulerContext) scheduler;
    CapacitySchedulerConfiguration conf = schedulerContext.getConfiguration();
    boolean overrideWithQueueMappings = conf.getOverrideWithQueueMappings();
    LOG.info(
        "Initialized queue mappings, override: " + overrideWithQueueMappings);

    List<QueueMapping> queueMappings = conf.getQueueMappings();

    // Get new user/group mappings
    List<QueueMapping> newMappings = new ArrayList<>();

    queueManager = schedulerContext.getCapacitySchedulerQueueManager();

    // check if mappings refer to valid queues
    for (QueueMapping mapping : queueMappings) {

      QueuePath queuePath = QueuePlacementRuleUtils
              .extractQueuePath(mapping.getQueue());
      if (isStaticQueueMapping(mapping)) {
        //Try getting queue by its leaf queue name
        // without splitting into parent/leaf queues
        CSQueue queue = queueManager.getQueue(mapping.getQueue());
        if (ifQueueDoesNotExist(queue)) {
          //Try getting the queue by extracting leaf and parent queue names
          //Assuming its a potential auto created leaf queue
          queue = queueManager.getQueue(queuePath.getLeafQueue());

          if (ifQueueDoesNotExist(queue)) {
            //if leaf queue does not exist,
            // this could be a potential auto created leaf queue
            //validate if parent queue is specified,
            // then it should exist and
            // be an instance of AutoCreateEnabledParentQueue
            QueueMapping newMapping = validateAndGetAutoCreatedQueueMapping(
                queueManager, mapping, queuePath);
            if (newMapping == null) {
              throw new IOException(
                  "mapping contains invalid or non-leaf queue " + mapping
                      .getQueue());
            }
            newMappings.add(newMapping);
          } else{
            QueueMapping newMapping = validateAndGetQueueMapping(queueManager,
                queue, mapping, queuePath);
            newMappings.add(newMapping);
          }
        } else{
          // if queue exists, validate
          //   if its an instance of leaf queue
          //   if its an instance of auto created leaf queue,
          // then extract parent queue name and update queue mapping
          QueueMapping newMapping = validateAndGetQueueMapping(queueManager,
              queue, mapping, queuePath);
          newMappings.add(newMapping);
        }
      } else{
        //If it is a dynamic queue mapping,
        // we can safely assume leaf queue name does not have '.' in it
        // validate
        // if parent queue is specified, then
        //  parent queue exists and an instance of AutoCreateEnabledParentQueue
        //
        QueueMapping newMapping = validateAndGetAutoCreatedQueueMapping(
            queueManager, mapping, queuePath);
        if (newMapping != null) {
          newMappings.add(newMapping);
        } else{
          newMappings.add(mapping);
        }
      }
    }

    // initialize groups if mappings are present
    if (newMappings.size() > 0) {
      Groups groups = new Groups(conf);
      this.mappings = newMappings;
      this.groups = groups;
      this.overrideWithQueueMappings = overrideWithQueueMappings;
      return true;
    }
    return false;
  }

  private static QueueMapping validateAndGetQueueMapping(
      CapacitySchedulerQueueManager queueManager, CSQueue queue,
      QueueMapping mapping, QueuePath queuePath) throws IOException {
    if (!(queue instanceof LeafQueue)) {
      throw new IOException(
          "mapping contains invalid or non-leaf queue : " + mapping.getQueue());
    }

    if (queue instanceof AutoCreatedLeafQueue && queue
        .getParent() instanceof ManagedParentQueue) {

      QueueMapping newMapping = validateAndGetAutoCreatedQueueMapping(
          queueManager, mapping, queuePath);
      if (newMapping == null) {
        throw new IOException(
            "mapping contains invalid or non-leaf queue " + mapping.getQueue());
      }
      return newMapping;
    }
    return mapping;
  }

  private static boolean ifQueueDoesNotExist(CSQueue queue) {
    return queue == null;
  }

  private static QueueMapping validateAndGetAutoCreatedQueueMapping(
      CapacitySchedulerQueueManager queueManager, QueueMapping mapping,
      QueuePath queuePath) throws IOException {
    if (queuePath.hasParentQueue()
        && (queuePath.getParentQueue().equals(PRIMARY_GROUP_MAPPING)
            || queuePath.getParentQueue().equals(SECONDARY_GROUP_MAPPING))) {
      // dynamic parent queue
      return new QueueMapping(mapping.getType(), mapping.getSource(),
          queuePath.getLeafQueue(), queuePath.getParentQueue());
    } else if (queuePath.hasParentQueue()) {
      //if parent queue is specified,
      // then it should exist and be an instance of ManagedParentQueue
      QueuePlacementRuleUtils.validateQueueMappingUnderParentQueue(
              queueManager.getQueue(queuePath.getParentQueue()),
          queuePath.getParentQueue(), queuePath.getLeafQueue());
      return new QueueMapping(mapping.getType(), mapping.getSource(),
          queuePath.getLeafQueue(), queuePath.getParentQueue());
    }

    return null;
  }

  private static boolean isStaticQueueMapping(QueueMapping mapping) {
    return !mapping.getQueue()
        .contains(UserGroupMappingPlacementRule.CURRENT_USER_MAPPING)
        && !mapping.getQueue()
            .contains(UserGroupMappingPlacementRule.PRIMARY_GROUP_MAPPING)
        && !mapping.getQueue()
            .contains(UserGroupMappingPlacementRule.SECONDARY_GROUP_MAPPING);
  }

  private void validateQueueMapping(QueueMapping queueMapping)
      throws IOException {
    String parentQueueName = queueMapping.getParentQueue();
    String leafQueueName = queueMapping.getQueue();
    CSQueue parentQueue = queueManager.getQueue(parentQueueName);
    CSQueue leafQueue = queueManager.getQueue(leafQueueName);

    if (leafQueue == null || (!(leafQueue instanceof LeafQueue))) {
      throw new IOException("mapping contains invalid or non-leaf queue : "
          + leafQueueName);
    } else if (parentQueue == null || (!(parentQueue instanceof ParentQueue))) {
      throw new IOException(
          "mapping contains invalid parent queue [" + parentQueueName + "]");
    } else if (!parentQueue.getQueueName()
        .equals(leafQueue.getParent().getQueueName())) {
      throw new IOException("mapping contains invalid parent queue "
          + "which does not match existing leaf queue's parent : ["
          + parentQueue.getQueueName() + "] does not match [ "
          + leafQueue.getParent().getQueueName() + "]");
    }
  }

  @VisibleForTesting
  public List<QueueMapping> getQueueMappings() {
    return mappings;
  }

  @VisibleForTesting
  @Private
  public void setQueueManager(CapacitySchedulerQueueManager queueManager) {
    this.queueManager = queueManager;
  }
}
