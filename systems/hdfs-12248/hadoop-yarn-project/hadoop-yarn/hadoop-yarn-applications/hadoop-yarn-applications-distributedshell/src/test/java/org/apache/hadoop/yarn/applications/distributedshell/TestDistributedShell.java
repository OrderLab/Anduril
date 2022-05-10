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

package org.apache.hadoop.yarn.applications.distributedshell;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.FileContext;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.net.ServerSocketUtil;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.util.JarFinder;
import org.apache.hadoop.util.Shell;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ContainerState;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.api.records.timeline.TimelineDomain;
import org.apache.hadoop.yarn.api.records.timeline.TimelineEntities;
import org.apache.hadoop.yarn.api.records.timelineservice.TimelineEntityType;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.impl.DirectTimelineWriter;
import org.apache.hadoop.yarn.client.api.impl.TestTimelineClient;
import org.apache.hadoop.yarn.client.api.impl.TimelineClientImpl;
import org.apache.hadoop.yarn.client.api.impl.TimelineWriter;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.server.MiniYARNCluster;
import org.apache.hadoop.yarn.server.metrics.AppAttemptMetricsConstants;
import org.apache.hadoop.yarn.server.metrics.ApplicationMetricsConstants;
import org.apache.hadoop.yarn.server.metrics.ContainerMetricsConstants;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacityScheduler;
import org.apache.hadoop.yarn.server.timeline.NameValuePair;
import org.apache.hadoop.yarn.server.timeline.PluginStoreTestUtils;
import org.apache.hadoop.yarn.server.timeline.TimelineVersion;
import org.apache.hadoop.yarn.server.timeline.TimelineVersionWatcher;
import org.apache.hadoop.yarn.server.timelineservice.collector.PerNodeTimelineCollectorsAuxService;
import org.apache.hadoop.yarn.server.timelineservice.storage.FileSystemTimelineWriterImpl;
import org.apache.hadoop.yarn.server.utils.BuilderUtils;
import org.apache.hadoop.yarn.util.LinuxResourceCalculatorPlugin;
import org.apache.hadoop.yarn.util.ProcfsBasedProcessTree;
import org.apache.hadoop.yarn.util.timeline.TimelineUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;

public class TestDistributedShell {

  private static final Log LOG =
      LogFactory.getLog(TestDistributedShell.class);

  protected MiniYARNCluster yarnCluster = null;
  protected MiniDFSCluster hdfsCluster = null;
  private FileSystem fs = null;
  private TimelineWriter spyTimelineWriter;
  protected YarnConfiguration conf = null;
  // location of the filesystem timeline writer for timeline service v.2
  private String timelineV2StorageDir = null;
  private static final int NUM_NMS = 1;
  private static final float DEFAULT_TIMELINE_VERSION = 1.0f;
  private static final String TIMELINE_AUX_SERVICE_NAME = "timeline_collector";

  protected final static String APPMASTER_JAR =
      JarFinder.getJar(ApplicationMaster.class);

  @Rule
  public TimelineVersionWatcher timelineVersionWatcher
      = new TimelineVersionWatcher();
  @Rule
  public Timeout globalTimeout = new Timeout(90000);
  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  @Before
  public void setup() throws Exception {
    setupInternal(NUM_NMS, timelineVersionWatcher.getTimelineVersion());
  }

  protected void setupInternal(int numNodeManager) throws Exception {
    setupInternal(numNodeManager, DEFAULT_TIMELINE_VERSION);
  }

  private void setupInternal(int numNodeManager, float timelineVersion)
      throws Exception {
    LOG.info("Starting up YARN cluster");

    conf = new YarnConfiguration();
    conf.setInt(YarnConfiguration.RM_SCHEDULER_MINIMUM_ALLOCATION_MB, 128);
    // reduce the teardown waiting time
    conf.setLong(YarnConfiguration.DISPATCHER_DRAIN_EVENTS_TIMEOUT, 1000);
    conf.set("yarn.log.dir", "target");
    conf.setBoolean(YarnConfiguration.TIMELINE_SERVICE_ENABLED, true);
    // mark if we need to launch the v1 timeline server
    // disable aux-service based timeline aggregators
    conf.set(YarnConfiguration.NM_AUX_SERVICES, "");
    conf.setBoolean(YarnConfiguration.SYSTEM_METRICS_PUBLISHER_ENABLED, true);

    conf.set(YarnConfiguration.NM_VMEM_PMEM_RATIO, "8");
    conf.set(YarnConfiguration.RM_SCHEDULER, CapacityScheduler.class.getName());
    conf.setBoolean(YarnConfiguration.NODE_LABELS_ENABLED, true);
    conf.set("mapreduce.jobhistory.address",
        "0.0.0.0:" + ServerSocketUtil.getPort(10021, 10));
    // Enable ContainersMonitorImpl
    conf.set(YarnConfiguration.NM_CONTAINER_MON_RESOURCE_CALCULATOR,
        LinuxResourceCalculatorPlugin.class.getName());
    conf.set(YarnConfiguration.NM_CONTAINER_MON_PROCESS_TREE,
        ProcfsBasedProcessTree.class.getName());
    conf.setBoolean(YarnConfiguration.NM_PMEM_CHECK_ENABLED, true);
    conf.setBoolean(YarnConfiguration.NM_VMEM_CHECK_ENABLED, true);
    conf.setBoolean(
        YarnConfiguration.YARN_MINICLUSTER_CONTROL_RESOURCE_MONITORING,
        true);
    conf.setBoolean(YarnConfiguration.RM_SYSTEM_METRICS_PUBLISHER_ENABLED,
          true);

    // ATS version specific settings
    if (timelineVersion == 1.0f) {
      conf.setFloat(YarnConfiguration.TIMELINE_SERVICE_VERSION, 1.0f);
      conf.set(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY,
          CommonConfigurationKeysPublic.FS_DEFAULT_NAME_DEFAULT);
    } else if (timelineVersion == 1.5f) {
      if (hdfsCluster == null) {
        HdfsConfiguration hdfsConfig = new HdfsConfiguration();
        hdfsCluster = new MiniDFSCluster.Builder(hdfsConfig)
            .numDataNodes(1).build();
      }
      fs = hdfsCluster.getFileSystem();
      PluginStoreTestUtils.prepareFileSystemForPluginStore(fs);
      PluginStoreTestUtils.prepareConfiguration(conf, hdfsCluster);
      conf.set(YarnConfiguration.TIMELINE_SERVICE_ENTITY_GROUP_PLUGIN_CLASSES,
          DistributedShellTimelinePlugin.class.getName());
    } else if (timelineVersion == 2.0f) {
      // set version to 2
      conf.setFloat(YarnConfiguration.TIMELINE_SERVICE_VERSION, 2.0f);
      // disable v1 timeline server since we no longer have a server here
      // enable aux-service based timeline aggregators
      conf.set(YarnConfiguration.NM_AUX_SERVICES, TIMELINE_AUX_SERVICE_NAME);
      conf.set(YarnConfiguration.NM_AUX_SERVICES + "." +
          TIMELINE_AUX_SERVICE_NAME + ".class",
          PerNodeTimelineCollectorsAuxService.class.getName());
      conf.setClass(YarnConfiguration.TIMELINE_SERVICE_WRITER_CLASS,
          FileSystemTimelineWriterImpl.class,
          org.apache.hadoop.yarn.server.timelineservice.storage.
              TimelineWriter.class);
      timelineV2StorageDir = tmpFolder.newFolder().getAbsolutePath();
      // set the file system timeline writer storage directory
      conf.set(FileSystemTimelineWriterImpl.TIMELINE_SERVICE_STORAGE_DIR_ROOT,
          timelineV2StorageDir);
    } else {
      Assert.fail("Wrong timeline version number: " + timelineVersion);
    }
    
    if (yarnCluster == null) {
      yarnCluster =
          new MiniYARNCluster(TestDistributedShell.class.getSimpleName(), 1,
              numNodeManager, 1, 1);
      yarnCluster.init(conf);
      
      yarnCluster.start();

      conf.set(
          YarnConfiguration.TIMELINE_SERVICE_WEBAPP_ADDRESS,
          MiniYARNCluster.getHostname() + ":"
              + yarnCluster.getApplicationHistoryServer().getPort());

      waitForNMsToRegister();

      URL url = Thread.currentThread().getContextClassLoader().getResource("yarn-site.xml");
      if (url == null) {
        throw new RuntimeException("Could not find 'yarn-site.xml' dummy file in classpath");
      }
      Configuration yarnClusterConfig = yarnCluster.getConfig();
      yarnClusterConfig.set(YarnConfiguration.YARN_APPLICATION_CLASSPATH,
          new File(url.getPath()).getParent());
      //write the document to a buffer (not directly to the file, as that
      //can cause the file being written to get read -which will then fail.
      ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
      yarnClusterConfig.writeXml(bytesOut);
      bytesOut.close();
      //write the bytes to the file in the classpath
      OutputStream os = new FileOutputStream(new File(url.getPath()));
      os.write(bytesOut.toByteArray());
      os.close();
    }
    FileContext fsContext = FileContext.getLocalFSFileContext();
    fsContext
        .delete(
            new Path(conf.get(YarnConfiguration.TIMELINE_SERVICE_LEVELDB_PATH)),
            true);
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      LOG.info("setup thread sleep interrupted. message=" + e.getMessage());
    }
  }

  @After
  public void tearDown() throws IOException {
    if (yarnCluster != null) {
      try {
        yarnCluster.stop();
      } finally {
        yarnCluster = null;
      }
    }
    if (hdfsCluster != null) {
      try {
        hdfsCluster.shutdown();
      } finally {
        hdfsCluster = null;
      }
    }
    FileContext fsContext = FileContext.getLocalFSFileContext();
    fsContext
        .delete(
            new Path(conf.get(YarnConfiguration.TIMELINE_SERVICE_LEVELDB_PATH)),
            true);
  }

  @Test
  public void testDSShellWithDomain() throws Exception {
    testDSShell(true);
  }

  @Test
  public void testDSShellWithoutDomain() throws Exception {
    testDSShell(false);
  }

  @Test
  @TimelineVersion(1.5f)
  public void testDSShellWithoutDomainV1_5() throws Exception {
    testDSShell(false);
  }

  @Test
  @TimelineVersion(1.5f)
  public void testDSShellWithDomainV1_5() throws Exception {
    testDSShell(true);
  }

  @Test
  @TimelineVersion(2.0f)
  public void testDSShellWithoutDomainV2() throws Exception {
    testDSShell(false);
  }

  public void testDSShell(boolean haveDomain) throws Exception {
    testDSShell(haveDomain, true);
  }

  @Test
  @TimelineVersion(2.0f)
  public void testDSShellWithoutDomainV2DefaultFlow() throws Exception {
    testDSShell(false, true);
  }

  @Test
  @TimelineVersion(2.0f)
  public void testDSShellWithoutDomainV2CustomizedFlow() throws Exception {
    testDSShell(false, false);
  }

  public void testDSShell(boolean haveDomain, boolean defaultFlow)
      throws Exception {
    String[] args = {
        "--jar",
        APPMASTER_JAR,
        "--num_containers",
        "2",
        "--shell_command",
        Shell.WINDOWS ? "dir" : "ls",
        "--master_memory",
        "512",
        "--master_vcores",
        "2",
        "--container_memory",
        "128",
        "--container_vcores",
        "1"
    };
    if (haveDomain) {
      String[] domainArgs = {
          "--domain",
          "TEST_DOMAIN",
          "--view_acls",
          "reader_user reader_group",
          "--modify_acls",
          "writer_user writer_group",
          "--create"
      };
      args = mergeArgs(args, domainArgs);
    }
    boolean isTestingTimelineV2 = false;
    if (timelineVersionWatcher.getTimelineVersion() == 2.0f) {
      isTestingTimelineV2 = true;
      if (!defaultFlow) {
        String[] flowArgs = {
            "--flow_name",
            "test_flow_name",
            "--flow_version",
            "test_flow_version",
            "--flow_run_id",
            "12345678"
        };
        args = mergeArgs(args, flowArgs);
      }
      LOG.info("Setup: Using timeline v2!");
    }

    LOG.info("Initializing DS Client");
    final Client client = new Client(new Configuration(yarnCluster.getConfig()));
    boolean initSuccess = client.init(args);
    Assert.assertTrue(initSuccess);
    LOG.info("Running DS Client");
    final AtomicBoolean result = new AtomicBoolean(false);
    Thread t = new Thread() {
      public void run() {
        try {
          result.set(client.run());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    };
    t.start();

    YarnClient yarnClient = YarnClient.createYarnClient();
    yarnClient.init(new Configuration(yarnCluster.getConfig()));
    yarnClient.start();
    String hostName = NetUtils.getHostname();

    boolean verified = false;
    String errorMessage = "";
    ApplicationId appId = null;
    ApplicationReport appReport = null;
    while(!verified) {
      List<ApplicationReport> apps = yarnClient.getApplications();
      if (apps.size() == 0 ) {
        Thread.sleep(10);
        continue;
      }
      appReport = apps.get(0);
      appId = appReport.getApplicationId();
      if(appReport.getHost().equals("N/A")) {
        Thread.sleep(10);
        continue;
      }
      errorMessage =
          "Expected host name to start with '" + hostName + "', was '"
              + appReport.getHost() + "'. Expected rpc port to be '-1', was '"
              + appReport.getRpcPort() + "'.";
      if (checkHostname(appReport.getHost()) && appReport.getRpcPort() == -1) {
        verified = true;
      }

      if (appReport.getYarnApplicationState() == YarnApplicationState.FINISHED
          && appReport.getFinalApplicationStatus() !=
              FinalApplicationStatus.UNDEFINED) {
        break;
      }
    }
    Assert.assertTrue(errorMessage, verified);
    t.join();
    LOG.info("Client run completed for testDSShell. Result=" + result);
    Assert.assertTrue(result.get());

    if (timelineVersionWatcher.getTimelineVersion() == 1.5f) {
      long scanInterval = conf.getLong(
          YarnConfiguration.TIMELINE_SERVICE_ENTITYGROUP_FS_STORE_SCAN_INTERVAL_SECONDS,
          YarnConfiguration.TIMELINE_SERVICE_ENTITYGROUP_FS_STORE_SCAN_INTERVAL_SECONDS_DEFAULT
      );
      Path doneDir = new Path(
          YarnConfiguration.TIMELINE_SERVICE_ENTITYGROUP_FS_STORE_DONE_DIR_DEFAULT
      );
      // Wait till the data is moved to done dir, or timeout and fail
      while (true) {
        RemoteIterator<FileStatus> iterApps = fs.listStatusIterator(doneDir);
        if (iterApps.hasNext()) {
          break;
        }
        Thread.sleep(scanInterval * 2);
      }
    }

    TimelineDomain domain = null;
    if (!isTestingTimelineV2) {
      checkTimelineV1(haveDomain);
    } else {
      checkTimelineV2(haveDomain, appId, defaultFlow, appReport);
    }
  }

  private void checkTimelineV1(boolean haveDomain) throws Exception {
    TimelineDomain domain = null;
    if (haveDomain) {
      domain = yarnCluster.getApplicationHistoryServer()
          .getTimelineStore().getDomain("TEST_DOMAIN");
      Assert.assertNotNull(domain);
      Assert.assertEquals("reader_user reader_group", domain.getReaders());
      Assert.assertEquals("writer_user writer_group", domain.getWriters());
    }
    TimelineEntities entitiesAttempts = yarnCluster
        .getApplicationHistoryServer()
        .getTimelineStore()
        .getEntities(ApplicationMaster.DSEntity.DS_APP_ATTEMPT.toString(),
            null, null, null, null, null, null, null, null, null);
    Assert.assertNotNull(entitiesAttempts);
    Assert.assertEquals(1, entitiesAttempts.getEntities().size());
    Assert.assertEquals(2, entitiesAttempts.getEntities().get(0).getEvents()
        .size());
    Assert.assertEquals(entitiesAttempts.getEntities().get(0).getEntityType()
        .toString(), ApplicationMaster.DSEntity.DS_APP_ATTEMPT.toString());
    if (haveDomain) {
      Assert.assertEquals(domain.getId(),
          entitiesAttempts.getEntities().get(0).getDomainId());
    } else {
      Assert.assertEquals("DEFAULT",
          entitiesAttempts.getEntities().get(0).getDomainId());
    }
    String currAttemptEntityId
        = entitiesAttempts.getEntities().get(0).getEntityId();
    ApplicationAttemptId attemptId = ApplicationAttemptId.fromString(
        currAttemptEntityId);
    NameValuePair primaryFilter = new NameValuePair(
        ApplicationMaster.APPID_TIMELINE_FILTER_NAME,
        attemptId.getApplicationId().toString());
    TimelineEntities entities = yarnCluster
        .getApplicationHistoryServer()
        .getTimelineStore()
        .getEntities(ApplicationMaster.DSEntity.DS_CONTAINER.toString(), null,
            null, null, null, null, primaryFilter, null, null, null);
    Assert.assertNotNull(entities);
    Assert.assertEquals(2, entities.getEntities().size());
    Assert.assertEquals(entities.getEntities().get(0).getEntityType()
        .toString(), ApplicationMaster.DSEntity.DS_CONTAINER.toString());
    if (haveDomain) {
      Assert.assertEquals(domain.getId(),
          entities.getEntities().get(0).getDomainId());
    } else {
      Assert.assertEquals("DEFAULT",
          entities.getEntities().get(0).getDomainId());
    }
  }

  private void checkTimelineV2(boolean haveDomain, ApplicationId appId,
      boolean defaultFlow, ApplicationReport appReport) throws Exception {
    LOG.info("Started checkTimelineV2 ");
    // For PoC check using the file-based timeline writer (YARN-3264)
    String tmpRoot = timelineV2StorageDir + File.separator + "entities" +
        File.separator;

    File tmpRootFolder = new File(tmpRoot);
    try {
      Assert.assertTrue(tmpRootFolder.isDirectory());
      String basePath = tmpRoot +
          YarnConfiguration.DEFAULT_RM_CLUSTER_ID + File.separator +
          UserGroupInformation.getCurrentUser().getShortUserName() +
          (defaultFlow ?
              File.separator + appReport.getName() + File.separator +
                  TimelineUtils.DEFAULT_FLOW_VERSION + File.separator +
                  appReport.getStartTime() + File.separator :
              File.separator + "test_flow_name" + File.separator +
                  "test_flow_version" + File.separator + "12345678" +
                  File.separator) +
          appId.toString();
      LOG.info("basePath: " + basePath);
      // for this test, we expect DS_APP_ATTEMPT AND DS_CONTAINER dirs

      // Verify DS_APP_ATTEMPT entities posted by the client
      // there will be at least one attempt, look for that file
      String appTimestampFileName =
          "appattempt_" + appId.getClusterTimestamp() + "_000" + appId.getId()
              + "_000001"
              + FileSystemTimelineWriterImpl.TIMELINE_SERVICE_STORAGE_EXTENSION;
      verifyEntityTypeFileExists(basePath, "DS_APP_ATTEMPT",
          appTimestampFileName);

      // Verify DS_CONTAINER entities posted by the client
      String containerTimestampFileName =
          "container_" + appId.getClusterTimestamp() + "_000" + appId.getId()
              + "_01_000002.thist";
      verifyEntityTypeFileExists(basePath, "DS_CONTAINER",
          containerTimestampFileName);

      // Verify NM posting container metrics info.
      String containerMetricsTimestampFileName =
          "container_" + appId.getClusterTimestamp() + "_000" + appId.getId()
              + "_01_000001"
              + FileSystemTimelineWriterImpl.TIMELINE_SERVICE_STORAGE_EXTENSION;
      File containerEntityFile = verifyEntityTypeFileExists(basePath,
          TimelineEntityType.YARN_CONTAINER.toString(),
          containerMetricsTimestampFileName);
      Assert.assertEquals(
          "Container created event needs to be published atleast once",
          1,
          getNumOfStringOccurrences(containerEntityFile,
              ContainerMetricsConstants.CREATED_EVENT_TYPE));

      // to avoid race condition of testcase, atleast check 4 times with sleep
      // of 500ms
      long numOfContainerFinishedOccurrences = 0;
      for (int i = 0; i < 4; i++) {
        numOfContainerFinishedOccurrences =
            getNumOfStringOccurrences(containerEntityFile,
                ContainerMetricsConstants.FINISHED_EVENT_TYPE);
        if (numOfContainerFinishedOccurrences > 0) {
          break;
        } else {
          Thread.sleep(500L);
        }
      }
      Assert.assertEquals(
          "Container finished event needs to be published atleast once",
          1,
          numOfContainerFinishedOccurrences);

      // Verify RM posting Application life cycle Events are getting published
      String appMetricsTimestampFileName =
          "application_" + appId.getClusterTimestamp() + "_000" + appId.getId()
              + FileSystemTimelineWriterImpl.TIMELINE_SERVICE_STORAGE_EXTENSION;
      File appEntityFile =
          verifyEntityTypeFileExists(basePath,
              TimelineEntityType.YARN_APPLICATION.toString(),
              appMetricsTimestampFileName);
      Assert.assertEquals(
          "Application created event should be published atleast once",
          1,
          getNumOfStringOccurrences(appEntityFile,
              ApplicationMetricsConstants.CREATED_EVENT_TYPE));

      // to avoid race condition of testcase, atleast check 4 times with sleep
      // of 500ms
      long numOfStringOccurrences = 0;
      for (int i = 0; i < 4; i++) {
        numOfStringOccurrences =
            getNumOfStringOccurrences(appEntityFile,
                ApplicationMetricsConstants.FINISHED_EVENT_TYPE);
        if (numOfStringOccurrences > 0) {
          break;
        } else {
          Thread.sleep(500L);
        }
      }
      Assert.assertEquals(
          "Application finished event should be published atleast once",
          1,
          numOfStringOccurrences);

      // Verify RM posting AppAttempt life cycle Events are getting published
      String appAttemptMetricsTimestampFileName =
          "appattempt_" + appId.getClusterTimestamp() + "_000" + appId.getId()
              + "_000001"
              + FileSystemTimelineWriterImpl.TIMELINE_SERVICE_STORAGE_EXTENSION;
      File appAttemptEntityFile =
          verifyEntityTypeFileExists(basePath,
              TimelineEntityType.YARN_APPLICATION_ATTEMPT.toString(),
              appAttemptMetricsTimestampFileName);
      Assert.assertEquals(
          "AppAttempt register event should be published atleast once",
          1,
          getNumOfStringOccurrences(appAttemptEntityFile,
              AppAttemptMetricsConstants.REGISTERED_EVENT_TYPE));

      Assert.assertEquals(
          "AppAttempt finished event should be published atleast once",
          1,
          getNumOfStringOccurrences(appAttemptEntityFile,
              AppAttemptMetricsConstants.FINISHED_EVENT_TYPE));
    } finally {
      FileUtils.deleteDirectory(tmpRootFolder.getParentFile());
    }
  }

  private File verifyEntityTypeFileExists(String basePath, String entityType,
      String entityfileName) {
    String outputDirPathForEntity =
        basePath + File.separator + entityType + File.separator;
    File outputDirForEntity = new File(outputDirPathForEntity);
    Assert.assertTrue(outputDirForEntity.isDirectory());

    String entityFilePath = outputDirPathForEntity + entityfileName;

    File entityFile = new File(entityFilePath);
    Assert.assertTrue(entityFile.exists());
    return entityFile;
  }

  private long getNumOfStringOccurrences(File entityFile, String searchString)
      throws IOException {
    BufferedReader reader = null;
    String strLine;
    long actualCount = 0;
    try {
      reader = new BufferedReader(new FileReader(entityFile));
      while ((strLine = reader.readLine()) != null) {
        if (strLine.trim().contains(searchString)) {
          actualCount++;
        }
      }
    } finally {
      reader.close();
    }
    return actualCount;
  }

  /**
   * Utility function to merge two String arrays to form a new String array for
   * our argumemts.
   *
   * @param args
   * @param newArgs
   * @return a String array consists of {args, newArgs}
   */
  private String[] mergeArgs(String[] args, String[] newArgs) {
    List<String> argsList = new ArrayList<String>(Arrays.asList(args));
    argsList.addAll(Arrays.asList(newArgs));
    return argsList.toArray(new String[argsList.size()]);
  }

  /*
   * NetUtils.getHostname() returns a string in the form "hostname/ip".
   * Sometimes the hostname we get is the FQDN and sometimes the short name. In
   * addition, on machines with multiple network interfaces, it runs any one of
   * the ips. The function below compares the returns values for
   * NetUtils.getHostname() accounting for the conditions mentioned.
   */
  private boolean checkHostname(String appHostname) throws Exception {

    String hostname = NetUtils.getHostname();
    if (hostname.equals(appHostname)) {
      return true;
    }

    Assert.assertTrue("Unknown format for hostname " + appHostname,
      appHostname.contains("/"));
    Assert.assertTrue("Unknown format for hostname " + hostname,
      hostname.contains("/"));

    String[] appHostnameParts = appHostname.split("/");
    String[] hostnameParts = hostname.split("/");

    return (compareFQDNs(appHostnameParts[0], hostnameParts[0]) && checkIPs(
      hostnameParts[0], hostnameParts[1], appHostnameParts[1]));
  }

  private boolean compareFQDNs(String appHostname, String hostname)
      throws Exception {
    if (appHostname.equals(hostname)) {
      return true;
    }
    String appFQDN = InetAddress.getByName(appHostname).getCanonicalHostName();
    String localFQDN = InetAddress.getByName(hostname).getCanonicalHostName();
    return appFQDN.equals(localFQDN);
  }

  private boolean checkIPs(String hostname, String localIP, String appIP)
      throws Exception {

    if (localIP.equals(appIP)) {
      return true;
    }
    boolean appIPCheck = false;
    boolean localIPCheck = false;
    InetAddress[] addresses = InetAddress.getAllByName(hostname);
    for (InetAddress ia : addresses) {
      if (ia.getHostAddress().equals(appIP)) {
        appIPCheck = true;
        continue;
      }
      if (ia.getHostAddress().equals(localIP)) {
        localIPCheck = true;
      }
    }
    return (appIPCheck && localIPCheck);

  }

  @Test
  public void testDSRestartWithPreviousRunningContainers() throws Exception {
    String[] args = {
        "--jar",
        APPMASTER_JAR,
        "--num_containers",
        "1",
        "--shell_command",
        "sleep 8",
        "--master_memory",
        "512",
        "--container_memory",
        "128",
        "--keep_containers_across_application_attempts"
      };

      LOG.info("Initializing DS Client");
      Client client = new Client(TestDSFailedAppMaster.class.getName(),
        new Configuration(yarnCluster.getConfig()));

      client.init(args);
      LOG.info("Running DS Client");
      boolean result = client.run();

      LOG.info("Client run completed. Result=" + result);
      // application should succeed
      Assert.assertTrue(result);
    }

  /*
   * The sleeping period in TestDSSleepingAppMaster is set as 5 seconds.
   * Set attempt_failures_validity_interval as 2.5 seconds. It will check
   * how many attempt failures for previous 2.5 seconds.
   * The application is expected to be successful.
   */
  @Test
  public void testDSAttemptFailuresValidityIntervalSucess() throws Exception {
    String[] args = {
        "--jar",
        APPMASTER_JAR,
        "--num_containers",
        "1",
        "--shell_command",
        "sleep 8",
        "--master_memory",
        "512",
        "--container_memory",
        "128",
        "--attempt_failures_validity_interval",
        "2500"
      };

      LOG.info("Initializing DS Client");
      Configuration conf = yarnCluster.getConfig();
      conf.setInt(YarnConfiguration.RM_AM_MAX_ATTEMPTS, 2);
      Client client = new Client(TestDSSleepingAppMaster.class.getName(),
        new Configuration(conf));

      client.init(args);
      LOG.info("Running DS Client");
      boolean result = client.run();

      LOG.info("Client run completed. Result=" + result);
      // application should succeed
      Assert.assertTrue(result);
    }

  /*
   * The sleeping period in TestDSSleepingAppMaster is set as 5 seconds.
   * Set attempt_failures_validity_interval as 15 seconds. It will check
   * how many attempt failure for previous 15 seconds.
   * The application is expected to be fail.
   */
  @Test
  public void testDSAttemptFailuresValidityIntervalFailed() throws Exception {
    String[] args = {
        "--jar",
        APPMASTER_JAR,
        "--num_containers",
        "1",
        "--shell_command",
        "sleep 8",
        "--master_memory",
        "512",
        "--container_memory",
        "128",
        "--attempt_failures_validity_interval",
        "15000"
      };

      LOG.info("Initializing DS Client");
      Configuration conf = yarnCluster.getConfig();
      conf.setInt(YarnConfiguration.RM_AM_MAX_ATTEMPTS, 2);
      Client client = new Client(TestDSSleepingAppMaster.class.getName(),
        new Configuration(conf));

      client.init(args);
      LOG.info("Running DS Client");
      boolean result = client.run();

      LOG.info("Client run completed. Result=" + result);
      // application should be failed
      Assert.assertFalse(result);
    }

  @Test
  public void testDSShellWithCustomLogPropertyFile() throws Exception {
    final File basedir =
        new File("target", TestDistributedShell.class.getName());
    final File tmpDir = new File(basedir, "tmpDir");
    tmpDir.mkdirs();
    final File customLogProperty = new File(tmpDir, "custom_log4j.properties");
    if (customLogProperty.exists()) {
      customLogProperty.delete();
    }
    if(!customLogProperty.createNewFile()) {
      Assert.fail("Can not create custom log4j property file.");
    }
    PrintWriter fileWriter = new PrintWriter(customLogProperty);
    // set the output to DEBUG level
    fileWriter.write("log4j.rootLogger=debug,stdout");
    fileWriter.close();
    String[] args = {
        "--jar",
        APPMASTER_JAR,
        "--num_containers",
        "3",
        "--shell_command",
        "echo",
        "--shell_args",
        "HADOOP",
        "--log_properties",
        customLogProperty.getAbsolutePath(),
        "--master_memory",
        "512",
        "--master_vcores",
        "2",
        "--container_memory",
        "128",
        "--container_vcores",
        "1"
    };

    //Before run the DS, the default the log level is INFO
    final Log LOG_Client =
        LogFactory.getLog(Client.class);
    Assert.assertTrue(LOG_Client.isInfoEnabled());
    Assert.assertFalse(LOG_Client.isDebugEnabled());
    final Log LOG_AM = LogFactory.getLog(ApplicationMaster.class);
    Assert.assertTrue(LOG_AM.isInfoEnabled());
    Assert.assertFalse(LOG_AM.isDebugEnabled());

    LOG.info("Initializing DS Client");
    final Client client =
        new Client(new Configuration(yarnCluster.getConfig()));
    boolean initSuccess = client.init(args);
    Assert.assertTrue(initSuccess);
    LOG.info("Running DS Client");
    boolean result = client.run();
    LOG.info("Client run completed. Result=" + result);
    Assert.assertTrue(verifyContainerLog(3, null, true, "DEBUG") > 10);
    //After DS is finished, the log level should be DEBUG
    Assert.assertTrue(LOG_Client.isInfoEnabled());
    Assert.assertTrue(LOG_Client.isDebugEnabled());
    Assert.assertTrue(LOG_AM.isInfoEnabled());
    Assert.assertTrue(LOG_AM.isDebugEnabled());
  }

  public void testDSShellWithCommands() throws Exception {

    String[] args = {
        "--jar",
        APPMASTER_JAR,
        "--num_containers",
        "2",
        "--shell_command",
        "\"echo output_ignored;echo output_expected\"",
        "--master_memory",
        "512",
        "--master_vcores",
        "2",
        "--container_memory",
        "128",
        "--container_vcores",
        "1"
    };

    LOG.info("Initializing DS Client");
    final Client client =
        new Client(new Configuration(yarnCluster.getConfig()));
    boolean initSuccess = client.init(args);
    Assert.assertTrue(initSuccess);
    LOG.info("Running DS Client");
    boolean result = client.run();
    LOG.info("Client run completed. Result=" + result);
    List<String> expectedContent = new ArrayList<String>();
    expectedContent.add("output_expected");
    verifyContainerLog(2, expectedContent, false, "");
  }

  @Test
  public void testDSShellWithMultipleArgs() throws Exception {
    String[] args = {
        "--jar",
        APPMASTER_JAR,
        "--num_containers",
        "4",
        "--shell_command",
        "echo",
        "--shell_args",
        "HADOOP YARN MAPREDUCE HDFS",
        "--master_memory",
        "512",
        "--master_vcores",
        "2",
        "--container_memory",
        "128",
        "--container_vcores",
        "1"
    };

    LOG.info("Initializing DS Client");
    final Client client =
        new Client(new Configuration(yarnCluster.getConfig()));
    boolean initSuccess = client.init(args);
    Assert.assertTrue(initSuccess);
    LOG.info("Running DS Client");
    boolean result = client.run();
    LOG.info("Client run completed. Result=" + result);
    List<String> expectedContent = new ArrayList<String>();
    expectedContent.add("HADOOP YARN MAPREDUCE HDFS");
    verifyContainerLog(4, expectedContent, false, "");
  }

  @Test
  public void testDSShellWithShellScript() throws Exception {
    final File basedir =
        new File("target", TestDistributedShell.class.getName());
    final File tmpDir = new File(basedir, "tmpDir");
    tmpDir.mkdirs();
    final File customShellScript = new File(tmpDir, "custom_script.sh");
    if (customShellScript.exists()) {
      customShellScript.delete();
    }
    if (!customShellScript.createNewFile()) {
      Assert.fail("Can not create custom shell script file.");
    }
    PrintWriter fileWriter = new PrintWriter(customShellScript);
    // set the output to DEBUG level
    fileWriter.write("echo testDSShellWithShellScript");
    fileWriter.close();
    System.out.println(customShellScript.getAbsolutePath());
    String[] args = {
        "--jar",
        APPMASTER_JAR,
        "--num_containers",
        "1",
        "--shell_script",
        customShellScript.getAbsolutePath(),
        "--master_memory",
        "512",
        "--master_vcores",
        "2",
        "--container_memory",
        "128",
        "--container_vcores",
        "1"
    };

    LOG.info("Initializing DS Client");
    final Client client =
        new Client(new Configuration(yarnCluster.getConfig()));
    boolean initSuccess = client.init(args);
    Assert.assertTrue(initSuccess);
    LOG.info("Running DS Client");
    boolean result = client.run();
    LOG.info("Client run completed. Result=" + result);
    List<String> expectedContent = new ArrayList<String>();
    expectedContent.add("testDSShellWithShellScript");
    verifyContainerLog(1, expectedContent, false, "");
  }

  @Test
  public void testDSShellWithInvalidArgs() throws Exception {
    Client client = new Client(new Configuration(yarnCluster.getConfig()));

    LOG.info("Initializing DS Client with no args");
    try {
      client.init(new String[]{});
      Assert.fail("Exception is expected");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue("The throw exception is not expected",
          e.getMessage().contains("No args"));
    }

    LOG.info("Initializing DS Client with no jar file");
    try {
      String[] args = {
          "--num_containers",
          "2",
          "--shell_command",
          Shell.WINDOWS ? "dir" : "ls",
          "--master_memory",
          "512",
          "--container_memory",
          "128"
      };
      client.init(args);
      Assert.fail("Exception is expected");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue("The throw exception is not expected",
          e.getMessage().contains("No jar"));
    }

    LOG.info("Initializing DS Client with no shell command");
    try {
      String[] args = {
          "--jar",
          APPMASTER_JAR,
          "--num_containers",
          "2",
          "--master_memory",
          "512",
          "--container_memory",
          "128"
      };
      client.init(args);
      Assert.fail("Exception is expected");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue("The throw exception is not expected",
          e.getMessage().contains("No shell command"));
    }

    LOG.info("Initializing DS Client with invalid no. of containers");
    try {
      String[] args = {
          "--jar",
          APPMASTER_JAR,
          "--num_containers",
          "-1",
          "--shell_command",
          Shell.WINDOWS ? "dir" : "ls",
          "--master_memory",
          "512",
          "--container_memory",
          "128"
      };
      client.init(args);
      Assert.fail("Exception is expected");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue("The throw exception is not expected",
          e.getMessage().contains("Invalid no. of containers"));
    }
    
    LOG.info("Initializing DS Client with invalid no. of vcores");
    try {
      String[] args = {
          "--jar",
          APPMASTER_JAR,
          "--num_containers",
          "2",
          "--shell_command",
          Shell.WINDOWS ? "dir" : "ls",
          "--master_memory",
          "512",
          "--master_vcores",
          "-2",
          "--container_memory",
          "128",
          "--container_vcores",
          "1"
      };
      client.init(args);
      Assert.fail("Exception is expected");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue("The throw exception is not expected",
          e.getMessage().contains("Invalid virtual cores specified"));
    }

    LOG.info("Initializing DS Client with --shell_command and --shell_script");
    try {
      String[] args = {
          "--jar",
          APPMASTER_JAR,
          "--num_containers",
          "2",
          "--shell_command",
          Shell.WINDOWS ? "dir" : "ls",
          "--master_memory",
          "512",
          "--master_vcores",
          "2",
          "--container_memory",
          "128",
          "--container_vcores",
          "1",
          "--shell_script",
          "test.sh"
      };
      client.init(args);
      Assert.fail("Exception is expected");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue("The throw exception is not expected",
          e.getMessage().contains("Can not specify shell_command option " +
          "and shell_script option at the same time"));
    }

    LOG.info("Initializing DS Client without --shell_command and --shell_script");
    try {
      String[] args = {
          "--jar",
          APPMASTER_JAR,
          "--num_containers",
          "2",
          "--master_memory",
          "512",
          "--master_vcores",
          "2",
          "--container_memory",
          "128",
          "--container_vcores",
          "1"
      };
      client.init(args);
      Assert.fail("Exception is expected");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue("The throw exception is not expected",
          e.getMessage().contains("No shell command or shell script specified " +
          "to be executed by application master"));
    }
  }

  @Test
  public void testDSTimelineClientWithConnectionRefuse() throws Exception {
    ApplicationMaster am = new ApplicationMaster();

    TimelineClientImpl client = new TimelineClientImpl() {
      @Override
      protected TimelineWriter createTimelineWriter(Configuration conf,
          UserGroupInformation authUgi, com.sun.jersey.api.client.Client client,
          URI resURI) throws IOException {
        TimelineWriter timelineWriter =
            new DirectTimelineWriter(authUgi, client, resURI);
        spyTimelineWriter = spy(timelineWriter);
        return spyTimelineWriter;
      }
    };
    client.init(conf);
    client.start();
    TestTimelineClient.mockEntityClientResponse(spyTimelineWriter, null,
        false, true);
    try {
      UserGroupInformation ugi = mock(UserGroupInformation.class);
      when(ugi.getShortUserName()).thenReturn("user1");
      // verify no ClientHandlerException get thrown out.
      am.publishContainerEndEvent(client, ContainerStatus.newInstance(
          BuilderUtils.newContainerId(1, 1, 1, 1), ContainerState.COMPLETE, "",
          1), "domainId", ugi);
    } finally {
      client.stop();
    }
  }

  protected void waitForNMsToRegister() throws Exception {
    int sec = 60;
    while (sec >= 0) {
      if (yarnCluster.getResourceManager().getRMContext().getRMNodes().size() 
          >= NUM_NMS) {
        break;
      }
      Thread.sleep(1000);
      sec--;
    }
  }

  @Test
  public void testContainerLaunchFailureHandling() throws Exception {
    String[] args = {
      "--jar",
      APPMASTER_JAR,
      "--num_containers",
      "2",
      "--shell_command",
      Shell.WINDOWS ? "dir" : "ls",
      "--master_memory",
      "512",
      "--container_memory",
      "128"
    };

    LOG.info("Initializing DS Client");
    Client client = new Client(ContainerLaunchFailAppMaster.class.getName(),
      new Configuration(yarnCluster.getConfig()));
    boolean initSuccess = client.init(args);
    Assert.assertTrue(initSuccess);
    LOG.info("Running DS Client");
    boolean result = client.run();

    LOG.info("Client run completed. Result=" + result);
    Assert.assertFalse(result);

  }

  @Test
  public void testDebugFlag() throws Exception {
    String[] args = {
        "--jar",
        APPMASTER_JAR,
        "--num_containers",
        "2",
        "--shell_command",
        Shell.WINDOWS ? "dir" : "ls",
        "--master_memory",
        "512",
        "--master_vcores",
        "2",
        "--container_memory",
        "128",
        "--container_vcores",
        "1",
        "--debug"
    };

    LOG.info("Initializing DS Client");
    Client client = new Client(new Configuration(yarnCluster.getConfig()));
    Assert.assertTrue(client.init(args));
    LOG.info("Running DS Client");
    Assert.assertTrue(client.run());
  }

  private int verifyContainerLog(int containerNum,
      List<String> expectedContent, boolean count, String expectedWord) {
    File logFolder =
        new File(yarnCluster.getNodeManager(0).getConfig()
            .get(YarnConfiguration.NM_LOG_DIRS,
                YarnConfiguration.DEFAULT_NM_LOG_DIRS));

    File[] listOfFiles = logFolder.listFiles();
    int currentContainerLogFileIndex = -1;
    for (int i = listOfFiles.length - 1; i >= 0; i--) {
      if (listOfFiles[i].listFiles().length == containerNum + 1) {
        currentContainerLogFileIndex = i;
        break;
      }
    }
    Assert.assertTrue(currentContainerLogFileIndex != -1);
    File[] containerFiles =
        listOfFiles[currentContainerLogFileIndex].listFiles();

    int numOfWords = 0;
    for (int i = 0; i < containerFiles.length; i++) {
      for (File output : containerFiles[i].listFiles()) {
        if (output.getName().trim().contains("stdout")) {
          BufferedReader br = null;
          List<String> stdOutContent = new ArrayList<String>();
          try {

            String sCurrentLine;
            br = new BufferedReader(new FileReader(output));
            int numOfline = 0;
            while ((sCurrentLine = br.readLine()) != null) {
              if (count) {
                if (sCurrentLine.contains(expectedWord)) {
                  numOfWords++;
                }
              } else if (output.getName().trim().equals("stdout")){
                if (! Shell.WINDOWS) {
                  Assert.assertEquals("The current is" + sCurrentLine,
                      expectedContent.get(numOfline), sCurrentLine.trim());
                  numOfline++;
                } else {
                  stdOutContent.add(sCurrentLine.trim());
                }
              }
            }
            /* By executing bat script using cmd /c,
             * it will output all contents from bat script first
             * It is hard for us to do check line by line
             * Simply check whether output from bat file contains
             * all the expected messages
             */
            if (Shell.WINDOWS && !count
                && output.getName().trim().equals("stdout")) {
              Assert.assertTrue(stdOutContent.containsAll(expectedContent));
            }
          } catch (IOException e) {
            e.printStackTrace();
          } finally {
            try {
              if (br != null)
                br.close();
            } catch (IOException ex) {
              ex.printStackTrace();
            }
          }
        }
      }
    }
    return numOfWords;
  }
}
