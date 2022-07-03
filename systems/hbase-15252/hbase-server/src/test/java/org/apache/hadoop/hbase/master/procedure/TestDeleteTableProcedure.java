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

package org.apache.hadoop.hbase.master.procedure;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.ProcedureInfo;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.TableNotDisabledException;
import org.apache.hadoop.hbase.TableNotFoundException;
import org.apache.hadoop.hbase.procedure2.ProcedureExecutor;
import org.apache.hadoop.hbase.procedure2.ProcedureTestingUtility;
import org.apache.hadoop.hbase.protobuf.generated.MasterProcedureProtos.DeleteTableState;
import org.apache.hadoop.hbase.testclassification.MasterTests;
import org.apache.hadoop.hbase.testclassification.MediumTests;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertTrue;

@Category({MasterTests.class, MediumTests.class})
public class TestDeleteTableProcedure {
  private static final Log LOG = LogFactory.getLog(TestDeleteTableProcedure.class);

  protected static final HBaseTestingUtility UTIL = new HBaseTestingUtility();

  private long nonceGroup = HConstants.NO_NONCE;
  private long nonce = HConstants.NO_NONCE;

  private static void setupConf(Configuration conf) {
    conf.setInt(MasterProcedureConstants.MASTER_PROCEDURE_THREADS, 1);
  }

  @BeforeClass
  public static void setupCluster() throws Exception {
    setupConf(UTIL.getConfiguration());
    UTIL.startMiniCluster(1);
  }

  @AfterClass
  public static void cleanupTest() throws Exception {
    try {
      UTIL.shutdownMiniCluster();
    } catch (Exception e) {
      LOG.warn("failure shutting down cluster", e);
    }
  }

  @Before
  public void setup() throws Exception {
    final ProcedureExecutor<MasterProcedureEnv> procExec = getMasterProcedureExecutor();
    ProcedureTestingUtility.setKillAndToggleBeforeStoreUpdate(procExec, false);
    assertTrue("expected executor to be running", procExec.isRunning());

    nonceGroup =
        MasterProcedureTestingUtility.generateNonceGroup(UTIL.getHBaseCluster().getMaster());
    nonce = MasterProcedureTestingUtility.generateNonce(UTIL.getHBaseCluster().getMaster());
  }

  @After
  public void tearDown() throws Exception {
    ProcedureTestingUtility.setKillAndToggleBeforeStoreUpdate(getMasterProcedureExecutor(), false);
    for (HTableDescriptor htd: UTIL.getHBaseAdmin().listTables()) {
      LOG.info("Tear down, remove table=" + htd.getTableName());
      UTIL.deleteTable(htd.getTableName());
    }
  }

  @Test(timeout=60000, expected=TableNotFoundException.class)
  public void testDeleteNotExistentTable() throws Exception {
    final TableName tableName = TableName.valueOf("testDeleteNotExistentTable");

    final ProcedureExecutor<MasterProcedureEnv> procExec = getMasterProcedureExecutor();
    ProcedurePrepareLatch latch = new ProcedurePrepareLatch.CompatibilityLatch();
    long procId = ProcedureTestingUtility.submitAndWait(procExec,
        new DeleteTableProcedure(procExec.getEnvironment(), tableName, latch));
    latch.await();
  }

  @Test(timeout=60000, expected=TableNotDisabledException.class)
  public void testDeleteNotDisabledTable() throws Exception {
    final TableName tableName = TableName.valueOf("testDeleteNotDisabledTable");

    final ProcedureExecutor<MasterProcedureEnv> procExec = getMasterProcedureExecutor();
    MasterProcedureTestingUtility.createTable(procExec, tableName, null, "f");

    ProcedurePrepareLatch latch = new ProcedurePrepareLatch.CompatibilityLatch();
    long procId = ProcedureTestingUtility.submitAndWait(procExec,
        new DeleteTableProcedure(procExec.getEnvironment(), tableName, latch));
    latch.await();
  }

  @Test(timeout=60000)
  public void testDeleteDeletedTable() throws Exception {
    final TableName tableName = TableName.valueOf("testDeleteDeletedTable");
    final ProcedureExecutor<MasterProcedureEnv> procExec = getMasterProcedureExecutor();

    HRegionInfo[] regions = MasterProcedureTestingUtility.createTable(
      procExec, tableName, null, "f");
    UTIL.getHBaseAdmin().disableTable(tableName);

    // delete the table (that exists)
    long procId1 = procExec.submitProcedure(
        new DeleteTableProcedure(procExec.getEnvironment(), tableName), nonceGroup, nonce);
    // delete the table (that will no longer exist)
    long procId2 = procExec.submitProcedure(
        new DeleteTableProcedure(procExec.getEnvironment(), tableName), nonceGroup + 1, nonce + 1);

    // Wait the completion
    ProcedureTestingUtility.waitProcedure(procExec, procId1);
    ProcedureTestingUtility.waitProcedure(procExec, procId2);

    // First delete should succeed
    ProcedureTestingUtility.assertProcNotFailed(procExec, procId1);
    MasterProcedureTestingUtility.validateTableDeletion(
      UTIL.getHBaseCluster().getMaster(), tableName, regions, "f");

    // Second delete should fail with TableNotFound
    ProcedureInfo result = procExec.getResult(procId2);
    assertTrue(result.isFailed());
    LOG.debug("Delete failed with exception: " + result.getExceptionFullMessage());
    assertTrue(ProcedureTestingUtility.getExceptionCause(result) instanceof TableNotFoundException);
  }

  @Test(timeout=60000)
  public void testDoubleDeletedTableWithSameNonce() throws Exception {
    final TableName tableName = TableName.valueOf("testDoubleDeletedTableWithSameNonce");
    final ProcedureExecutor<MasterProcedureEnv> procExec = getMasterProcedureExecutor();

    HRegionInfo[] regions = MasterProcedureTestingUtility.createTable(
      procExec, tableName, null, "f");
    UTIL.getHBaseAdmin().disableTable(tableName);

    // delete the table (that exists)
    long procId1 = procExec.submitProcedure(
        new DeleteTableProcedure(procExec.getEnvironment(), tableName), nonceGroup, nonce);
    // delete the table (that will no longer exist)
    long procId2 = procExec.submitProcedure(
        new DeleteTableProcedure(procExec.getEnvironment(), tableName), nonceGroup, nonce);

    // Wait the completion
    ProcedureTestingUtility.waitProcedure(procExec, procId1);
    ProcedureTestingUtility.waitProcedure(procExec, procId2);

    // First delete should succeed
    ProcedureTestingUtility.assertProcNotFailed(procExec, procId1);
    MasterProcedureTestingUtility.validateTableDeletion(
      UTIL.getHBaseCluster().getMaster(), tableName, regions, "f");

    // Second delete should not fail, because it is the same delete
    ProcedureTestingUtility.assertProcNotFailed(procExec, procId2);
    assertTrue(procId1 == procId2);
  }

  @Test(timeout=60000)
  public void testSimpleDelete() throws Exception {
    final TableName tableName = TableName.valueOf("testSimpleDelete");
    final byte[][] splitKeys = null;
    testSimpleDelete(tableName, splitKeys);
  }

  @Test(timeout=60000)
  public void testSimpleDeleteWithSplits() throws Exception {
    final TableName tableName = TableName.valueOf("testSimpleDeleteWithSplits");
    final byte[][] splitKeys = new byte[][] {
      Bytes.toBytes("a"), Bytes.toBytes("b"), Bytes.toBytes("c")
    };
    testSimpleDelete(tableName, splitKeys);
  }

  private void testSimpleDelete(final TableName tableName, byte[][] splitKeys) throws Exception {
    HRegionInfo[] regions = MasterProcedureTestingUtility.createTable(
      getMasterProcedureExecutor(), tableName, splitKeys, "f1", "f2");
    UTIL.getHBaseAdmin().disableTable(tableName);

    // delete the table
    final ProcedureExecutor<MasterProcedureEnv> procExec = getMasterProcedureExecutor();
    long procId = ProcedureTestingUtility.submitAndWait(procExec,
      new DeleteTableProcedure(procExec.getEnvironment(), tableName));
    ProcedureTestingUtility.assertProcNotFailed(procExec, procId);
    MasterProcedureTestingUtility.validateTableDeletion(
      UTIL.getHBaseCluster().getMaster(), tableName, regions, "f1", "f2");
  }

  @Test(timeout=60000)
  public void testRecoveryAndDoubleExecution() throws Exception {
    final TableName tableName = TableName.valueOf("testRecoveryAndDoubleExecution");

    // create the table
    byte[][] splitKeys = null;
    HRegionInfo[] regions = MasterProcedureTestingUtility.createTable(
      getMasterProcedureExecutor(), tableName, splitKeys, "f1", "f2");
    UTIL.getHBaseAdmin().disableTable(tableName);

    final ProcedureExecutor<MasterProcedureEnv> procExec = getMasterProcedureExecutor();
    ProcedureTestingUtility.waitNoProcedureRunning(procExec);
    ProcedureTestingUtility.setKillAndToggleBeforeStoreUpdate(procExec, true);

    // Start the Delete procedure && kill the executor
    long procId = procExec.submitProcedure(
      new DeleteTableProcedure(procExec.getEnvironment(), tableName), nonceGroup, nonce);

    // Restart the executor and execute the step twice
    // NOTE: the 6 (number of DeleteTableState steps) is hardcoded,
    //       so you have to look at this test at least once when you add a new step.
    MasterProcedureTestingUtility.testRecoveryAndDoubleExecution(
      procExec, procId, 6, DeleteTableState.values());

    MasterProcedureTestingUtility.validateTableDeletion(
      UTIL.getHBaseCluster().getMaster(), tableName, regions, "f1", "f2");
  }

  private ProcedureExecutor<MasterProcedureEnv> getMasterProcedureExecutor() {
    return UTIL.getHBaseCluster().getMaster().getMasterProcedureExecutor();
  }
}
