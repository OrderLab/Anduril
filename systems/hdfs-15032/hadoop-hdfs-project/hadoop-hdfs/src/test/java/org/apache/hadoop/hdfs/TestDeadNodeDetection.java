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

import com.google.common.base.Supplier;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.hadoop.test.GenericTestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.apache.hadoop.hdfs.client.HdfsClientConfigKeys.DFS_CLIENT_DEAD_NODE_DETECTION_DEAD_NODE_QUEUE_MAX_KEY;
import static org.apache.hadoop.hdfs.client.HdfsClientConfigKeys.DFS_CLIENT_DEAD_NODE_DETECTION_ENABLED_KEY;
import static org.apache.hadoop.hdfs.client.HdfsClientConfigKeys.DFS_CLIENT_DEAD_NODE_DETECTION_PROBE_DEAD_NODE_INTERVAL_MS_KEY;
import static org.apache.hadoop.hdfs.client.HdfsClientConfigKeys.DFS_CLIENT_DEAD_NODE_DETECTION_PROBE_SUSPECT_NODE_INTERVAL_MS_KEY;
import static org.apache.hadoop.hdfs.client.HdfsClientConfigKeys.DFS_CLIENT_DEAD_NODE_DETECTION_SUSPECT_NODE_QUEUE_MAX_KEY;
import static org.junit.Assert.assertEquals;

/**
 * Tests for dead node detection in DFSClient.
 */
public class TestDeadNodeDetection {

  private MiniDFSCluster cluster;
  private Configuration conf;

  @Before
  public void setUp() {
    cluster = null;
    conf = new HdfsConfiguration();
    conf.setBoolean(DFS_CLIENT_DEAD_NODE_DETECTION_ENABLED_KEY, true);
    conf.setLong(
        DFS_CLIENT_DEAD_NODE_DETECTION_PROBE_DEAD_NODE_INTERVAL_MS_KEY, 1000);
    conf.setLong(
        DFS_CLIENT_DEAD_NODE_DETECTION_PROBE_SUSPECT_NODE_INTERVAL_MS_KEY, 100);
  }

  @After
  public void tearDown() {
    if (cluster != null) {
      cluster.shutdown();
    }
  }

  @Test
  public void testDeadNodeDetectionInBackground() throws Exception {
    cluster = new MiniDFSCluster.Builder(conf).numDataNodes(3).build();
    cluster.waitActive();

    FileSystem fs = cluster.getFileSystem();
    Path filePath = new Path("/testDetectDeadNodeInBackground");

    // 256 bytes data chunk for writes
    byte[] bytes = new byte[256];
    for (int index = 0; index < bytes.length; index++) {
      bytes[index] = '0';
    }

    // File with a 512 bytes block size
    FSDataOutputStream out = fs.create(filePath, true, 4096, (short) 3, 512);

    // Write a block to all 3 DNs (2x256bytes).
    out.write(bytes);
    out.write(bytes);
    out.hflush();
    out.close();

    // Remove three DNs,
    cluster.stopDataNode(0);
    cluster.stopDataNode(0);
    cluster.stopDataNode(0);

    FSDataInputStream in = fs.open(filePath);
    DFSInputStream din = (DFSInputStream) in.getWrappedStream();
    DFSClient dfsClient = din.getDFSClient();
    try {
      try {
        in.read();
      } catch (BlockMissingException e) {
      }

      waitForDeadNode(dfsClient, 3);
      assertEquals(3, dfsClient.getDeadNodes(din).size());
      assertEquals(3, dfsClient.getClientContext().getDeadNodeDetector()
          .clearAndGetDetectedDeadNodes().size());
    } finally {
      in.close();
      fs.delete(filePath, true);
      // check the dead node again here, the dead node is expected be removed
      assertEquals(0, dfsClient.getDeadNodes(din).size());
      assertEquals(0, dfsClient.getClientContext().getDeadNodeDetector()
          .clearAndGetDetectedDeadNodes().size());
    }
  }

  @Test
  public void testDeadNodeDetectionInMultipleDFSInputStream()
      throws IOException {
    cluster = new MiniDFSCluster.Builder(conf).numDataNodes(1).build();
    cluster.waitActive();

    FileSystem fs = cluster.getFileSystem();
    Path filePath = new Path("/testDeadNodeMultipleDFSInputStream");
    createFile(fs, filePath);

    String datanodeUuid = cluster.getDataNodes().get(0).getDatanodeUuid();
    FSDataInputStream in1 = fs.open(filePath);
    DFSInputStream din1 = (DFSInputStream) in1.getWrappedStream();
    DFSClient dfsClient1 = din1.getDFSClient();
    cluster.stopDataNode(0);

    FSDataInputStream in2 = fs.open(filePath);
    DFSInputStream din2 = null;
    DFSClient dfsClient2 = null;
    try {
      try {
        in1.read();
      } catch (BlockMissingException e) {
      }

      din2 = (DFSInputStream) in2.getWrappedStream();
      dfsClient2 = din2.getDFSClient();
      assertEquals(dfsClient1.toString(), dfsClient2.toString());
      assertEquals(1, dfsClient1.getDeadNodes(din1).size());
      assertEquals(1, dfsClient2.getDeadNodes(din2).size());
      assertEquals(1, dfsClient1.getClientContext().getDeadNodeDetector()
          .clearAndGetDetectedDeadNodes().size());
      assertEquals(1, dfsClient2.getClientContext().getDeadNodeDetector()
          .clearAndGetDetectedDeadNodes().size());
      // check the dn uuid of dead node to see if its expected dead node
      assertEquals(datanodeUuid,
          ((DatanodeInfo) dfsClient1.getClientContext().getDeadNodeDetector()
              .clearAndGetDetectedDeadNodes().toArray()[0]).getDatanodeUuid());
      assertEquals(datanodeUuid,
          ((DatanodeInfo) dfsClient2.getClientContext().getDeadNodeDetector()
              .clearAndGetDetectedDeadNodes().toArray()[0]).getDatanodeUuid());
    } finally {
      in1.close();
      in2.close();
      deleteFile(fs, filePath);
      // check the dead node again here, the dead node is expected be removed
      assertEquals(0, dfsClient1.getDeadNodes(din1).size());
      assertEquals(0, dfsClient2.getDeadNodes(din2).size());
      assertEquals(0, dfsClient1.getClientContext().getDeadNodeDetector()
          .clearAndGetDetectedDeadNodes().size());
      assertEquals(0, dfsClient2.getClientContext().getDeadNodeDetector()
          .clearAndGetDetectedDeadNodes().size());
    }
  }

  @Test
  public void testDeadNodeDetectionDeadNodeRecovery() throws Exception {
    cluster = new MiniDFSCluster.Builder(conf).numDataNodes(3).build();
    cluster.waitActive();

    FileSystem fs = cluster.getFileSystem();
    Path filePath = new Path("/testDeadNodeDetectionDeadNodeRecovery");
    createFile(fs, filePath);

    // Remove three DNs,
    MiniDFSCluster.DataNodeProperties one = cluster.stopDataNode(0);
    cluster.stopDataNode(0);
    cluster.stopDataNode(0);

    FSDataInputStream in = fs.open(filePath);
    DFSInputStream din = (DFSInputStream) in.getWrappedStream();
    DFSClient dfsClient = din.getDFSClient();
    try {
      try {
        in.read();
      } catch (BlockMissingException e) {
      }

      waitForDeadNode(dfsClient, 3);
      assertEquals(3, dfsClient.getDeadNodes(din).size());
      assertEquals(3, dfsClient.getClientContext().getDeadNodeDetector()
          .clearAndGetDetectedDeadNodes().size());

      cluster.restartDataNode(one, true);
      waitForDeadNode(dfsClient, 2);
      assertEquals(2, dfsClient.getDeadNodes(din).size());
      assertEquals(2, dfsClient.getClientContext().getDeadNodeDetector()
          .clearAndGetDetectedDeadNodes().size());
    } finally {
      in.close();
      deleteFile(fs, filePath);
      assertEquals(0, dfsClient.getDeadNodes(din).size());
      assertEquals(0, dfsClient.getClientContext().getDeadNodeDetector()
          .clearAndGetDetectedDeadNodes().size());
    }
  }

  @Test
  public void testDeadNodeDetectionMaxDeadNodesProbeQueue() throws Exception {
    conf.setInt(DFS_CLIENT_DEAD_NODE_DETECTION_DEAD_NODE_QUEUE_MAX_KEY, 1);
    cluster = new MiniDFSCluster.Builder(conf).numDataNodes(3).build();
    cluster.waitActive();

    FileSystem fs = cluster.getFileSystem();
    Path filePath = new Path("/testDeadNodeDetectionMaxDeadNodesProbeQueue");
    createFile(fs, filePath);

    // Remove three DNs,
    cluster.stopDataNode(0);
    cluster.stopDataNode(0);
    cluster.stopDataNode(0);

    FSDataInputStream in = fs.open(filePath);
    DFSInputStream din = (DFSInputStream) in.getWrappedStream();
    DFSClient dfsClient = din.getDFSClient();
    try {
      try {
        in.read();
      } catch (BlockMissingException e) {
      }

      Thread.sleep(1500);
      Assert.assertTrue((dfsClient.getClientContext().getDeadNodeDetector()
          .getDeadNodesProbeQueue().size()
          + dfsClient.getDeadNodes(din).size()) <= 4);
    } finally {
      in.close();
      deleteFile(fs, filePath);
    }
  }

  @Test
  public void testDeadNodeDetectionSuspectNode() throws Exception {
    conf.setInt(DFS_CLIENT_DEAD_NODE_DETECTION_SUSPECT_NODE_QUEUE_MAX_KEY, 1);
    DeadNodeDetector.disabledProbeThreadForTest();
    cluster = new MiniDFSCluster.Builder(conf).numDataNodes(1).build();
    cluster.waitActive();

    FileSystem fs = cluster.getFileSystem();
    Path filePath = new Path("/testDeadNodeDetectionSuspectNode");
    createFile(fs, filePath);

    MiniDFSCluster.DataNodeProperties one = cluster.stopDataNode(0);

    FSDataInputStream in = fs.open(filePath);
    DFSInputStream din = (DFSInputStream) in.getWrappedStream();
    DFSClient dfsClient = din.getDFSClient();
    DeadNodeDetector deadNodeDetector =
        dfsClient.getClientContext().getDeadNodeDetector();
    try {
      try {
        in.read();
      } catch (BlockMissingException e) {
      }
      waitForSuspectNode(din.getDFSClient());
      cluster.restartDataNode(one, true);
      Assert.assertEquals(1,
          deadNodeDetector.getSuspectNodesProbeQueue().size());
      Assert.assertEquals(0,
          deadNodeDetector.clearAndGetDetectedDeadNodes().size());
      deadNodeDetector.startProbeScheduler();
      Thread.sleep(1000);
      Assert.assertEquals(0,
          deadNodeDetector.getSuspectNodesProbeQueue().size());
      Assert.assertEquals(0,
          deadNodeDetector.clearAndGetDetectedDeadNodes().size());
    } finally {
      in.close();
      deleteFile(fs, filePath);
      assertEquals(0, dfsClient.getDeadNodes(din).size());
      assertEquals(0, dfsClient.getClientContext().getDeadNodeDetector()
          .clearAndGetDetectedDeadNodes().size());
    }
  }

  private void createFile(FileSystem fs, Path filePath) throws IOException {
    FSDataOutputStream out = null;
    try {
      // 256 bytes data chunk for writes
      byte[] bytes = new byte[256];
      for (int index = 0; index < bytes.length; index++) {
        bytes[index] = '0';
      }

      // File with a 512 bytes block size
      out = fs.create(filePath, true, 4096, (short) 3, 512);

      // Write a block to all 3 DNs (2x256bytes).
      out.write(bytes);
      out.write(bytes);
      out.hflush();

    } finally {
      out.close();
    }
  }

  private void deleteFile(FileSystem fs, Path filePath) throws IOException {
    fs.delete(filePath, true);
  }

  private void waitForDeadNode(DFSClient dfsClient, int size) throws Exception {
    GenericTestUtils.waitFor(new Supplier<Boolean>() {
      @Override
      public Boolean get() {
        try {
          if (dfsClient.getClientContext().getDeadNodeDetector()
              .clearAndGetDetectedDeadNodes().size() == size) {
            return true;
          }
        } catch (Exception e) {
          // Ignore the exception
        }

        return false;
      }
    }, 5000, 100000);
  }

  private void waitForSuspectNode(DFSClient dfsClient) throws Exception {
    GenericTestUtils.waitFor(new Supplier<Boolean>() {
      @Override
      public Boolean get() {
        try {
          if (dfsClient.getClientContext().getDeadNodeDetector()
              .getSuspectNodesProbeQueue().size() > 0) {
            return true;
          }
        } catch (Exception e) {
          // Ignore the exception
        }

        return false;
      }
    }, 5000, 100000);
  }
}
