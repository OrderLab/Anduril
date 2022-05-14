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
package org.apache.hadoop.hdfs;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.MiniDFSCluster.DataNodeProperties;
import org.apache.hadoop.hdfs.protocol.HdfsConstants.RollingUpgradeAction;
import org.apache.hadoop.hdfs.protocol.HdfsConstants.SafeModeAction;
import org.apache.hadoop.hdfs.protocol.RollingUpgradeInfo;
import org.apache.hadoop.hdfs.qjournal.MiniJournalCluster;
import org.apache.hadoop.hdfs.qjournal.MiniQJMHACluster;
import org.apache.hadoop.hdfs.server.common.HdfsServerConstants.StartupOption;
import org.apache.hadoop.hdfs.server.datanode.DataNode;
import org.apache.hadoop.hdfs.server.namenode.CheckpointFaultInjector;
import org.apache.hadoop.hdfs.server.namenode.FSImage;
import org.apache.hadoop.hdfs.server.namenode.NNStorage;
import org.apache.hadoop.hdfs.server.namenode.SecondaryNameNode;
import org.apache.hadoop.hdfs.server.namenode.TestFileTruncate;
import org.apache.hadoop.hdfs.tools.DFSAdmin;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.test.GenericTestUtils;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

/**
 * This class tests rolling upgrade.
 */
public class TestRollingUpgrade {
  private static final Log LOG = LogFactory.getLog(TestRollingUpgrade.class);

  public static void runCmd(DFSAdmin dfsadmin, boolean success,
      String... args) throws  Exception {
    if (success) {
      assertEquals(0, dfsadmin.run(args));
    } else {
      Assert.assertTrue(dfsadmin.run(args) != 0);
    }
  }

  @Test(timeout = 60000)
  public void testRollBackImage() throws Exception {
    final Configuration conf = new Configuration();
    conf.setInt(DFSConfigKeys.DFS_NAMENODE_CHECKPOINT_TXNS_KEY, 10);
    conf.setInt(DFSConfigKeys.DFS_HA_TAILEDITS_PERIOD_KEY, 1);
    conf.setInt(DFSConfigKeys.DFS_NAMENODE_CHECKPOINT_CHECK_PERIOD_KEY, 2);
    MiniQJMHACluster cluster = null;
    CheckpointFaultInjector old = CheckpointFaultInjector.getInstance();
    try {
      cluster = new MiniQJMHACluster.Builder(conf).setNumNameNodes(2).build();
      MiniDFSCluster dfsCluster = cluster.getDfsCluster();
      dfsCluster.waitActive();
      dfsCluster.transitionToActive(0);
      DistributedFileSystem dfs = dfsCluster.getFileSystem(0);
      for (int i = 0; i <= 10; i++) {
        Path foo = new Path("/foo" + i);
        dfs.mkdirs(foo);
      }
      cluster.getDfsCluster().getNameNodeRpc(0).rollEdits();
      CountDownLatch ruEdit = new CountDownLatch(1);
      CheckpointFaultInjector.set(new CheckpointFaultInjector() {
        @Override
        public void duringUploadInProgess()
            throws IOException, InterruptedException {
          if (ruEdit.getCount() == 1) {
            ruEdit.countDown();
            //Thread.sleep(180000);
          }
        }
      });
      //ruEdit.await();
      RollingUpgradeInfo info = dfs
          .rollingUpgrade(RollingUpgradeAction.PREPARE);
      Assert.assertTrue(info.isStarted());
      FSImage fsimage = dfsCluster.getNamesystem(0).getFSImage();
      queryForPreparation(dfs);
      // The NN should have a copy of the fsimage in case of rollbacks.
      Assert.assertTrue(fsimage.hasRollbackFSImage());
    } finally {
      CheckpointFaultInjector.set(old);
      if (cluster != null) {
        cluster.shutdown();
      }
    }
  }

  /**
   * Verify that the namenode at the given index has an FSImage with a TxId up to txid-1
   */
  private void verifyNNCheckpoint(MiniDFSCluster dfsCluster, long txid, int nnIndex) throws InterruptedException {
    int retries = 0;
    while (++retries < 5) {
      NNStorage storage = dfsCluster.getNamesystem(nnIndex).getFSImage()
              .getStorage();
      if (storage.getFsImageName(txid - 1) != null) {
        return;
      }
      Thread.sleep(1000);
    }
    Assert.fail("new checkpoint does not exist");
  }

  static void queryForPreparation(DistributedFileSystem dfs) throws IOException,
      InterruptedException {
    RollingUpgradeInfo info;
    int retries = 0;
    while (++retries < 10) {
      info = dfs.rollingUpgrade(RollingUpgradeAction.QUERY);
      if (info.createdRollbackImages()) {
        break;
      }
      Thread.sleep(1000);
    }

    if (retries >= 10) {
      Assert.fail("Query return false");
    }
  }

}
