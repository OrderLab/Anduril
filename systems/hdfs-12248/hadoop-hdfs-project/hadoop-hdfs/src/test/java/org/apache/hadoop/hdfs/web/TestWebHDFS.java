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

package org.apache.hadoop.hdfs.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.BlockStoragePolicySpi;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.fs.StorageType;
import org.apache.hadoop.fs.contract.ContractTestUtils;
import org.apache.hadoop.fs.permission.AclEntry;
import org.apache.hadoop.fs.permission.AclEntryScope;
import org.apache.hadoop.fs.permission.AclEntryType;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hdfs.DFSConfigKeys;
import org.apache.hadoop.hdfs.DFSTestUtil;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.hdfs.TestDFSClientRetries;
import org.apache.hadoop.hdfs.TestFileCreation;
import org.apache.hadoop.hdfs.client.HdfsClientConfigKeys;
import org.apache.hadoop.hdfs.protocol.BlockStoragePolicy;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.hadoop.hdfs.protocol.SystemErasureCodingPolicies;
import org.apache.hadoop.hdfs.protocol.ExtendedBlock;
import org.apache.hadoop.hdfs.protocol.HdfsConstants;
import org.apache.hadoop.hdfs.protocol.LocatedBlock;
import org.apache.hadoop.hdfs.protocol.LocatedBlocks;
import org.apache.hadoop.hdfs.protocol.SnapshottableDirectoryStatus;
import org.apache.hadoop.hdfs.server.namenode.NameNode;
import org.apache.hadoop.hdfs.server.namenode.snapshot.SnapshotTestHelper;
import org.apache.hadoop.hdfs.server.namenode.web.resources.NamenodeWebHdfsMethods;
import org.apache.hadoop.hdfs.server.protocol.NamenodeProtocols;
import org.apache.hadoop.hdfs.web.WebHdfsFileSystem.WebHdfsInputStream;
import org.apache.hadoop.hdfs.web.resources.LengthParam;
import org.apache.hadoop.hdfs.web.resources.NoRedirectParam;
import org.apache.hadoop.hdfs.web.resources.OffsetParam;
import org.apache.hadoop.hdfs.web.resources.Param;
import org.apache.hadoop.http.HttpServer2;
import org.apache.hadoop.http.HttpServerFunctionalTest;
import org.apache.hadoop.io.retry.RetryPolicy;
import org.apache.hadoop.io.retry.RetryPolicy.RetryAction;
import org.apache.hadoop.io.retry.RetryPolicy.RetryAction.RetryDecision;
import org.apache.hadoop.ipc.RemoteException;
import org.apache.hadoop.ipc.RetriableException;
import org.apache.hadoop.security.AccessControlException;
import org.apache.hadoop.security.token.SecretManager.InvalidToken;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.test.GenericTestUtils;
import org.apache.log4j.Level;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Test WebHDFS */
public class TestWebHDFS {
  static final Log LOG = LogFactory.getLog(TestWebHDFS.class);
  
  static final Random RANDOM = new Random();
  
  static final long systemStartTime = System.nanoTime();

  /** A timer for measuring performance. */
  static class Ticker {
    final String name;
    final long startTime = System.nanoTime();
    private long previousTick = startTime;

    Ticker(final String name, String format, Object... args) {
      this.name = name;
      LOG.info(String.format("\n\n%s START: %s\n",
          name, String.format(format, args)));
    }

    void tick(final long nBytes, String format, Object... args) {
      final long now = System.nanoTime();
      if (now - previousTick > 10000000000L) {
        previousTick = now;
        final double mintues = (now - systemStartTime)/60000000000.0;
        LOG.info(String.format("\n\n%s %.2f min) %s %s\n", name, mintues,
            String.format(format, args), toMpsString(nBytes, now)));
      }
    }
    
    void end(final long nBytes) {
      final long now = System.nanoTime();
      final double seconds = (now - startTime)/1000000000.0;
      LOG.info(String.format("\n\n%s END: duration=%.2fs %s\n",
          name, seconds, toMpsString(nBytes, now)));
    }
    
    String toMpsString(final long nBytes, final long now) {
      final double mb = nBytes/(double)(1<<20);
      final double mps = mb*1000000000.0/(now - startTime);
      return String.format("[nBytes=%.2fMB, speed=%.2fMB/s]", mb, mps);
    }
  }

  @Test(timeout=300000)
  public void testLargeFile() throws Exception {
    largeFileTest(200L << 20); //200MB file length
  }

  /** Test read and write large files. */
  static void largeFileTest(final long fileLength) throws Exception {
    final Configuration conf = WebHdfsTestUtil.createConf();

    final MiniDFSCluster cluster = new MiniDFSCluster.Builder(conf)
        .numDataNodes(3)
        .build();
    try {
      cluster.waitActive();

      final FileSystem fs = WebHdfsTestUtil.getWebHdfsFileSystem(conf, WebHdfsConstants.WEBHDFS_SCHEME);
      final Path dir = new Path("/test/largeFile");
      Assert.assertTrue(fs.mkdirs(dir));

      final byte[] data = new byte[1 << 20];
      RANDOM.nextBytes(data);

      final byte[] expected = new byte[2 * data.length];
      System.arraycopy(data, 0, expected, 0, data.length);
      System.arraycopy(data, 0, expected, data.length, data.length);

      final Path p = new Path(dir, "file");
      final Ticker t = new Ticker("WRITE", "fileLength=" + fileLength);
      final FSDataOutputStream out = fs.create(p);
      try {
        long remaining = fileLength;
        for(; remaining > 0;) {
          t.tick(fileLength - remaining, "remaining=%d", remaining);
          
          final int n = (int)Math.min(remaining, data.length);
          out.write(data, 0, n);
          remaining -= n;
        }
      } finally {
        out.close();
      }
      t.end(fileLength);
  
      Assert.assertEquals(fileLength, fs.getFileStatus(p).getLen());

      final long smallOffset = RANDOM.nextInt(1 << 20) + (1 << 20);
      final long largeOffset = fileLength - smallOffset;
      final byte[] buf = new byte[data.length];

      verifySeek(fs, p, largeOffset, fileLength, buf, expected);
      verifySeek(fs, p, smallOffset, fileLength, buf, expected);
  
      verifyPread(fs, p, largeOffset, fileLength, buf, expected);
    } finally {
      cluster.shutdown();
    }
  }

  static void checkData(long offset, long remaining, int n,
      byte[] actual, byte[] expected) {
    if (RANDOM.nextInt(100) == 0) {
      int j = (int)(offset % actual.length);
      for(int i = 0; i < n; i++) {
        if (expected[j] != actual[i]) {
          Assert.fail("expected[" + j + "]=" + expected[j]
              + " != actual[" + i + "]=" + actual[i]
              + ", offset=" + offset + ", remaining=" + remaining + ", n=" + n);
        }
        j++;
      }
    }
  }

  /** test seek */
  static void verifySeek(FileSystem fs, Path p, long offset, long length,
      byte[] buf, byte[] expected) throws IOException { 
    long remaining = length - offset;
    long checked = 0;
    LOG.info("XXX SEEK: offset=" + offset + ", remaining=" + remaining);

    final Ticker t = new Ticker("SEEK", "offset=%d, remaining=%d",
        offset, remaining);
    final FSDataInputStream in = fs.open(p, 64 << 10);
    in.seek(offset);
    for(; remaining > 0; ) {
      t.tick(checked, "offset=%d, remaining=%d", offset, remaining);
      final int n = (int)Math.min(remaining, buf.length);
      in.readFully(buf, 0, n);
      checkData(offset, remaining, n, buf, expected);

      offset += n;
      remaining -= n;
      checked += n;
    }
    in.close();
    t.end(checked);
  }

  static void verifyPread(FileSystem fs, Path p, long offset, long length,
      byte[] buf, byte[] expected) throws IOException {
    long remaining = length - offset;
    long checked = 0;
    LOG.info("XXX PREAD: offset=" + offset + ", remaining=" + remaining);

    final Ticker t = new Ticker("PREAD", "offset=%d, remaining=%d",
        offset, remaining);
    final FSDataInputStream in = fs.open(p, 64 << 10);
    for(; remaining > 0; ) {
      t.tick(checked, "offset=%d, remaining=%d", offset, remaining);
      final int n = (int)Math.min(remaining, buf.length);
      in.readFully(offset, buf, 0, n);
      checkData(offset, remaining, n, buf, expected);

      offset += n;
      remaining -= n;
      checked += n;
    }
    in.close();
    t.end(checked);
  }

  /** Test client retry with namenode restarting. */
  @Test(timeout=300000)
  public void testNamenodeRestart() throws Exception {
    GenericTestUtils.setLogLevel(NamenodeWebHdfsMethods.LOG, Level.ALL);
    final Configuration conf = WebHdfsTestUtil.createConf();
    TestDFSClientRetries.namenodeRestartTest(conf, true);
  }
  
  @Test(timeout=300000)
  public void testLargeDirectory() throws Exception {
    final Configuration conf = WebHdfsTestUtil.createConf();
    final int listLimit = 2;
    // force small chunking of directory listing
    conf.setInt(DFSConfigKeys.DFS_LIST_LIMIT, listLimit);
    // force paths to be only owner-accessible to ensure ugi isn't changing
    // during listStatus
    FsPermission.setUMask(conf, new FsPermission((short)0077));
    
    final MiniDFSCluster cluster =
        new MiniDFSCluster.Builder(conf).numDataNodes(3).build();
    try {
      cluster.waitActive();
      WebHdfsTestUtil.getWebHdfsFileSystem(conf, WebHdfsConstants.WEBHDFS_SCHEME)
          .setPermission(new Path("/"),
              new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL));

      // trick the NN into not believing it's not the superuser so we can
      // tell if the correct user is used by listStatus
      UserGroupInformation.setLoginUser(
          UserGroupInformation.createUserForTesting(
              "not-superuser", new String[]{"not-supergroup"}));

      UserGroupInformation.createUserForTesting("me", new String[]{"my-group"})
        .doAs(new PrivilegedExceptionAction<Void>() {
          @Override
          public Void run() throws IOException, URISyntaxException {
              FileSystem fs = WebHdfsTestUtil.getWebHdfsFileSystem(conf,
                  WebHdfsConstants.WEBHDFS_SCHEME);
              Path d = new Path("/my-dir");
            Assert.assertTrue(fs.mkdirs(d));
            // Iterator should have no items when dir is empty
            RemoteIterator<FileStatus> it = fs.listStatusIterator(d);
            assertFalse(it.hasNext());
            Path p = new Path(d, "file-"+0);
            Assert.assertTrue(fs.createNewFile(p));
            // Iterator should have an item when dir is not empty
            it = fs.listStatusIterator(d);
            assertTrue(it.hasNext());
            it.next();
            assertFalse(it.hasNext());
            for (int i=1; i < listLimit*3; i++) {
              p = new Path(d, "file-"+i);
              Assert.assertTrue(fs.createNewFile(p));
            }
            // Check the FileStatus[] listing
            FileStatus[] statuses = fs.listStatus(d);
            Assert.assertEquals(listLimit*3, statuses.length);
            // Check the iterator-based listing
            GenericTestUtils.setLogLevel(WebHdfsFileSystem.LOG, Level.TRACE);
            GenericTestUtils.setLogLevel(NamenodeWebHdfsMethods.LOG, Level
                .TRACE);
            it = fs.listStatusIterator(d);
            int count = 0;
            while (it.hasNext()) {
              FileStatus stat = it.next();
              assertEquals("FileStatuses not equal", statuses[count], stat);
              count++;
            }
            assertEquals("Different # of statuses!", statuses.length, count);
            // Do some more basic iterator tests
            it = fs.listStatusIterator(d);
            // Try advancing the iterator without calling hasNext()
            for (int i = 0; i < statuses.length; i++) {
              FileStatus stat = it.next();
              assertEquals("FileStatuses not equal", statuses[i], stat);
            }
            assertFalse("No more items expected", it.hasNext());
            // Try doing next when out of items
            try {
              it.next();
              fail("Iterator should error if out of elements.");
            } catch (IllegalStateException e) {
              // pass
            }
            return null;
          }
        });
    } finally {
      cluster.shutdown();
    }
  }

  @Test(timeout=300000)
  public void testCustomizedUserAndGroupNames() throws Exception {
    final Configuration conf = WebHdfsTestUtil.createConf();
    conf.setBoolean(DFSConfigKeys.DFS_NAMENODE_ACLS_ENABLED_KEY, true);
    // Modify username pattern to allow numeric usernames
    conf.set(HdfsClientConfigKeys.DFS_WEBHDFS_USER_PATTERN_KEY, "^[A-Za-z0-9_][A-Za-z0-9" +
        "._-]*[$]?$");
    // Modify acl pattern to allow numeric and "@" characters user/groups in ACL spec
    conf.set(HdfsClientConfigKeys.DFS_WEBHDFS_ACL_PERMISSION_PATTERN_KEY,
        "^(default:)?(user|group|mask|other):" +
            "[[0-9A-Za-z_][@A-Za-z0-9._-]]*:([rwx-]{3})?(,(default:)?" +
            "(user|group|mask|other):[[0-9A-Za-z_][@A-Za-z0-9._-]]*:([rwx-]{3})?)*$");
    final MiniDFSCluster cluster =
        new MiniDFSCluster.Builder(conf).numDataNodes(1).build();
    try {
      cluster.waitActive();
      WebHdfsTestUtil.getWebHdfsFileSystem(conf, WebHdfsConstants.WEBHDFS_SCHEME)
          .setPermission(new Path("/"),
              new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL));

      // Test a numeric username
      UserGroupInformation.createUserForTesting("123", new String[]{"my-group"})
        .doAs(new PrivilegedExceptionAction<Void>() {
          @Override
          public Void run() throws IOException, URISyntaxException {
            FileSystem fs = WebHdfsTestUtil.getWebHdfsFileSystem(conf,
                WebHdfsConstants.WEBHDFS_SCHEME);
            Path d = new Path("/my-dir");
            Assert.assertTrue(fs.mkdirs(d));
            // Test also specifying a default ACL with a numeric username
            // and another of a groupname with '@'
            fs.modifyAclEntries(d, ImmutableList.of(
                new AclEntry.Builder()
                    .setPermission(FsAction.READ)
                    .setScope(AclEntryScope.DEFAULT)
                    .setType(AclEntryType.USER)
                    .setName("11010")
                    .build(),
                new AclEntry.Builder()
                    .setPermission(FsAction.READ_WRITE)
                    .setType(AclEntryType.GROUP)
                    .setName("foo@bar")
                    .build()
            ));
            return null;
          }
        });
    } finally {
      cluster.shutdown();
    }
  }

  /**
   * Test for catching "no datanode" IOException, when to create a file
   * but datanode is not running for some reason.
   */
  @Test(timeout=300000)
  public void testCreateWithNoDN() throws Exception {
    MiniDFSCluster cluster = null;
    final Configuration conf = WebHdfsTestUtil.createConf();
    try {
      cluster = new MiniDFSCluster.Builder(conf).numDataNodes(0).build();
      conf.setInt(DFSConfigKeys.DFS_REPLICATION_KEY, 1);
      cluster.waitActive();
      FileSystem fs = WebHdfsTestUtil.getWebHdfsFileSystem(conf,
          WebHdfsConstants.WEBHDFS_SCHEME);
      fs.create(new Path("/testnodatanode"));
      Assert.fail("No exception was thrown");
    } catch (IOException ex) {
      GenericTestUtils.assertExceptionContains("Failed to find datanode", ex);
    } finally {
      if (cluster != null) {
        cluster.shutdown();
      }
    }
  }

  /**
   * Test allow and disallow snapshot through WebHdfs. Verifying webhdfs with
   * Distributed filesystem methods.
   */
  @Test
  public void testWebHdfsAllowandDisallowSnapshots() throws Exception {
    MiniDFSCluster cluster = null;
    final Configuration conf = WebHdfsTestUtil.createConf();
    try {
      cluster = new MiniDFSCluster.Builder(conf).numDataNodes(0).build();
      cluster.waitActive();
      final DistributedFileSystem dfs = cluster.getFileSystem();
      final WebHdfsFileSystem webHdfs = WebHdfsTestUtil
          .getWebHdfsFileSystem(conf, WebHdfsConstants.WEBHDFS_SCHEME);

      final Path bar = new Path("/bar");
      dfs.mkdirs(bar);

      // allow snapshots on /bar using webhdfs
      webHdfs.allowSnapshot(bar);
      webHdfs.createSnapshot(bar, "s1");
      final Path s1path = SnapshotTestHelper.getSnapshotRoot(bar, "s1");
      Assert.assertTrue(webHdfs.exists(s1path));
      SnapshottableDirectoryStatus[] snapshottableDirs =
          dfs.getSnapshottableDirListing();
      assertEquals(1, snapshottableDirs.length);
      assertEquals(bar, snapshottableDirs[0].getFullPath());
      dfs.deleteSnapshot(bar, "s1");
      dfs.disallowSnapshot(bar);
      snapshottableDirs = dfs.getSnapshottableDirListing();
      assertNull(snapshottableDirs);

      // disallow snapshots on /bar using webhdfs
      dfs.allowSnapshot(bar);
      snapshottableDirs = dfs.getSnapshottableDirListing();
      assertEquals(1, snapshottableDirs.length);
      assertEquals(bar, snapshottableDirs[0].getFullPath());
      webHdfs.disallowSnapshot(bar);
      snapshottableDirs = dfs.getSnapshottableDirListing();
      assertNull(snapshottableDirs);
      try {
        webHdfs.createSnapshot(bar);
        fail("Cannot create snapshot on a non-snapshottable directory");
      } catch (Exception e) {
        GenericTestUtils.assertExceptionContains(
            "Directory is not a snapshottable directory", e);
      }
    } finally {
      if (cluster != null) {
        cluster.shutdown();
      }
    }
  }

  @Test (timeout = 60000)
  public void testWebHdfsErasureCodingFiles() throws Exception {
    MiniDFSCluster cluster = null;
    final Configuration conf = WebHdfsTestUtil.createConf();
    conf.set(DFSConfigKeys.DFS_NAMENODE_EC_POLICIES_ENABLED_KEY,
        SystemErasureCodingPolicies.getByID(
            SystemErasureCodingPolicies.XOR_2_1_POLICY_ID).getName());
    try {
      cluster = new MiniDFSCluster.Builder(conf).numDataNodes(3).build();
      cluster.waitActive();
      final DistributedFileSystem dfs = cluster.getFileSystem();
      final WebHdfsFileSystem webHdfs = WebHdfsTestUtil
          .getWebHdfsFileSystem(conf, WebHdfsConstants.WEBHDFS_SCHEME);

      final Path ecDir = new Path("/ec");
      dfs.mkdirs(ecDir);
      dfs.setErasureCodingPolicy(ecDir,
          SystemErasureCodingPolicies.getByID(
              SystemErasureCodingPolicies.XOR_2_1_POLICY_ID).getName());
      final Path ecFile = new Path(ecDir, "ec-file.log");
      DFSTestUtil.createFile(dfs, ecFile, 1024 * 10, (short) 1, 0xFEED);

      final Path normalDir = new Path("/dir");
      dfs.mkdirs(normalDir);
      final Path normalFile = new Path(normalDir, "file.log");
      DFSTestUtil.createFile(dfs, normalFile, 1024 * 10, (short) 1, 0xFEED);

      FileStatus expectedECDirStatus = dfs.getFileStatus(ecDir);
      FileStatus actualECDirStatus = webHdfs.getFileStatus(ecDir);
      Assert.assertEquals(expectedECDirStatus.isErasureCoded(),
          actualECDirStatus.isErasureCoded());
      ContractTestUtils.assertErasureCoded(dfs, ecDir);
      assertTrue(ecDir+ " should have erasure coding set in " +
              "FileStatus#toString(): " + actualECDirStatus,
          actualECDirStatus.toString().contains("isErasureCoded=true"));

      FileStatus expectedECFileStatus = dfs.getFileStatus(ecFile);
      FileStatus actualECFileStatus = webHdfs.getFileStatus(ecFile);
      Assert.assertEquals(expectedECFileStatus.isErasureCoded(),
          actualECFileStatus.isErasureCoded());
      ContractTestUtils.assertErasureCoded(dfs, ecFile);
      assertTrue(ecFile+ " should have erasure coding set in " +
              "FileStatus#toString(): " + actualECFileStatus,
          actualECFileStatus.toString().contains("isErasureCoded=true"));

      FileStatus expectedNormalDirStatus = dfs.getFileStatus(normalDir);
      FileStatus actualNormalDirStatus = webHdfs.getFileStatus(normalDir);
      Assert.assertEquals(expectedNormalDirStatus.isErasureCoded(),
          actualNormalDirStatus.isErasureCoded());
      ContractTestUtils.assertNotErasureCoded(dfs, normalDir);
      assertTrue(normalDir + " should have erasure coding unset in " +
              "FileStatus#toString(): " + actualNormalDirStatus,
          actualNormalDirStatus.toString().contains("isErasureCoded=false"));

      FileStatus expectedNormalFileStatus = dfs.getFileStatus(normalFile);
      FileStatus actualNormalFileStatus = webHdfs.getFileStatus(normalDir);
      Assert.assertEquals(expectedNormalFileStatus.isErasureCoded(),
          actualNormalFileStatus.isErasureCoded());
      ContractTestUtils.assertNotErasureCoded(dfs, normalFile);
      assertTrue(normalFile + " should have erasure coding unset in " +
              "FileStatus#toString(): " + actualNormalFileStatus,
          actualNormalFileStatus.toString().contains("isErasureCoded=false"));

    } finally {
      if (cluster != null) {
        cluster.shutdown();
      }
    }
  }

  /**
   * Test snapshot creation through WebHdfs
   */
  @Test
  public void testWebHdfsCreateSnapshot() throws Exception {
    MiniDFSCluster cluster = null;
    final Configuration conf = WebHdfsTestUtil.createConf();
    try {
      cluster = new MiniDFSCluster.Builder(conf).numDataNodes(0).build();
      cluster.waitActive();
      final DistributedFileSystem dfs = cluster.getFileSystem();
      final FileSystem webHdfs = WebHdfsTestUtil.getWebHdfsFileSystem(conf,
          WebHdfsConstants.WEBHDFS_SCHEME);

      final Path foo = new Path("/foo");
      dfs.mkdirs(foo);

      try {
        webHdfs.createSnapshot(foo);
        fail("Cannot create snapshot on a non-snapshottable directory");
      } catch (Exception e) {
        GenericTestUtils.assertExceptionContains(
            "Directory is not a snapshottable directory", e);
      }

      // allow snapshots on /foo
      dfs.allowSnapshot(foo);
      // create snapshots on foo using WebHdfs
      webHdfs.createSnapshot(foo, "s1");
      // create snapshot without specifying name
      final Path spath = webHdfs.createSnapshot(foo, null);

      Assert.assertTrue(webHdfs.exists(spath));
      final Path s1path = SnapshotTestHelper.getSnapshotRoot(foo, "s1");
      Assert.assertTrue(webHdfs.exists(s1path));
    } finally {
      if (cluster != null) {
        cluster.shutdown();
      }
    }
  }

  /**
   * Test snapshot deletion through WebHdfs
   */
  @Test
  public void testWebHdfsDeleteSnapshot() throws Exception {
    MiniDFSCluster cluster = null;
    final Configuration conf = WebHdfsTestUtil.createConf();
    try {
      cluster = new MiniDFSCluster.Builder(conf).numDataNodes(0).build();
      cluster.waitActive();
      final DistributedFileSystem dfs = cluster.getFileSystem();
      final FileSystem webHdfs = WebHdfsTestUtil.getWebHdfsFileSystem(conf,
          WebHdfsConstants.WEBHDFS_SCHEME);

      final Path foo = new Path("/foo");
      dfs.mkdirs(foo);
      dfs.allowSnapshot(foo);

      webHdfs.createSnapshot(foo, "s1");
      final Path spath = webHdfs.createSnapshot(foo, null);
      Assert.assertTrue(webHdfs.exists(spath));
      final Path s1path = SnapshotTestHelper.getSnapshotRoot(foo, "s1");
      Assert.assertTrue(webHdfs.exists(s1path));

      // delete operation snapshot name as null
      try {
        webHdfs.deleteSnapshot(foo, null);
        fail("Expected IllegalArgumentException");
      } catch (RemoteException e) {
        Assert.assertEquals("Required param snapshotname for "
            + "op: DELETESNAPSHOT is null or empty", e.getLocalizedMessage());
      }

      // delete the two snapshots
      webHdfs.deleteSnapshot(foo, "s1");
      assertFalse(webHdfs.exists(s1path));
      webHdfs.deleteSnapshot(foo, spath.getName());
      assertFalse(webHdfs.exists(spath));
    } finally {
      if (cluster != null) {
        cluster.shutdown();
      }
    }
  }

  @Test
  public void testWebHdfsCreateNonRecursive() throws IOException, URISyntaxException {
    MiniDFSCluster cluster = null;
    final Configuration conf = WebHdfsTestUtil.createConf();
    WebHdfsFileSystem webHdfs = null;

    try {
      cluster = new MiniDFSCluster.Builder(conf).build();
      cluster.waitActive();

      webHdfs = WebHdfsTestUtil.getWebHdfsFileSystem(conf, WebHdfsConstants.WEBHDFS_SCHEME);

      TestFileCreation.testFileCreationNonRecursive(webHdfs);

    } finally {
      if(webHdfs != null) {
       webHdfs.close();
      }

      if(cluster != null) {
        cluster.shutdown();
      }
    }
  }
  /**
   * Test snapshot rename through WebHdfs
   */
  @Test
  public void testWebHdfsRenameSnapshot() throws Exception {
    MiniDFSCluster cluster = null;
    final Configuration conf = WebHdfsTestUtil.createConf();
    try {
      cluster = new MiniDFSCluster.Builder(conf).numDataNodes(0).build();
      cluster.waitActive();
      final DistributedFileSystem dfs = cluster.getFileSystem();
      final FileSystem webHdfs = WebHdfsTestUtil.getWebHdfsFileSystem(conf,
          WebHdfsConstants.WEBHDFS_SCHEME);

      final Path foo = new Path("/foo");
      dfs.mkdirs(foo);
      dfs.allowSnapshot(foo);

      webHdfs.createSnapshot(foo, "s1");
      final Path s1path = SnapshotTestHelper.getSnapshotRoot(foo, "s1");
      Assert.assertTrue(webHdfs.exists(s1path));

      // rename s1 to s2 with oldsnapshotName as null
      try {
        webHdfs.renameSnapshot(foo, null, "s2");
        fail("Expected IllegalArgumentException");
      } catch (RemoteException e) {
        Assert.assertEquals("Required param oldsnapshotname for "
            + "op: RENAMESNAPSHOT is null or empty", e.getLocalizedMessage());
      }

      // rename s1 to s2
      webHdfs.renameSnapshot(foo, "s1", "s2");
      assertFalse(webHdfs.exists(s1path));
      final Path s2path = SnapshotTestHelper.getSnapshotRoot(foo, "s2");
      Assert.assertTrue(webHdfs.exists(s2path));

      webHdfs.deleteSnapshot(foo, "s2");
      assertFalse(webHdfs.exists(s2path));
    } finally {
      if (cluster != null) {
        cluster.shutdown();
      }
    }
  }

  /**
   * Make sure a RetriableException is thrown when rpcServer is null in
   * NamenodeWebHdfsMethods.
   */
  @Test
  public void testRaceWhileNNStartup() throws Exception {
    MiniDFSCluster cluster = null;
    final Configuration conf = WebHdfsTestUtil.createConf();
    try {
      cluster = new MiniDFSCluster.Builder(conf).numDataNodes(0).build();
      cluster.waitActive();
      final NameNode namenode = cluster.getNameNode();
      final NamenodeProtocols rpcServer = namenode.getRpcServer();
      Whitebox.setInternalState(namenode, "rpcServer", null);

      final Path foo = new Path("/foo");
      final FileSystem webHdfs = WebHdfsTestUtil.getWebHdfsFileSystem(conf,
          WebHdfsConstants.WEBHDFS_SCHEME);
      try {
        webHdfs.mkdirs(foo);
        fail("Expected RetriableException");
      } catch (RetriableException e) {
        GenericTestUtils.assertExceptionContains("Namenode is in startup mode",
            e);
      }
      Whitebox.setInternalState(namenode, "rpcServer", rpcServer);
    } finally {
      if (cluster != null) {
        cluster.shutdown();
      }
    }
  }

  @Test
  public void testDTInInsecureClusterWithFallback()
      throws IOException, URISyntaxException {
    MiniDFSCluster cluster = null;
    final Configuration conf = WebHdfsTestUtil.createConf();
    conf.setBoolean(CommonConfigurationKeys
        .IPC_CLIENT_FALLBACK_TO_SIMPLE_AUTH_ALLOWED_KEY, true);
    try {
      cluster = new MiniDFSCluster.Builder(conf).numDataNodes(0).build();
      final FileSystem webHdfs = WebHdfsTestUtil.getWebHdfsFileSystem(conf,
            WebHdfsConstants.WEBHDFS_SCHEME);
      Assert.assertNull(webHdfs.getDelegationToken(null));
    } finally {
      if (cluster != null) {
        cluster.shutdown();
      }
    }
  }

  @Test
  public void testDTInInsecureCluster() throws Exception {
    MiniDFSCluster cluster = null;
    final Configuration conf = WebHdfsTestUtil.createConf();
    try {
      cluster = new MiniDFSCluster.Builder(conf).numDataNodes(0).build();
      final FileSystem webHdfs = WebHdfsTestUtil.getWebHdfsFileSystem(conf,
          WebHdfsConstants.WEBHDFS_SCHEME);
      webHdfs.getDelegationToken(null);
      fail("No exception is thrown.");
    } catch (AccessControlException ace) {
      Assert.assertTrue(ace.getMessage().startsWith(
          WebHdfsFileSystem.CANT_FALLBACK_TO_INSECURE_MSG));
    } finally {
      if (cluster != null) {
        cluster.shutdown();
      }
    }
  }

  @Test
  public void testWebHdfsOffsetAndLength() throws Exception{
    MiniDFSCluster cluster = null;
    final Configuration conf = WebHdfsTestUtil.createConf();
    final int OFFSET = 42;
    final int LENGTH = 512;
    final String PATH = "/foo";
    byte[] CONTENTS = new byte[1024];
    RANDOM.nextBytes(CONTENTS);
    try {
      cluster = new MiniDFSCluster.Builder(conf).numDataNodes(1).build();
      final WebHdfsFileSystem fs =
          WebHdfsTestUtil.getWebHdfsFileSystem(conf, WebHdfsConstants.WEBHDFS_SCHEME);
      try (OutputStream os = fs.create(new Path(PATH))) {
        os.write(CONTENTS);
      }
      InetSocketAddress addr = cluster.getNameNode().getHttpAddress();
      URL url = new URL("http", addr.getHostString(), addr
          .getPort(), WebHdfsFileSystem.PATH_PREFIX + PATH + "?op=OPEN" +
          Param.toSortedString("&", new OffsetParam((long) OFFSET),
                               new LengthParam((long) LENGTH))
      );
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setInstanceFollowRedirects(true);
      Assert.assertEquals(LENGTH, conn.getContentLength());
      byte[] subContents = new byte[LENGTH];
      byte[] realContents = new byte[LENGTH];
      System.arraycopy(CONTENTS, OFFSET, subContents, 0, LENGTH);
      IOUtils.readFully(conn.getInputStream(), realContents);
      Assert.assertArrayEquals(subContents, realContents);
    } finally {
      if (cluster != null) {
        cluster.shutdown();
      }
    }
  }

  @Test
  public void testContentSummary() throws Exception {
    MiniDFSCluster cluster = null;
    final Configuration conf = WebHdfsTestUtil.createConf();
    final Path path = new Path("/QuotaDir");
    try {
      cluster = new MiniDFSCluster.Builder(conf).numDataNodes(0).build();
      final WebHdfsFileSystem webHdfs = WebHdfsTestUtil.getWebHdfsFileSystem(
          conf, WebHdfsConstants.WEBHDFS_SCHEME);
      final DistributedFileSystem dfs = cluster.getFileSystem();
      dfs.mkdirs(path);
      dfs.setQuotaByStorageType(path, StorageType.DISK, 100000);
      ContentSummary contentSummary = webHdfs.getContentSummary(path);
      Assert.assertTrue((contentSummary.getTypeQuota(
          StorageType.DISK) == 100000));
    } finally {
      if (cluster != null) {
        cluster.shutdown();
      }
    }
  }

  @Test
  public void testWebHdfsPread() throws Exception {
    final Configuration conf = WebHdfsTestUtil.createConf();
    MiniDFSCluster cluster = new MiniDFSCluster.Builder(conf).numDataNodes(1)
        .build();
    byte[] content = new byte[1024];
    RANDOM.nextBytes(content);
    final Path foo = new Path("/foo");
    FSDataInputStream in = null;
    try {
      final WebHdfsFileSystem fs = WebHdfsTestUtil.getWebHdfsFileSystem(conf,
          WebHdfsConstants.WEBHDFS_SCHEME);
      try (OutputStream os = fs.create(foo)) {
        os.write(content);
      }

      // pread
      in = fs.open(foo, 1024);
      byte[] buf = new byte[1024];
      try {
        in.readFully(1020, buf, 0, 5);
        Assert.fail("EOF expected");
      } catch (EOFException ignored) {}

      // mix pread with stateful read
      int length = in.read(buf, 0, 512);
      in.readFully(100, new byte[1024], 0, 100);
      int preadLen = in.read(200, new byte[1024], 0, 200);
      Assert.assertTrue(preadLen > 0);
      IOUtils.readFully(in, buf, length, 1024 - length);
      Assert.assertArrayEquals(content, buf);
    } finally {
      if (in != null) {
        in.close();
      }
      cluster.shutdown();
    }
  }

  @Test(timeout = 30000)
  public void testGetHomeDirectory() throws Exception {

    MiniDFSCluster cluster = null;
    try {
      Configuration conf = new Configuration();
      cluster = new MiniDFSCluster.Builder(conf).build();
      cluster.waitActive();
      DistributedFileSystem hdfs = cluster.getFileSystem();

      final URI uri = new URI(WebHdfsConstants.WEBHDFS_SCHEME + "://"
          + cluster.getHttpUri(0).replace("http://", ""));
      final Configuration confTemp = new Configuration();

      {
        WebHdfsFileSystem webhdfs = (WebHdfsFileSystem) FileSystem.get(uri,
            confTemp);

        assertEquals(hdfs.getHomeDirectory().toUri().getPath(), webhdfs
            .getHomeDirectory().toUri().getPath());

        webhdfs.close();
      }

      {
        WebHdfsFileSystem webhdfs = createWebHDFSAsTestUser(confTemp, uri,
            "XXX");

        assertNotEquals(hdfs.getHomeDirectory().toUri().getPath(), webhdfs
            .getHomeDirectory().toUri().getPath());

        webhdfs.close();
      }

    } finally {
      if (cluster != null)
        cluster.shutdown();
    }
  }

  @Test
  public void testWebHdfsGetBlockLocationsWithStorageType() throws Exception{
    MiniDFSCluster cluster = null;
    final Configuration conf = WebHdfsTestUtil.createConf();
    final int OFFSET = 42;
    final int LENGTH = 512;
    final Path PATH = new Path("/foo");
    byte[] CONTENTS = new byte[1024];
    RANDOM.nextBytes(CONTENTS);
    try {
      cluster = new MiniDFSCluster.Builder(conf).numDataNodes(1).build();
      final WebHdfsFileSystem fs = WebHdfsTestUtil.getWebHdfsFileSystem(conf,
          WebHdfsConstants.WEBHDFS_SCHEME);
      try (OutputStream os = fs.create(PATH)) {
        os.write(CONTENTS);
      }
      BlockLocation[] locations = fs.getFileBlockLocations(PATH, OFFSET,
          LENGTH);
      for (BlockLocation location: locations) {
        StorageType[] storageTypes = location.getStorageTypes();
        Assert.assertTrue(storageTypes != null && storageTypes.length > 0 &&
            storageTypes[0] == StorageType.DISK);
      }

      // Query webhdfs REST API to get block locations
      InetSocketAddress addr = cluster.getNameNode().getHttpAddress();

      // Case 1
      // URL without length or offset parameters
      URL url1 = new URL("http", addr.getHostString(), addr.getPort(),
          WebHdfsFileSystem.PATH_PREFIX + "/foo?op=GETFILEBLOCKLOCATIONS");
      LOG.info("Sending GETFILEBLOCKLOCATIONS request " + url1);

      String response1 = getResponse(url1, "GET");
      LOG.info("The output of GETFILEBLOCKLOCATIONS request " + response1);
      // Parse BlockLocation array from json output using object mapper
      BlockLocation[] locationArray1 = toBlockLocationArray(response1);

      // Verify the result from rest call is same as file system api
      verifyEquals(locations, locationArray1);

      // Case 2
      // URL contains length and offset parameters
      URL url2 = new URL("http", addr.getHostString(), addr.getPort(),
          WebHdfsFileSystem.PATH_PREFIX + "/foo?op=GETFILEBLOCKLOCATIONS"
              + "&length=" + LENGTH + "&offset=" + OFFSET);
      LOG.info("Sending GETFILEBLOCKLOCATIONS request " + url2);

      String response2 = getResponse(url2, "GET");
      LOG.info("The output of GETFILEBLOCKLOCATIONS request " + response2);
      BlockLocation[] locationArray2 = toBlockLocationArray(response2);

      verifyEquals(locations, locationArray2);

      // Case 3
      // URL contains length parameter but without offset parameters
      URL url3 = new URL("http", addr.getHostString(), addr.getPort(),
          WebHdfsFileSystem.PATH_PREFIX + "/foo?op=GETFILEBLOCKLOCATIONS"
              + "&length=" + LENGTH);
      LOG.info("Sending GETFILEBLOCKLOCATIONS request " + url3);

      String response3 = getResponse(url3, "GET");
      LOG.info("The output of GETFILEBLOCKLOCATIONS request " + response3);
      BlockLocation[] locationArray3 = toBlockLocationArray(response3);

      verifyEquals(locations, locationArray3);

      // Case 4
      // URL contains offset parameter but without length parameter
      URL url4 = new URL("http", addr.getHostString(), addr.getPort(),
          WebHdfsFileSystem.PATH_PREFIX + "/foo?op=GETFILEBLOCKLOCATIONS"
              + "&offset=" + OFFSET);
      LOG.info("Sending GETFILEBLOCKLOCATIONS request " + url4);

      String response4 = getResponse(url4, "GET");
      LOG.info("The output of GETFILEBLOCKLOCATIONS request " + response4);
      BlockLocation[] locationArray4 = toBlockLocationArray(response4);

      verifyEquals(locations, locationArray4);

      // Case 5
      // URL specifies offset exceeds the file length
      URL url5 = new URL("http", addr.getHostString(), addr.getPort(),
          WebHdfsFileSystem.PATH_PREFIX + "/foo?op=GETFILEBLOCKLOCATIONS"
              + "&offset=1200");
      LOG.info("Sending GETFILEBLOCKLOCATIONS request " + url5);

      String response5 = getResponse(url5, "GET");
      LOG.info("The output of GETFILEBLOCKLOCATIONS request " + response5);
      BlockLocation[] locationArray5 = toBlockLocationArray(response5);

      // Expected an empty array of BlockLocation
      verifyEquals(new BlockLocation[] {}, locationArray5);
    } finally {
      if (cluster != null) {
        cluster.shutdown();
      }
    }
  }

  private BlockLocation[] toBlockLocationArray(String json)
      throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    MapType subType = mapper.getTypeFactory().constructMapType(
        Map.class,
        String.class,
        BlockLocation[].class);
    MapType rootType = mapper.getTypeFactory().constructMapType(
        Map.class,
        mapper.constructType(String.class),
        mapper.constructType(subType));

    Map<String, Map<String, BlockLocation[]>> jsonMap = mapper
        .readValue(json, rootType);
    Map<String, BlockLocation[]> locationMap = jsonMap
        .get("BlockLocations");
    BlockLocation[] locationArray = locationMap.get(
        BlockLocation.class.getSimpleName());
    return locationArray;
  }

  private void verifyEquals(BlockLocation[] locations1,
      BlockLocation[] locations2) throws IOException {
    for(int i=0; i<locations1.length; i++) {
      BlockLocation location1 = locations1[i];
      BlockLocation location2 = locations2[i];
      Assert.assertEquals(location1.getLength(),
          location2.getLength());
      Assert.assertEquals(location1.getOffset(),
          location2.getOffset());
      Assert.assertArrayEquals(location1.getCachedHosts(),
          location2.getCachedHosts());
      Assert.assertArrayEquals(location1.getHosts(),
          location2.getHosts());
      Assert.assertArrayEquals(location1.getNames(),
          location2.getNames());
      Assert.assertArrayEquals(location1.getStorageIds(),
          location2.getStorageIds());
      Assert.assertArrayEquals(location1.getTopologyPaths(),
          location2.getTopologyPaths());
      Assert.assertArrayEquals(location1.getStorageTypes(),
          location2.getStorageTypes());
    }
  }

  private static String getResponse(URL url, String httpRequestType)
      throws IOException {
    HttpURLConnection conn = null;
    try {
      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod(httpRequestType);
      conn.setInstanceFollowRedirects(false);
      return IOUtils.toString(conn.getInputStream());
    } finally {
      if(conn != null) {
        conn.disconnect();
      }
    }
  }

  private WebHdfsFileSystem createWebHDFSAsTestUser(final Configuration conf,
      final URI uri, final String userName) throws Exception {

    final UserGroupInformation ugi = UserGroupInformation.createUserForTesting(
        userName, new String[] { "supergroup" });

    return ugi.doAs(new PrivilegedExceptionAction<WebHdfsFileSystem>() {
      @Override
      public WebHdfsFileSystem run() throws IOException {
        WebHdfsFileSystem webhdfs = (WebHdfsFileSystem) FileSystem.get(uri,
            conf);
        return webhdfs;
      }
    });
  }

  @Test(timeout=90000)
  public void testWebHdfsReadRetries() throws Exception {
    // ((Log4JLogger)DFSClient.LOG).getLogger().setLevel(Level.ALL);
    final Configuration conf = WebHdfsTestUtil.createConf();
    final Path dir = new Path("/testWebHdfsReadRetries");

    conf.setBoolean(HdfsClientConfigKeys.Retry.POLICY_ENABLED_KEY, true);
    conf.setInt(DFSConfigKeys.DFS_NAMENODE_SAFEMODE_MIN_DATANODES_KEY, 1);
    conf.setInt(DFSConfigKeys.DFS_BLOCK_SIZE_KEY, 1024*512);
    conf.setInt(DFSConfigKeys.DFS_REPLICATION_KEY, 1);

    final short numDatanodes = 1;
    final MiniDFSCluster cluster = new MiniDFSCluster.Builder(conf)
        .numDataNodes(numDatanodes)
        .build();
    try {
      cluster.waitActive();
      final FileSystem fs = WebHdfsTestUtil
                .getWebHdfsFileSystem(conf, WebHdfsConstants.WEBHDFS_SCHEME);

      //create a file
      final long length = 1L << 20;
      final Path file1 = new Path(dir, "testFile");

      DFSTestUtil.createFile(fs, file1, length, numDatanodes, 20120406L);

      //get file status and check that it was written properly.
      final FileStatus s1 = fs.getFileStatus(file1);
      assertEquals("Write failed for file " + file1, length, s1.getLen());

      // Ensure file can be read through WebHdfsInputStream
      FSDataInputStream in = fs.open(file1);
      assertTrue("Input stream is not an instance of class WebHdfsInputStream",
          in.getWrappedStream() instanceof WebHdfsInputStream);
      int count = 0;
      for(; in.read() != -1; count++);
      assertEquals("Read failed for file " + file1, s1.getLen(), count);
      assertEquals("Sghould not be able to read beyond end of file",
          in.read(), -1);
      in.close();
      try {
        in.read();
        fail("Read after close should have failed");
      } catch(IOException ioe) { }

      WebHdfsFileSystem wfs = (WebHdfsFileSystem)fs;
      // Read should not be retried if AccessControlException is encountered.
      String msg = "ReadRetries: Test Access Control Exception";
      testReadRetryExceptionHelper(wfs, file1,
                          new AccessControlException(msg), msg, false, 1);

      // Retry policy should be invoked if IOExceptions are thrown.
      msg = "ReadRetries: Test SocketTimeoutException";
      testReadRetryExceptionHelper(wfs, file1,
                          new SocketTimeoutException(msg), msg, true, 5);
      msg = "ReadRetries: Test SocketException";
      testReadRetryExceptionHelper(wfs, file1,
                          new SocketException(msg), msg, true, 5);
      msg = "ReadRetries: Test EOFException";
      testReadRetryExceptionHelper(wfs, file1,
                          new EOFException(msg), msg, true, 5);
      msg = "ReadRetries: Test Generic IO Exception";
      testReadRetryExceptionHelper(wfs, file1,
                          new IOException(msg), msg, true, 5);

      // If InvalidToken exception occurs, WebHdfs only retries if the
      // delegation token was replaced. Do that twice, then verify by checking
      // the number of times it tried.
      WebHdfsFileSystem spyfs = spy(wfs);
      when(spyfs.replaceExpiredDelegationToken()).thenReturn(true, true, false);
      msg = "ReadRetries: Test Invalid Token Exception";
      testReadRetryExceptionHelper(spyfs, file1,
                          new InvalidToken(msg), msg, false, 3);
    } finally {
      cluster.shutdown();
    }
  }

  public boolean attemptedRetry;
  private void testReadRetryExceptionHelper(WebHdfsFileSystem fs, Path fn,
      final IOException ex, String msg, boolean shouldAttemptRetry,
      int numTimesTried)
      throws Exception {
    // Ovverride WebHdfsInputStream#getInputStream so that it returns
    // an input stream that throws the specified exception when read
    // is called.
    FSDataInputStream in = fs.open(fn);
    in.read(); // Connection is made only when the first read() occurs.
    final WebHdfsInputStream webIn =
        (WebHdfsInputStream)(in.getWrappedStream());

    final InputStream spyInputStream =
        spy(webIn.getReadRunner().getInputStream());
    doThrow(ex).when(spyInputStream).read((byte[])any(), anyInt(), anyInt());
    final WebHdfsFileSystem.ReadRunner rr = spy(webIn.getReadRunner());
    doReturn(spyInputStream)
        .when(rr).initializeInputStream((HttpURLConnection) any());
    rr.setInputStream(spyInputStream);
    webIn.setReadRunner(rr);

    // Override filesystem's retry policy in order to verify that
    // WebHdfsInputStream is calling shouldRetry for the appropriate
    // exceptions.
    final RetryAction retryAction = new RetryAction(RetryDecision.RETRY);
    final RetryAction failAction = new RetryAction(RetryDecision.FAIL);
    RetryPolicy rp = new RetryPolicy() {
      @Override
      public RetryAction shouldRetry(Exception e, int retries, int failovers,
          boolean isIdempotentOrAtMostOnce) throws Exception {
        attemptedRetry = true;
       if (retries > 3) {
          return failAction;
        } else {
          return retryAction;
        }
      }
    };
    fs.setRetryPolicy(rp);

    // If the retry logic is exercised, attemptedRetry will be true. Some
    // exceptions should exercise the retry logic and others should not.
    // Either way, the value of attemptedRetry should match shouldAttemptRetry.
    attemptedRetry = false;
    try {
      webIn.read();
      fail(msg + ": Read should have thrown exception.");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains(msg));
    }
    assertEquals(msg + ": Read should " + (shouldAttemptRetry ? "" : "not ")
                + "have called shouldRetry. ",
        attemptedRetry, shouldAttemptRetry);

    verify(rr, times(numTimesTried)).getResponse((HttpURLConnection) any());
    webIn.close();
    in.close();
  }

  private void checkResponseContainsLocation(URL url, String TYPE)
    throws JSONException, IOException {
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod(TYPE);
    conn.setInstanceFollowRedirects(false);
    String response = IOUtils.toString(conn.getInputStream());
    LOG.info("Response was : " + response);
    Assert.assertEquals(
      "Response wasn't " + HttpURLConnection.HTTP_OK,
      HttpURLConnection.HTTP_OK, conn.getResponseCode());

    JSONObject responseJson = new JSONObject(response);
    Assert.assertTrue("Response didn't give us a location. " + response,
      responseJson.has("Location"));

    //Test that the DN allows CORS on Create
    if(TYPE.equals("CREATE")) {
      URL dnLocation = new URL(responseJson.getString("Location"));
      HttpURLConnection dnConn = (HttpURLConnection) dnLocation.openConnection();
      dnConn.setRequestMethod("OPTIONS");
      Assert.assertEquals("Datanode url : " + dnLocation + " didn't allow "
        + "CORS", HttpURLConnection.HTTP_OK, dnConn.getResponseCode());
    }
  }

  @Test
  /**
   * Test that when "&noredirect=true" is added to operations CREATE, APPEND,
   * OPEN, and GETFILECHECKSUM the response (which is usually a 307 temporary
   * redirect) is a 200 with JSON that contains the redirected location
   */
  public void testWebHdfsNoRedirect() throws Exception {
    MiniDFSCluster cluster = null;
    final Configuration conf = WebHdfsTestUtil.createConf();
    try {
      cluster = new MiniDFSCluster.Builder(conf).numDataNodes(3).build();
      LOG.info("Started cluster");
      InetSocketAddress addr = cluster.getNameNode().getHttpAddress();

      URL url = new URL("http", addr.getHostString(), addr.getPort(),
        WebHdfsFileSystem.PATH_PREFIX + "/testWebHdfsNoRedirectCreate" +
        "?op=CREATE" + Param.toSortedString("&", new NoRedirectParam(true)));
      LOG.info("Sending create request " + url);
      checkResponseContainsLocation(url, "PUT");

      //Write a file that we can read
      final WebHdfsFileSystem fs = WebHdfsTestUtil.getWebHdfsFileSystem(
        conf, WebHdfsConstants.WEBHDFS_SCHEME);
      final String PATH = "/testWebHdfsNoRedirect";
      byte[] CONTENTS = new byte[1024];
      RANDOM.nextBytes(CONTENTS);
      try (OutputStream os = fs.create(new Path(PATH))) {
        os.write(CONTENTS);
      }
      url = new URL("http", addr.getHostString(), addr.getPort(),
        WebHdfsFileSystem.PATH_PREFIX + "/testWebHdfsNoRedirect" +
        "?op=OPEN" + Param.toSortedString("&", new NoRedirectParam(true)));
      LOG.info("Sending open request " + url);
      checkResponseContainsLocation(url, "GET");

      url = new URL("http", addr.getHostString(), addr.getPort(),
        WebHdfsFileSystem.PATH_PREFIX + "/testWebHdfsNoRedirect" +
        "?op=GETFILECHECKSUM" + Param.toSortedString(
        "&", new NoRedirectParam(true)));
      LOG.info("Sending getfilechecksum request " + url);
      checkResponseContainsLocation(url, "GET");

      url = new URL("http", addr.getHostString(), addr.getPort(),
        WebHdfsFileSystem.PATH_PREFIX + "/testWebHdfsNoRedirect" +
        "?op=APPEND" + Param.toSortedString("&", new NoRedirectParam(true)));
      LOG.info("Sending append request " + url);
      checkResponseContainsLocation(url, "POST");
    } finally {
      if (cluster != null) {
        cluster.shutdown();
      }
    }
  }

  @Test
  public void testGetTrashRoot() throws Exception {
    MiniDFSCluster cluster = null;
    final Configuration conf = WebHdfsTestUtil.createConf();
    final String currentUser =
        UserGroupInformation.getCurrentUser().getShortUserName();
    try {
      cluster = new MiniDFSCluster.Builder(conf).numDataNodes(0).build();
      final WebHdfsFileSystem webFS = WebHdfsTestUtil.getWebHdfsFileSystem(
          conf, WebHdfsConstants.WEBHDFS_SCHEME);

      Path trashPath = webFS.getTrashRoot(new Path("/"));
      Path expectedPath = new Path(FileSystem.USER_HOME_PREFIX,
          new Path(currentUser, FileSystem.TRASH_PREFIX));
      assertEquals(expectedPath.toUri().getPath(), trashPath.toUri().getPath());
    } finally {
      if (cluster != null) {
        cluster.shutdown();
      }
    }
  }


  @Test
  public void testStoragePolicy() throws Exception {
    MiniDFSCluster cluster = null;
    final Configuration conf = WebHdfsTestUtil.createConf();
    final Path path = new Path("/file");
    try {
      cluster = new MiniDFSCluster.Builder(conf).numDataNodes(0).build();
      final DistributedFileSystem dfs = cluster.getFileSystem();
      final WebHdfsFileSystem webHdfs = WebHdfsTestUtil.getWebHdfsFileSystem(
          conf, WebHdfsConstants.WEBHDFS_SCHEME);

      // test getAllStoragePolicies
      BlockStoragePolicy[] dfsPolicies = (BlockStoragePolicy[]) dfs
          .getAllStoragePolicies().toArray();
      BlockStoragePolicy[] webHdfsPolicies = (BlockStoragePolicy[]) webHdfs
          .getAllStoragePolicies().toArray();
      Assert.assertTrue(Arrays.equals(dfsPolicies, webHdfsPolicies));

      // test get/set/unset policies
      DFSTestUtil.createFile(dfs, path, 0, (short) 1, 0L);
      // get defaultPolicy
      BlockStoragePolicySpi defaultdfsPolicy = dfs.getStoragePolicy(path);
      // set policy through webhdfs
      webHdfs.setStoragePolicy(path, HdfsConstants.COLD_STORAGE_POLICY_NAME);
      // get policy from dfs
      BlockStoragePolicySpi dfsPolicy = dfs.getStoragePolicy(path);
      // get policy from webhdfs
      BlockStoragePolicySpi webHdfsPolicy = webHdfs.getStoragePolicy(path);
      Assert.assertEquals(HdfsConstants.COLD_STORAGE_POLICY_NAME.toString(),
          webHdfsPolicy.getName());
      Assert.assertEquals(webHdfsPolicy, dfsPolicy);
      // unset policy
      webHdfs.unsetStoragePolicy(path);
      Assert.assertEquals(defaultdfsPolicy, webHdfs.getStoragePolicy(path));
    } finally {
      if (cluster != null) {
        cluster.shutdown();
      }
    }
  }

  @Test
  public void testSetStoragePolicyWhenPolicyDisabled() throws Exception {
    Configuration conf = new HdfsConfiguration();
    conf.setBoolean(DFSConfigKeys.DFS_STORAGE_POLICY_ENABLED_KEY, false);
    MiniDFSCluster cluster = new MiniDFSCluster.Builder(conf).numDataNodes(0)
        .build();
    try {
      cluster.waitActive();
      final WebHdfsFileSystem webHdfs = WebHdfsTestUtil.getWebHdfsFileSystem(
          conf, WebHdfsConstants.WEBHDFS_SCHEME);
      webHdfs.setStoragePolicy(new Path("/"),
          HdfsConstants.COLD_STORAGE_POLICY_NAME);
      fail("Should throw exception, when storage policy disabled");
    } catch (IOException e) {
      Assert.assertTrue(e.getMessage().contains(
          "Failed to set storage policy since"));
    } finally {
      cluster.shutdown();
    }
  }

  @Test
  public void testWebHdfsAppend() throws Exception {
    MiniDFSCluster cluster = null;
    final Configuration conf = WebHdfsTestUtil.createConf();
    final int dnNumber = 3;
    try {

      cluster = new MiniDFSCluster.Builder(conf).numDataNodes(dnNumber).build();

      final WebHdfsFileSystem webFS = WebHdfsTestUtil.getWebHdfsFileSystem(
          conf, WebHdfsConstants.WEBHDFS_SCHEME);

      final DistributedFileSystem fs = cluster.getFileSystem();

      final Path appendFile = new Path("/testAppend.txt");
      final String content = "hello world";
      DFSTestUtil.writeFile(fs, appendFile, content);

      for (int index = 0; index < dnNumber - 1; index++){
        cluster.shutdownDataNode(index);
      }
      cluster.restartNameNodes();
      cluster.waitActive();

      try {
        DFSTestUtil.appendFile(webFS, appendFile, content);
        fail("Should fail to append file since "
            + "datanode number is 1 and replication is 3");
      } catch (IOException ignored) {
        String resultContent = DFSTestUtil.readFile(fs, appendFile);
        assertTrue(resultContent.equals(content));
      }
    } finally {
      if (cluster != null) {
        cluster.shutdown(true);
      }
    }
  }

  /**
   * A mock class to handle the {@link WebHdfsFileSystem} client
   * request. The format of the response depends on how many of
   * times it gets called (1 to 3 times).
   * <p>
   * First time call it return a wrapped json response with a
   * IllegalArgumentException
   * <p>
   * Second time call it return a valid GET_BLOCK_LOCATIONS
   * json response
   * <p>
   * Third time call it return a wrapped json response with
   * a random IOException
   *
   */
  public static class MockWebHdfsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static int respondTimes = 0;
    private static final String RANDOM_EXCEPTION_MSG =
        "This is a random exception";

    @Override
    public void doGet(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {
      response.setHeader("Content-Type",
          MediaType.APPLICATION_JSON);
      String param = request.getParameter("op");
      if(respondTimes == 0) {
        Exception mockException = new IllegalArgumentException(
            "Invalid value for webhdfs parameter \"op\". "
                + "" + "No enum constant " + param);
        sendException(request, response, mockException);
      } else if (respondTimes == 1) {
        sendResponse(request, response);
      } else if (respondTimes == 2) {
        Exception mockException = new IOException(RANDOM_EXCEPTION_MSG);
        sendException(request, response, mockException);
      }
      respondTimes++;
    }

    private void sendResponse(HttpServletRequest request,
        HttpServletResponse response) throws IOException {
      response.setStatus(HttpServletResponse.SC_OK);
      // Construct a LocatedBlock for testing
      DatanodeInfo d = DFSTestUtil.getLocalDatanodeInfo();
      DatanodeInfo[] ds = new DatanodeInfo[1];
      ds[0] = d;
      ExtendedBlock b1 = new ExtendedBlock("bpid", 1, 121, 1);
      LocatedBlock l1 = new LocatedBlock(b1, ds);
      l1.setStartOffset(0);
      l1.setCorrupt(false);
      List<LocatedBlock> ls = Arrays.asList(l1);
      LocatedBlocks locatedblocks =
          new LocatedBlocks(10, false, ls, l1,
              true, null, null);

      try (PrintWriter pw = response.getWriter()) {
        pw.write(JsonUtil.toJsonString(locatedblocks));
      }
    }

    private void sendException(HttpServletRequest request,
        HttpServletResponse response,
        Exception mockException) throws IOException {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      String errJs = JsonUtil.toJsonString(mockException);
      try (PrintWriter pw = response.getWriter()) {
        pw.write(errJs);
      }
    }
  }

  @Test
  public void testGetFileBlockLocationsBackwardsCompatibility()
      throws Exception {
    final Configuration conf = WebHdfsTestUtil.createConf();
    final String pathSpec = WebHdfsFileSystem.PATH_PREFIX + "/*";
    HttpServer2 http = null;
    try {
      http = HttpServerFunctionalTest.createTestServer(conf);
      http.addServlet("test", pathSpec, MockWebHdfsServlet.class);
      http.start();

      // Write the address back to configuration so
      // WebHdfsFileSystem could connect to the mock server
      conf.set(DFSConfigKeys.DFS_NAMENODE_HTTP_ADDRESS_KEY,
          "localhost:" + http.getConnectorAddress(0).getPort());

      final WebHdfsFileSystem webFS = WebHdfsTestUtil.getWebHdfsFileSystem(
          conf, WebHdfsConstants.WEBHDFS_SCHEME);

      WebHdfsFileSystem spyFs = spy(webFS);
      BlockLocation[] locations = spyFs
          .getFileBlockLocations(new Path("p"), 0, 100);

      // Verify result
      assertEquals(1, locations.length);
      assertEquals(121, locations[0].getLength());

      // Verify the fall back
      // The function should be called exactly 2 times
      // 1st time handles GETFILEBLOCKLOCATIONS and found it is not supported
      // 2nd time fall back to handle GET_FILE_BLOCK_LOCATIONS
      verify(spyFs, times(2)).getFileBlockLocations(any(),
          any(), anyLong(), anyLong());

      // Verify it doesn't erroneously fall back
      // When server returns a different error, it should directly
      // throw an exception.
      try {
        spyFs.getFileBlockLocations(new Path("p"), 0, 100);
      } catch (Exception e) {
        assertTrue(e instanceof IOException);
        assertEquals(e.getMessage(), MockWebHdfsServlet.RANDOM_EXCEPTION_MSG);
        // Totally this function has been called 3 times
        verify(spyFs, times(3)).getFileBlockLocations(any(),
            any(), anyLong(), anyLong());
      }
    } finally {
      if(http != null) {
        http.stop();
      }
    }
  }
}
