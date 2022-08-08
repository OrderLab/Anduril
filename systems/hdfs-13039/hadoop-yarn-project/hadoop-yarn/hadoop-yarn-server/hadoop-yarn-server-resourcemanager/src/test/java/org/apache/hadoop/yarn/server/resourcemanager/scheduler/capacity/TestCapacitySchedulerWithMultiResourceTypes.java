/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity;

import com.google.common.collect.ImmutableMap;
import org.apache.hadoop.yarn.api.protocolrecords.AllocateResponse;
import org.apache.hadoop.yarn.api.protocolrecords.ResourceTypes;
import org.apache.hadoop.yarn.api.records.ProfileCapability;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.ResourceInformation;
import org.apache.hadoop.yarn.api.records.ResourceRequest;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.security.ContainerTokenIdentifier;
import org.apache.hadoop.yarn.server.resourcemanager.MockAM;
import org.apache.hadoop.yarn.server.resourcemanager.MockNM;
import org.apache.hadoop.yarn.server.resourcemanager.MockRM;
import org.apache.hadoop.yarn.server.resourcemanager.resource.MockResourceProfileManager;
import org.apache.hadoop.yarn.server.resourcemanager.resource.ResourceProfilesManager;
import org.apache.hadoop.yarn.server.resourcemanager.resource.TestResourceProfiles;
import org.apache.hadoop.yarn.server.resourcemanager.rmapp.RMApp;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNode;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.ResourceScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeUpdateSchedulerEvent;
import org.apache.hadoop.yarn.server.utils.BuilderUtils;
import org.apache.hadoop.yarn.util.resource.DominantResourceCalculator;
import org.apache.hadoop.yarn.util.resource.ResourceUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Test Capacity Scheduler with multiple resource types.
 */
public class TestCapacitySchedulerWithMultiResourceTypes {
  private static String RESOURCE_1 = "res1";
  private final int GB = 1024;

  @Test
  public void testBasicCapacitySchedulerWithProfile() throws Exception {

    // Initialize resource map
    Map<String, ResourceInformation> riMap = new HashMap<>();

    // Initialize mandatory resources
    ResourceInformation memory = ResourceInformation.newInstance(
        ResourceInformation.MEMORY_MB.getName(),
        ResourceInformation.MEMORY_MB.getUnits(),
        YarnConfiguration.DEFAULT_RM_SCHEDULER_MINIMUM_ALLOCATION_MB,
        YarnConfiguration.DEFAULT_RM_SCHEDULER_MAXIMUM_ALLOCATION_MB);
    ResourceInformation vcores = ResourceInformation.newInstance(
        ResourceInformation.VCORES.getName(),
        ResourceInformation.VCORES.getUnits(),
        YarnConfiguration.DEFAULT_RM_SCHEDULER_MINIMUM_ALLOCATION_VCORES,
        YarnConfiguration.DEFAULT_RM_SCHEDULER_MAXIMUM_ALLOCATION_VCORES);
    riMap.put(ResourceInformation.MEMORY_URI, memory);
    riMap.put(ResourceInformation.VCORES_URI, vcores);
    riMap.put(RESOURCE_1, ResourceInformation.newInstance(RESOURCE_1, "", 0,
        ResourceTypes.COUNTABLE, 0, Integer.MAX_VALUE));

    ResourceUtils.initializeResourcesFromResourceInformationMap(riMap);

    CapacitySchedulerConfiguration csconf =
        new CapacitySchedulerConfiguration();
    csconf.setMaximumApplicationMasterResourcePerQueuePercent("root", 100.0f);
    csconf.setMaximumAMResourcePercentPerPartition("root", "", 100.0f);
    csconf.setMaximumApplicationMasterResourcePerQueuePercent("root.default",
        100.0f);
    csconf.setMaximumAMResourcePercentPerPartition("root.default", "", 100.0f);
    csconf.setResourceComparator(DominantResourceCalculator.class);

    YarnConfiguration conf = new YarnConfiguration(csconf);
    // Don't reset resource types since we have already configured resource
    // types
    conf.setBoolean(TestResourceProfiles.TEST_CONF_RESET_RESOURCE_TYPES, false);
    conf.setClass(YarnConfiguration.RM_SCHEDULER, CapacityScheduler.class,
        ResourceScheduler.class);
    conf.setBoolean(YarnConfiguration.RM_RESOURCE_PROFILES_ENABLED, true);

    final MockResourceProfileManager mrpm = new MockResourceProfileManager(
        ImmutableMap.of("res-1", TestUtils
            .createResource(2 * GB, 2, ImmutableMap.of(RESOURCE_1, 2))));

    MockRM rm = new MockRM(conf) {
      @Override
      protected ResourceProfilesManager createResourceProfileManager() {
        return mrpm;
      }
    };
    rm.start();

    CapacityScheduler cs = (CapacityScheduler) rm.getResourceScheduler();
    LeafQueue leafQueue = (LeafQueue) cs.getQueue("default");

    MockNM nm1 = rm.registerNode("h1:1234",
        TestUtils.createResource(8 * GB, 8, ImmutableMap.of(RESOURCE_1, 8)));

    RMApp app1 = rm.submitApp(1 * GB, "app", "user", null, "default");
    MockAM am1 = MockRM.launchAndRegisterAM(app1, rm, nm1);

    Assert.assertEquals(Resource.newInstance(1 * GB, 1),
        leafQueue.getUsedResources());

    RMNode rmNode1 = rm.getRMContext().getRMNodes().get(nm1.getNodeId());

    // Now request resource:
    am1.allocate(Arrays.asList(ResourceRequest.newBuilder().capability(
        Resource.newInstance(1 * GB, 1)).numContainers(1).resourceName("*")
            .profileCapability(ProfileCapability
                .newInstance("res-1",
                    Resource.newInstance(2 * GB, 2))).build()),
        null);

    // Do node heartbeats 1 time and check container allocated.
    cs.handle(new NodeUpdateSchedulerEvent(rmNode1));

    // Now used resource = <mem=1GB, vcore=1> + <mem=2GB,vcore=2,res_1=2>
    Assert.assertEquals(
        TestUtils.createResource(3 * GB, 3, ImmutableMap.of(RESOURCE_1, 2)),
        leafQueue.getUsedResources());

    // Acquire container
    AllocateResponse amResponse = am1.allocate(null, null);
    Assert.assertFalse(amResponse.getAllocatedContainers().isEmpty());
    ContainerTokenIdentifier containerTokenIdentifier =
        BuilderUtils.newContainerTokenIdentifier(
            amResponse.getAllocatedContainers().get(0).getContainerToken());
    Assert.assertEquals(
        TestUtils.createResource(2 * GB, 2, ImmutableMap.of(RESOURCE_1, 2)),
        containerTokenIdentifier.getResource());
  }

  @Test
  public void testMaximumAllocationRefreshWithMultipleResourceTypes() throws Exception {

    // Initialize resource map
    Map<String, ResourceInformation> riMap = new HashMap<>();

    // Initialize mandatory resources
    ResourceInformation memory = ResourceInformation.newInstance(
        ResourceInformation.MEMORY_MB.getName(),
        ResourceInformation.MEMORY_MB.getUnits(),
        YarnConfiguration.DEFAULT_RM_SCHEDULER_MINIMUM_ALLOCATION_MB,
        YarnConfiguration.DEFAULT_RM_SCHEDULER_MAXIMUM_ALLOCATION_MB);
    ResourceInformation vcores = ResourceInformation.newInstance(
        ResourceInformation.VCORES.getName(),
        ResourceInformation.VCORES.getUnits(),
        YarnConfiguration.DEFAULT_RM_SCHEDULER_MINIMUM_ALLOCATION_VCORES,
        YarnConfiguration.DEFAULT_RM_SCHEDULER_MAXIMUM_ALLOCATION_VCORES);
    riMap.put(ResourceInformation.MEMORY_URI, memory);
    riMap.put(ResourceInformation.VCORES_URI, vcores);
    riMap.put(RESOURCE_1, ResourceInformation.newInstance(RESOURCE_1, "", 0,
        ResourceTypes.COUNTABLE, 0, 3333L));

    ResourceUtils.initializeResourcesFromResourceInformationMap(riMap);

    CapacitySchedulerConfiguration csconf =
        new CapacitySchedulerConfiguration();
    csconf.setMaximumApplicationMasterResourcePerQueuePercent("root", 100.0f);
    csconf.setMaximumAMResourcePercentPerPartition("root", "", 100.0f);
    csconf.setMaximumApplicationMasterResourcePerQueuePercent("root.default",
        100.0f);
    csconf.setMaximumAMResourcePercentPerPartition("root.default", "", 100.0f);
    csconf.setResourceComparator(DominantResourceCalculator.class);
    csconf.set(YarnConfiguration.RESOURCE_TYPES, RESOURCE_1);
    csconf.setInt(YarnConfiguration.RESOURCE_TYPES + "." + RESOURCE_1
        + ".maximum-allocation", 3333);

    YarnConfiguration conf = new YarnConfiguration(csconf);
    // Don't reset resource types since we have already configured resource
    // types
    conf.setBoolean(TestResourceProfiles.TEST_CONF_RESET_RESOURCE_TYPES, false);
    conf.setClass(YarnConfiguration.RM_SCHEDULER, CapacityScheduler.class,
        ResourceScheduler.class);

    MockRM rm = new MockRM(conf);
    rm.start();

    CapacityScheduler cs = (CapacityScheduler) rm.getResourceScheduler();
    Assert.assertEquals(3333L,
        cs.getMaximumResourceCapability().getResourceValue(RESOURCE_1));
    Assert.assertEquals(3333L,
        cs.getMaximumAllocation().getResourceValue(RESOURCE_1));
    Assert.assertEquals(
        YarnConfiguration.DEFAULT_RM_SCHEDULER_MAXIMUM_ALLOCATION_MB,
        cs.getMaximumResourceCapability()
            .getResourceValue(ResourceInformation.MEMORY_URI));
    Assert.assertEquals(
        YarnConfiguration.DEFAULT_RM_SCHEDULER_MAXIMUM_ALLOCATION_MB,
        cs.getMaximumAllocation()
            .getResourceValue(ResourceInformation.MEMORY_URI));
    Assert.assertEquals(
        YarnConfiguration.DEFAULT_RM_SCHEDULER_MAXIMUM_ALLOCATION_VCORES,
        cs.getMaximumResourceCapability()
            .getResourceValue(ResourceInformation.VCORES_URI));
    Assert.assertEquals(
        YarnConfiguration.DEFAULT_RM_SCHEDULER_MAXIMUM_ALLOCATION_VCORES,
        cs.getMaximumAllocation()
            .getResourceValue(ResourceInformation.VCORES_URI));

    // Set RES_1 to 3332 (less than 3333) and refresh CS, failures expected.
    csconf.set(YarnConfiguration.RESOURCE_TYPES, RESOURCE_1);
    csconf.setInt(YarnConfiguration.RESOURCE_TYPES + "." + RESOURCE_1
        + ".maximum-allocation", 3332);

    boolean exception = false;
    try {
      cs.reinitialize(csconf, rm.getRMContext());
    } catch (IOException e) {
      exception = true;
    }

    Assert.assertTrue("Should have exception in CS", exception);

    // Maximum allocation won't be updated
    Assert.assertEquals(3333L,
        cs.getMaximumResourceCapability().getResourceValue(RESOURCE_1));
    Assert.assertEquals(3333L,
        cs.getMaximumAllocation().getResourceValue(RESOURCE_1));
    Assert.assertEquals(
        YarnConfiguration.DEFAULT_RM_SCHEDULER_MAXIMUM_ALLOCATION_MB,
        cs.getMaximumResourceCapability()
            .getResourceValue(ResourceInformation.MEMORY_URI));
    Assert.assertEquals(
        YarnConfiguration.DEFAULT_RM_SCHEDULER_MAXIMUM_ALLOCATION_MB,
        cs.getMaximumAllocation()
            .getResourceValue(ResourceInformation.MEMORY_URI));
    Assert.assertEquals(
        YarnConfiguration.DEFAULT_RM_SCHEDULER_MAXIMUM_ALLOCATION_VCORES,
        cs.getMaximumResourceCapability()
            .getResourceValue(ResourceInformation.VCORES_URI));
    Assert.assertEquals(
        YarnConfiguration.DEFAULT_RM_SCHEDULER_MAXIMUM_ALLOCATION_VCORES,
        cs.getMaximumAllocation()
            .getResourceValue(ResourceInformation.VCORES_URI));

    // Set RES_1 to 3334 and refresh CS, should success
    csconf.set(YarnConfiguration.RESOURCE_TYPES, RESOURCE_1);
    csconf.setInt(YarnConfiguration.RESOURCE_TYPES + "." + RESOURCE_1
        + ".maximum-allocation", 3334);
    cs.reinitialize(csconf, rm.getRMContext());

    // Maximum allocation will be updated
    Assert.assertEquals(3334,
        cs.getMaximumResourceCapability().getResourceValue(RESOURCE_1));

    // Since we haven't updated the real configuration of ResourceUtils,
    // cs.getMaximumAllocation won't be updated.
    Assert.assertEquals(3333,
        cs.getMaximumAllocation().getResourceValue(RESOURCE_1));
    Assert.assertEquals(
        YarnConfiguration.DEFAULT_RM_SCHEDULER_MAXIMUM_ALLOCATION_MB,
        cs.getMaximumResourceCapability()
            .getResourceValue(ResourceInformation.MEMORY_URI));
    Assert.assertEquals(
        YarnConfiguration.DEFAULT_RM_SCHEDULER_MAXIMUM_ALLOCATION_MB,
        cs.getMaximumAllocation()
            .getResourceValue(ResourceInformation.MEMORY_URI));
    Assert.assertEquals(
        YarnConfiguration.DEFAULT_RM_SCHEDULER_MAXIMUM_ALLOCATION_VCORES,
        cs.getMaximumResourceCapability()
            .getResourceValue(ResourceInformation.VCORES_URI));
    Assert.assertEquals(
        YarnConfiguration.DEFAULT_RM_SCHEDULER_MAXIMUM_ALLOCATION_VCORES,
        cs.getMaximumAllocation()
            .getResourceValue(ResourceInformation.VCORES_URI));

    rm.close();
  }
}
