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

package org.apache.hadoop.yarn.service;

import org.apache.commons.io.FileUtils;
import org.apache.curator.test.TestingCluster;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.yarn.service.api.records.Service;
import org.apache.hadoop.yarn.service.conf.YarnServiceConf;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.server.MiniYARNCluster;
import org.apache.hadoop.yarn.service.api.records.Component;
import org.apache.hadoop.yarn.service.api.records.Resource;
import org.apache.hadoop.yarn.service.utils.JsonSerDeser;
import org.apache.hadoop.yarn.service.utils.ServiceApiUtil;
import org.apache.hadoop.yarn.service.utils.SliderFileSystem;
import org.apache.hadoop.yarn.util.LinuxResourceCalculatorPlugin;
import org.apache.hadoop.yarn.util.ProcfsBasedProcessTree;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import static org.apache.hadoop.registry.client.api.RegistryConstants.KEY_REGISTRY_ZK_QUORUM;
import static org.apache.hadoop.yarn.conf.YarnConfiguration.DEBUG_NM_DELETE_DELAY_SEC;
import static org.apache.hadoop.yarn.conf.YarnConfiguration.NM_PMEM_CHECK_ENABLED;
import static org.apache.hadoop.yarn.conf.YarnConfiguration.NM_VMEM_CHECK_ENABLED;
import static org.apache.hadoop.yarn.conf.YarnConfiguration.TIMELINE_SERVICE_ENABLED;
import static org.apache.hadoop.yarn.service.conf.YarnServiceConf.AM_RESOURCE_MEM;
import static org.apache.hadoop.yarn.service.conf.YarnServiceConf.YARN_SERVICE_BASE_PATH;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceTestUtils {

  private static final Logger LOG =
      LoggerFactory.getLogger(ServiceTestUtils.class);

  private MiniYARNCluster yarnCluster = null;
  private MiniDFSCluster hdfsCluster = null;
  TestingCluster zkCluster;
  private FileSystem fs = null;
  private Configuration conf = null;
  public static final int NUM_NMS = 1;
  private File basedir;

  public static final JsonSerDeser<Service> JSON_SER_DESER =
      new JsonSerDeser<>(Service.class,
          PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

  // Example service definition
  // 2 components, each of which has 2 containers.
  protected Service createExampleApplication() {
    Service exampleApp = new Service();
    exampleApp.setName("example-app");
    exampleApp.addComponent(createComponent("compa"));
    exampleApp.addComponent(createComponent("compb"));
    return exampleApp;
  }

  public static Component createComponent(String name) {
    return createComponent(name, 2L, "sleep 1000");
  }

  protected static Component createComponent(String name, long numContainers,
      String command) {
    Component comp1 = new Component();
    comp1.setNumberOfContainers(numContainers);
    comp1.setLaunchCommand(command);
    comp1.setName(name);
    Resource resource = new Resource();
    comp1.setResource(resource);
    resource.setMemory("128");
    resource.setCpus(1);
    return comp1;
  }

  public static SliderFileSystem initMockFs() throws IOException {
    return initMockFs(null);
  }

  public static SliderFileSystem initMockFs(Service ext) throws IOException {
    SliderFileSystem sfs = mock(SliderFileSystem.class);
    FileSystem mockFs = mock(FileSystem.class);
    JsonSerDeser<Service> jsonSerDeser = mock(JsonSerDeser.class);
    when(sfs.getFileSystem()).thenReturn(mockFs);
    when(sfs.buildClusterDirPath(anyObject())).thenReturn(
        new Path("cluster_dir_path"));
    if (ext != null) {
      when(jsonSerDeser.load(anyObject(), anyObject())).thenReturn(ext);
    }
    ServiceApiUtil.setJsonSerDeser(jsonSerDeser);
    return sfs;
  }

  protected void setConf(YarnConfiguration conf) {
    this.conf = conf;
  }

  protected Configuration getConf() {
    return conf;
  }

  protected FileSystem getFS() {
    return fs;
  }

  protected MiniYARNCluster getYarnCluster() {
    return yarnCluster;
  }

  protected void setupInternal(int numNodeManager)
      throws Exception {
    LOG.info("Starting up YARN cluster");
    if (conf == null) {
      setConf(new YarnConfiguration());
    }
    conf.setInt(YarnConfiguration.RM_SCHEDULER_MINIMUM_ALLOCATION_MB, 128);
    // reduce the teardown waiting time
    conf.setLong(YarnConfiguration.DISPATCHER_DRAIN_EVENTS_TIMEOUT, 1000);
    conf.set("yarn.log.dir", "target");
    // mark if we need to launch the v1 timeline server
    // disable aux-service based timeline aggregators
    conf.set(YarnConfiguration.NM_AUX_SERVICES, "");
    conf.set(YarnConfiguration.NM_VMEM_PMEM_RATIO, "8");
    // Enable ContainersMonitorImpl
    conf.set(YarnConfiguration.NM_CONTAINER_MON_RESOURCE_CALCULATOR,
        LinuxResourceCalculatorPlugin.class.getName());
    conf.set(YarnConfiguration.NM_CONTAINER_MON_PROCESS_TREE,
        ProcfsBasedProcessTree.class.getName());
    conf.setBoolean(
        YarnConfiguration.YARN_MINICLUSTER_CONTROL_RESOURCE_MONITORING, true);
    conf.setBoolean(TIMELINE_SERVICE_ENABLED, false);
    conf.setInt(YarnConfiguration.NM_MAX_PER_DISK_UTILIZATION_PERCENTAGE, 100);
    conf.setLong(DEBUG_NM_DELETE_DELAY_SEC, 60000);
    conf.setLong(AM_RESOURCE_MEM, 526);
    conf.setLong(YarnServiceConf.READINESS_CHECK_INTERVAL, 5);
    // Disable vmem check to disallow NM killing the container
    conf.setBoolean(NM_VMEM_CHECK_ENABLED, false);
    conf.setBoolean(NM_PMEM_CHECK_ENABLED, false);
    // setup zk cluster
    zkCluster = new TestingCluster(1);
    zkCluster.start();
    conf.set(YarnConfiguration.RM_ZK_ADDRESS, zkCluster.getConnectString());
    conf.set(KEY_REGISTRY_ZK_QUORUM, zkCluster.getConnectString());
    LOG.info("ZK cluster: " +  zkCluster.getConnectString());

    fs = FileSystem.get(conf);
    basedir = new File("target", "apps");
    if (basedir.exists()) {
      FileUtils.deleteDirectory(basedir);
    } else {
      basedir.mkdirs();
    }

    conf.set(YARN_SERVICE_BASE_PATH, basedir.getAbsolutePath());

    if (yarnCluster == null) {
      yarnCluster =
          new MiniYARNCluster(TestYarnNativeServices.class.getSimpleName(), 1,
              numNodeManager, 1, 1);
      yarnCluster.init(conf);
      yarnCluster.start();

      waitForNMsToRegister();

      URL url = Thread.currentThread().getContextClassLoader()
          .getResource("yarn-site.xml");
      if (url == null) {
        throw new RuntimeException(
            "Could not find 'yarn-site.xml' dummy file in classpath");
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
      LOG.info("Write yarn-site.xml configs to: " + url);
    }
    if (hdfsCluster == null) {
      HdfsConfiguration hdfsConfig = new HdfsConfiguration();
      hdfsCluster = new MiniDFSCluster.Builder(hdfsConfig)
          .numDataNodes(1).build();
    }

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      LOG.info("setup thread sleep interrupted. message=" + e.getMessage());
    }
  }

  public void shutdown() throws IOException {
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
    if (zkCluster != null) {
      zkCluster.stop();
    }
    if (basedir != null) {
      FileUtils.deleteDirectory(basedir);
    }
    SliderFileSystem sfs = new SliderFileSystem(conf);
    Path appDir = sfs.getBaseApplicationPath();
    sfs.getFileSystem().delete(appDir, true);
  }

  private void waitForNMsToRegister() throws Exception {
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

}
