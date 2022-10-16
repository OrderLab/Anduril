/*
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

package org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.converter.weightconversion;

import static org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacitySchedulerConfiguration.PREFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.FSQueue;
import org.junit.Before;
import org.junit.Test;

public class TestWeightToWeightConverter extends WeightConverterTestBase {
  private WeightToWeightConverter converter;
  private Configuration config;

  @Before
  public void setup() {
    converter = new WeightToWeightConverter();
    config = new Configuration(false);
  }

  @Test
  public void testNoChildQueueConversion() {
    FSQueue root = createFSQueues();
    converter.convertWeightsForChildQueues(root, config);

    assertEquals("root weight", "1.0w",
        config.get(PREFIX + "root.capacity"));
    assertEquals("Converted items", 2,
        config.getPropsWithPrefix(PREFIX).size());
  }

  @Test
  public void testSingleWeightConversion() {
    FSQueue root = createFSQueues(1);
    converter.convertWeightsForChildQueues(root, config);

    assertEquals("root weight", "1.0w",
        config.get(PREFIX + "root.capacity"));
    assertEquals("root.a weight", "1.0w",
        config.get(PREFIX + "root.a.capacity"));
    assertEquals("Number of properties", 3,
        config.getPropsWithPrefix(PREFIX).size());
  }

  @Test
  public void testMultiWeightConversion() {
    FSQueue root = createFSQueues(1, 2, 3);

    converter.convertWeightsForChildQueues(root, config);

    assertEquals("Number of properties", 5,
        config.getPropsWithPrefix(PREFIX).size());
    assertEquals("root weight", "1.0w",
        config.get(PREFIX + "root.capacity"));
    assertEquals("root.a weight", "1.0w",
        config.get(PREFIX + "root.a.capacity"));
    assertEquals("root.b weight", "2.0w",
        config.get(PREFIX + "root.b.capacity"));
    assertEquals("root.c weight", "3.0w",
        config.get(PREFIX + "root.c.capacity"));
  }

  @Test
  public void testAutoCreateV2FlagOnParent() {
    FSQueue root = createFSQueues(1);
    converter.convertWeightsForChildQueues(root, config);

    assertTrue("root autocreate v2 enabled",
        config.getBoolean(PREFIX + "root.auto-queue-creation-v2.enabled",
            false));
  }

  @Test
  public void testAutoCreateV2FlagOnParentWithoutChildren() {
    FSQueue root = createParent(new ArrayList<>());
    converter.convertWeightsForChildQueues(root, config);

    assertEquals("Number of properties", 2,
        config.getPropsWithPrefix(PREFIX).size());
    assertTrue("root autocreate v2 enabled",
        config.getBoolean(PREFIX + "root.auto-queue-creation-v2.enabled",
            false));
  }
}
