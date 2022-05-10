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

package org.apache.hadoop.yarn.util.resource;

import org.apache.hadoop.yarn.api.records.Resource;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestResources {
  
  public Resource createResource(long memory, int vCores) {
    return Resource.newInstance(memory, vCores);
  }

  @Test(timeout=10000)
  public void testCompareToWithUnboundedResource() {
    assertTrue(Resources.unbounded().compareTo(
            createResource(Long.MAX_VALUE, Integer.MAX_VALUE)) == 0);
    assertTrue(Resources.unbounded().compareTo(
        createResource(Long.MAX_VALUE, 0)) > 0);
    assertTrue(Resources.unbounded().compareTo(
        createResource(0, Integer.MAX_VALUE)) > 0);
  }

  @Test(timeout=10000)
  public void testCompareToWithNoneResource() {
    assertTrue(Resources.none().compareTo(createResource(0, 0)) == 0);
    assertTrue(Resources.none().compareTo(
        createResource(1, 0)) < 0);
    assertTrue(Resources.none().compareTo(
        createResource(0, 1)) < 0);
  }

  @Test(timeout=10000)
  public void testMultipleRoundUp() {
    final double by = 0.5;
    final String memoryErrorMsg = "Invalid memory size.";
    final String vcoreErrorMsg = "Invalid virtual core number.";
    Resource resource = Resources.createResource(1, 1);
    Resource result = Resources.multiplyAndRoundUp(resource, by);
    assertEquals(memoryErrorMsg, result.getMemorySize(), 1);
    assertEquals(vcoreErrorMsg, result.getVirtualCores(), 1);

    resource = Resources.createResource(2, 2);
    result = Resources.multiplyAndRoundUp(resource, by);
    assertEquals(memoryErrorMsg, result.getMemorySize(), 1);
    assertEquals(vcoreErrorMsg, result.getVirtualCores(), 1);

    resource = Resources.createResource(0, 0);
    result = Resources.multiplyAndRoundUp(resource, by);
    assertEquals(memoryErrorMsg, result.getMemorySize(), 0);
    assertEquals(vcoreErrorMsg, result.getVirtualCores(), 0);
  }
}
