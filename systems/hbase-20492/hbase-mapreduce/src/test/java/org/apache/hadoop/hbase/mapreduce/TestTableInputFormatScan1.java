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
package org.apache.hadoop.hbase.mapreduce;

import java.io.IOException;
import org.apache.hadoop.hbase.HBaseClassTestRule;
import org.apache.hadoop.hbase.testclassification.LargeTests;
import org.apache.hadoop.hbase.testclassification.VerySlowMapReduceTests;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * TestTableInputFormatScan part 1.
 * @see TestTableInputFormatScanBase
 */
@Category({VerySlowMapReduceTests.class, LargeTests.class})
public class TestTableInputFormatScan1 extends TestTableInputFormatScanBase {

  @ClassRule
  public static final HBaseClassTestRule CLASS_RULE =
      HBaseClassTestRule.forClass(TestTableInputFormatScan1.class);

  /**
   * Tests a MR scan using specific start and stop rows.
   *
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws InterruptedException
   */
  @Test
  public void testScanEmptyToEmpty()
  throws IOException, InterruptedException, ClassNotFoundException {
    testScan(null, null, null);
  }

  /**
   * Tests a MR scan using specific start and stop rows.
   *
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws InterruptedException
   */
  @Test
  public void testScanEmptyToAPP()
  throws IOException, InterruptedException, ClassNotFoundException {
    testScan(null, "app", "apo");
  }

  /**
   * Tests a MR scan using specific start and stop rows.
   *
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws InterruptedException
   */
  @Test
  public void testScanEmptyToBBA()
  throws IOException, InterruptedException, ClassNotFoundException {
    testScan(null, "bba", "baz");
  }

  /**
   * Tests a MR scan using specific start and stop rows.
   *
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws InterruptedException
   */
  @Test
  public void testScanEmptyToBBB()
  throws IOException, InterruptedException, ClassNotFoundException {
    testScan(null, "bbb", "bba");
  }

  /**
   * Tests a MR scan using specific start and stop rows.
   *
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws InterruptedException
   */
  @Test
  public void testScanEmptyToOPP()
  throws IOException, InterruptedException, ClassNotFoundException {
    testScan(null, "opp", "opo");
  }

  /**
   * Tests a MR scan using specific number of mappers. The test table has 26 regions,
   *
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws InterruptedException
   */
  @Test
    public void testGetSplits() throws IOException, InterruptedException, ClassNotFoundException {
      testNumOfSplits(1, 26);
      testNumOfSplits(3, 78);
    }

  /**
   * Runs a MR to test TIF using specific number of mappers. The test table has 26 regions,
   * @throws InterruptedException
   * @throws IOException
   * @throws ClassNotFoundException
   */
  @Test
  public void testSpecifiedNumOfMappersMR()
      throws InterruptedException, IOException, ClassNotFoundException {
    testNumOfSplitsMR(2, 52);
    testNumOfSplitsMR(4, 104);
  }

  /**
   * Test if autoBalance create correct splits
   * @throws IOException
   */
  @Test
  public void testAutoBalanceSplits() throws IOException {
    testAutobalanceNumOfSplit();
  }

}
