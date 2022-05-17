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

import java.io.DataOutputStream;
import java.io.IOException;
import java.security.PrivilegedExceptionAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.protocol.HdfsConstants.SafeModeAction;
import org.apache.hadoop.hdfs.protocol.ClientProtocol;
import org.apache.hadoop.hdfs.server.namenode.NameNodeAdapter;
import org.apache.hadoop.hdfs.server.protocol.NamenodeProtocols;
import org.apache.hadoop.ipc.RemoteException;
import org.apache.hadoop.security.token.SecretManager.InvalidToken;
import org.apache.hadoop.security.UserGroupInformation;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;

public class TestLease {
  static boolean hasLease(MiniDFSCluster cluster, Path src) {
    return NameNodeAdapter.getLeaseManager(cluster.getNamesystem()
        ).getLeaseByPath(src.toString()) != null;
  }

  static final String dirString = "/test/lease";
  final Path dir = new Path(dirString);
  static final Log LOG = LogFactory.getLog(TestLease.class);
  Configuration conf = new HdfsConfiguration();

  @Test
  public void testLeaseAbort() throws Exception {
    MiniDFSCluster cluster =
        new MiniDFSCluster.Builder(conf).numDataNodes(2).build();
    try {
      cluster.waitActive();
      NamenodeProtocols preSpyNN = cluster.getNameNodeRpc();
      NamenodeProtocols spyNN = spy(preSpyNN);

      DFSClient dfs = new DFSClient(null, spyNN, conf, null);
      byte[] buf = new byte[1024];

      FSDataOutputStream c_out = createFsOut(dfs, dirString + "c");
      c_out.write(buf, 0, 1024);
      c_out.close();

      DFSInputStream c_in = dfs.open(dirString + "c");
      FSDataOutputStream d_out = createFsOut(dfs, dirString + "d");

      // stub the renew method.
      doThrow(new RemoteException(InvalidToken.class.getName(),
          "Your token is worthless")).when(spyNN).renewLease(anyString());

      // We don't need to wait the lease renewer thread to act.
      // call renewLease() manually.
      // make it look like lease has already expired.
      LeaseRenewer originalRenewer = dfs.getLeaseRenewer();
      dfs.lastLeaseRenewal = System.currentTimeMillis() - 300000;
      dfs.renewLease();

      // this should not work.
      try {
        d_out.write(buf, 0, 1024);
        d_out.close();
        Assert.fail("Write did not fail even after the fatal lease renewal failure");
      } catch (IOException e) {
        LOG.info("Write failed as expected. ", e);
      }

      // If aborted, the renewer should be empty. (no reference to clients)
      Thread.sleep(1000);
      Assert.assertTrue(originalRenewer.isEmpty());

      // unstub
      doNothing().when(spyNN).renewLease(anyString());

      // existing input streams should work
      try {
        int num = c_in.read(buf, 0, 1);
        if (num != 1) {
          Assert.fail("Failed to read 1 byte");
        }
        c_in.close();
      } catch (IOException e) {
         LOG.error("Read failed with ", e);
         Assert.fail("Read after lease renewal failure failed");
      }

      // new file writes should work.
      try {
        c_out = createFsOut(dfs, dirString + "c");
        c_out.write(buf, 0, 1024);
        c_out.close();
      } catch (IOException e) {
        LOG.error("Write failed with ", e);
        Assert.fail("Write failed");
      }
    } finally {
      cluster.shutdown();
    }
  }

  @Test
  public void testLease() throws Exception {
    MiniDFSCluster cluster = new MiniDFSCluster.Builder(conf).numDataNodes(2).build();
    try {
      FileSystem fs = cluster.getFileSystem();
      Assert.assertTrue(fs.mkdirs(dir));
      
      Path a = new Path(dir, "a");
      Path b = new Path(dir, "b");

      DataOutputStream a_out = fs.create(a);
      a_out.writeBytes("something");

      Assert.assertTrue(hasLease(cluster, a));
      Assert.assertTrue(!hasLease(cluster, b));
      
      DataOutputStream b_out = fs.create(b);
      b_out.writeBytes("something");

      Assert.assertTrue(hasLease(cluster, a));
      Assert.assertTrue(hasLease(cluster, b));

      a_out.close();
      b_out.close();

      Assert.assertTrue(!hasLease(cluster, a));
      Assert.assertTrue(!hasLease(cluster, b));
      
      fs.delete(dir, true);
    } finally {
      if (cluster != null) {cluster.shutdown();}
    }
  }

  @Test
  public void testFactory() throws Exception {
    final String[] groups = new String[]{"supergroup"};
    final UserGroupInformation[] ugi = new UserGroupInformation[3];
    for(int i = 0; i < ugi.length; i++) {
      ugi[i] = UserGroupInformation.createUserForTesting("user" + i, groups);
    }

    final Configuration conf = new Configuration();
    final DFSClient c1 = createDFSClientAs(ugi[0], conf);
    FSDataOutputStream out1 = createFsOut(c1, "/out1");
    final DFSClient c2 = createDFSClientAs(ugi[0], conf);
    FSDataOutputStream out2 = createFsOut(c2, "/out2");
    Assert.assertEquals(c1.getLeaseRenewer(), c2.getLeaseRenewer());
    final DFSClient c3 = createDFSClientAs(ugi[1], conf);
    FSDataOutputStream out3 = createFsOut(c3, "/out3");
    Assert.assertTrue(c1.getLeaseRenewer() != c3.getLeaseRenewer());
    final DFSClient c4 = createDFSClientAs(ugi[1], conf);
    FSDataOutputStream out4 = createFsOut(c4, "/out4");
    Assert.assertEquals(c3.getLeaseRenewer(), c4.getLeaseRenewer());
    final DFSClient c5 = createDFSClientAs(ugi[2], conf);
    FSDataOutputStream out5 = createFsOut(c5, "/out5");
    Assert.assertTrue(c1.getLeaseRenewer() != c5.getLeaseRenewer());
    Assert.assertTrue(c3.getLeaseRenewer() != c5.getLeaseRenewer());
  }
  
  private FSDataOutputStream createFsOut(DFSClient dfs, String path) 
      throws IOException {
    return new FSDataOutputStream(dfs.create(path, true), null);
  }

  static final ClientProtocol mcp = Mockito.mock(ClientProtocol.class);
  static public DFSClient createDFSClientAs(UserGroupInformation ugi, 
      final Configuration conf) throws Exception {
    return ugi.doAs(new PrivilegedExceptionAction<DFSClient>() {
      @Override
      public DFSClient run() throws Exception {
        return new DFSClient(null, mcp, conf, null);
      }
    });
  }
}
