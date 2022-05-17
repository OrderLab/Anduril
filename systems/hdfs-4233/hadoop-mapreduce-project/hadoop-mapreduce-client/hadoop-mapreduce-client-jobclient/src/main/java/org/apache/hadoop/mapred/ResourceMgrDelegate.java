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

package org.apache.hadoop.mapred;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.ClusterMetrics;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.JobStatus;
import org.apache.hadoop.mapreduce.MRJobConfig;
import org.apache.hadoop.mapreduce.QueueAclsInfo;
import org.apache.hadoop.mapreduce.QueueInfo;
import org.apache.hadoop.mapreduce.TaskTrackerInfo;
import org.apache.hadoop.mapreduce.TypeConverter;
import org.apache.hadoop.mapreduce.security.token.delegation.DelegationTokenIdentifier;
import org.apache.hadoop.mapreduce.v2.util.MRApps;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.token.Token;
import org.apache.hadoop.yarn.api.ClientRMProtocol;
import org.apache.hadoop.yarn.api.protocolrecords.GetAllApplicationsRequest;
import org.apache.hadoop.yarn.api.protocolrecords.GetAllApplicationsResponse;
import org.apache.hadoop.yarn.api.protocolrecords.GetApplicationReportRequest;
import org.apache.hadoop.yarn.api.protocolrecords.GetApplicationReportResponse;
import org.apache.hadoop.yarn.api.protocolrecords.GetClusterMetricsRequest;
import org.apache.hadoop.yarn.api.protocolrecords.GetClusterMetricsResponse;
import org.apache.hadoop.yarn.api.protocolrecords.GetClusterNodesRequest;
import org.apache.hadoop.yarn.api.protocolrecords.GetClusterNodesResponse;
import org.apache.hadoop.yarn.api.protocolrecords.GetNewApplicationRequest;
import org.apache.hadoop.yarn.api.protocolrecords.GetQueueInfoRequest;
import org.apache.hadoop.yarn.api.protocolrecords.GetQueueUserAclsInfoRequest;
import org.apache.hadoop.yarn.api.protocolrecords.KillApplicationRequest;
import org.apache.hadoop.yarn.api.protocolrecords.SubmitApplicationRequest;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.api.records.QueueUserACLInfo;
import org.apache.hadoop.yarn.api.records.DelegationToken;
import org.apache.hadoop.yarn.api.records.YarnClusterMetrics;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnRemoteException;
import org.apache.hadoop.yarn.factories.RecordFactory;
import org.apache.hadoop.yarn.factory.providers.RecordFactoryProvider;
import org.apache.hadoop.yarn.ipc.YarnRPC;
import org.apache.hadoop.yarn.util.ProtoUtils;


// TODO: This should be part of something like yarn-client.
public class ResourceMgrDelegate {
  private static final Log LOG = LogFactory.getLog(ResourceMgrDelegate.class);
      
  private final InetSocketAddress rmAddress;
  private YarnConfiguration conf;
  ClientRMProtocol applicationsManager;
  private ApplicationId applicationId;
  private final RecordFactory recordFactory = RecordFactoryProvider.getRecordFactory(null);

  /**
   * Delegate responsible for communicating with the Resource Manager's {@link ClientRMProtocol}.
   * @param conf the configuration object.
   */
  public ResourceMgrDelegate(YarnConfiguration conf) {
    this.conf = conf;
    YarnRPC rpc = YarnRPC.create(this.conf);
    this.rmAddress = getRmAddress(conf);
    LOG.debug("Connecting to ResourceManager at " + rmAddress);
    applicationsManager =
        (ClientRMProtocol) rpc.getProxy(ClientRMProtocol.class,
            rmAddress, this.conf);
    LOG.debug("Connected to ResourceManager at " + rmAddress);
  }
  
  /**
   * Used for injecting applicationsManager, mostly for testing.
   * @param conf the configuration object
   * @param applicationsManager the handle to talk the resource managers 
   *                            {@link ClientRMProtocol}.
   */
  public ResourceMgrDelegate(YarnConfiguration conf, 
      ClientRMProtocol applicationsManager) {
    this.conf = conf;
    this.applicationsManager = applicationsManager;
    this.rmAddress = getRmAddress(conf);
  }
  
  private static InetSocketAddress getRmAddress(YarnConfiguration conf) {
    return conf.getSocketAddr(YarnConfiguration.RM_ADDRESS,
                              YarnConfiguration.DEFAULT_RM_ADDRESS,
                              YarnConfiguration.DEFAULT_RM_PORT);
  }
  
  public void cancelDelegationToken(Token<DelegationTokenIdentifier> arg0)
      throws IOException, InterruptedException {
    return;
  }


  public TaskTrackerInfo[] getActiveTrackers() throws IOException,
      InterruptedException {
    GetClusterNodesRequest request = 
      recordFactory.newRecordInstance(GetClusterNodesRequest.class);
    GetClusterNodesResponse response = 
      applicationsManager.getClusterNodes(request);
    return TypeConverter.fromYarnNodes(response.getNodeReports());
  }


  public JobStatus[] getAllJobs() throws IOException, InterruptedException {
    GetAllApplicationsRequest request =
      recordFactory.newRecordInstance(GetAllApplicationsRequest.class);
    GetAllApplicationsResponse response = 
      applicationsManager.getAllApplications(request);
    return TypeConverter.fromYarnApps(response.getApplicationList(), this.conf);
  }


  public TaskTrackerInfo[] getBlacklistedTrackers() throws IOException,
      InterruptedException {
    // TODO: Implement getBlacklistedTrackers
    LOG.warn("getBlacklistedTrackers - Not implemented yet");
    return new TaskTrackerInfo[0];
  }


  public ClusterMetrics getClusterMetrics() throws IOException,
      InterruptedException {
    GetClusterMetricsRequest request = recordFactory.newRecordInstance(GetClusterMetricsRequest.class);
    GetClusterMetricsResponse response = applicationsManager.getClusterMetrics(request);
    YarnClusterMetrics metrics = response.getClusterMetrics();
    ClusterMetrics oldMetrics = new ClusterMetrics(1, 1, 1, 1, 1, 1, 
        metrics.getNumNodeManagers() * 10, metrics.getNumNodeManagers() * 2, 1,
        metrics.getNumNodeManagers(), 0, 0);
    return oldMetrics;
  }


  @SuppressWarnings("rawtypes")
  public Token getDelegationToken(Text renewer)
      throws IOException, InterruptedException {
    /* get the token from RM */
    org.apache.hadoop.yarn.api.protocolrecords.GetDelegationTokenRequest 
    rmDTRequest = recordFactory.newRecordInstance(
        org.apache.hadoop.yarn.api.protocolrecords.GetDelegationTokenRequest.class);
    rmDTRequest.setRenewer(renewer.toString());
    org.apache.hadoop.yarn.api.protocolrecords.GetDelegationTokenResponse 
      response = applicationsManager.getDelegationToken(rmDTRequest);
    DelegationToken yarnToken = response.getRMDelegationToken();
    return ProtoUtils.convertFromProtoFormat(yarnToken, rmAddress);
  }


  public String getFilesystemName() throws IOException, InterruptedException {
    return FileSystem.get(conf).getUri().toString();
  }

  public JobID getNewJobID() throws IOException, InterruptedException {
    GetNewApplicationRequest request = recordFactory.newRecordInstance(GetNewApplicationRequest.class);
    applicationId = applicationsManager.getNewApplication(request).getApplicationId();
    return TypeConverter.fromYarn(applicationId);
  }

  private static final String ROOT = "root";

  private GetQueueInfoRequest getQueueInfoRequest(String queueName, 
      boolean includeApplications, boolean includeChildQueues, boolean recursive) {
    GetQueueInfoRequest request = 
      recordFactory.newRecordInstance(GetQueueInfoRequest.class);
    request.setQueueName(queueName);
    request.setIncludeApplications(includeApplications);
    request.setIncludeChildQueues(includeChildQueues);
    request.setRecursive(recursive);
    return request;
    
  }
  
  public QueueInfo getQueue(String queueName) throws IOException,
  InterruptedException {
    GetQueueInfoRequest request = 
      getQueueInfoRequest(queueName, true, false, false); 
      recordFactory.newRecordInstance(GetQueueInfoRequest.class);
    return TypeConverter.fromYarn(
        applicationsManager.getQueueInfo(request).getQueueInfo(), this.conf);
  }
  
  private void getChildQueues(org.apache.hadoop.yarn.api.records.QueueInfo parent, 
      List<org.apache.hadoop.yarn.api.records.QueueInfo> queues,
      boolean recursive) {
    List<org.apache.hadoop.yarn.api.records.QueueInfo> childQueues = 
      parent.getChildQueues();

    for (org.apache.hadoop.yarn.api.records.QueueInfo child : childQueues) {
      queues.add(child);
      if(recursive) {
        getChildQueues(child, queues, recursive);
      }
    }
  }


  public QueueAclsInfo[] getQueueAclsForCurrentUser() throws IOException,
      InterruptedException {
    GetQueueUserAclsInfoRequest request = 
      recordFactory.newRecordInstance(GetQueueUserAclsInfoRequest.class);
    List<QueueUserACLInfo> userAcls = 
      applicationsManager.getQueueUserAcls(request).getUserAclsInfoList();
    return TypeConverter.fromYarnQueueUserAclsInfo(userAcls);
  }


  public QueueInfo[] getQueues() throws IOException, InterruptedException {
    List<org.apache.hadoop.yarn.api.records.QueueInfo> queues = 
      new ArrayList<org.apache.hadoop.yarn.api.records.QueueInfo>();

    org.apache.hadoop.yarn.api.records.QueueInfo rootQueue = 
      applicationsManager.getQueueInfo(
          getQueueInfoRequest(ROOT, false, true, true)).getQueueInfo();
    getChildQueues(rootQueue, queues, true);

    return TypeConverter.fromYarnQueueInfo(queues, this.conf);
  }


  public QueueInfo[] getRootQueues() throws IOException, InterruptedException {
    List<org.apache.hadoop.yarn.api.records.QueueInfo> queues = 
      new ArrayList<org.apache.hadoop.yarn.api.records.QueueInfo>();

    org.apache.hadoop.yarn.api.records.QueueInfo rootQueue = 
      applicationsManager.getQueueInfo(
          getQueueInfoRequest(ROOT, false, true, true)).getQueueInfo();
    getChildQueues(rootQueue, queues, false);

    return TypeConverter.fromYarnQueueInfo(queues, this.conf);
  }

  public QueueInfo[] getChildQueues(String parent) throws IOException,
      InterruptedException {
      List<org.apache.hadoop.yarn.api.records.QueueInfo> queues = 
          new ArrayList<org.apache.hadoop.yarn.api.records.QueueInfo>();
        
        org.apache.hadoop.yarn.api.records.QueueInfo parentQueue = 
          applicationsManager.getQueueInfo(
              getQueueInfoRequest(parent, false, true, false)).getQueueInfo();
        getChildQueues(parentQueue, queues, true);
        
        return TypeConverter.fromYarnQueueInfo(queues, this.conf);
  }

  public String getStagingAreaDir() throws IOException, InterruptedException {
//    Path path = new Path(MRJobConstants.JOB_SUBMIT_DIR);
    String user = 
      UserGroupInformation.getCurrentUser().getShortUserName();
    Path path = MRApps.getStagingAreaDir(conf, user);
    LOG.debug("getStagingAreaDir: dir=" + path);
    return path.toString();
  }


  public String getSystemDir() throws IOException, InterruptedException {
    Path sysDir = new Path(MRJobConfig.JOB_SUBMIT_DIR);
    //FileContext.getFileContext(conf).delete(sysDir, true);
    return sysDir.toString();
  }
  

  public long getTaskTrackerExpiryInterval() throws IOException,
      InterruptedException {
    return 0;
  }
  
  public void setJobPriority(JobID arg0, String arg1) throws IOException,
      InterruptedException {
    return;
  }


  public long getProtocolVersion(String arg0, long arg1) throws IOException {
    return 0;
  }

  public long renewDelegationToken(Token<DelegationTokenIdentifier> arg0)
      throws IOException, InterruptedException {
    // TODO: Implement renewDelegationToken
    LOG.warn("renewDelegationToken - Not implemented");
    return 0;
  }
  
  
  public ApplicationId submitApplication(
      ApplicationSubmissionContext appContext) 
  throws IOException {
    appContext.setApplicationId(applicationId);
    SubmitApplicationRequest request = 
        recordFactory.newRecordInstance(SubmitApplicationRequest.class);
    request.setApplicationSubmissionContext(appContext);
    applicationsManager.submitApplication(request);
    LOG.info("Submitted application " + applicationId + " to ResourceManager" +
    		" at " + rmAddress);
    return applicationId;
  }
  
  public void killApplication(ApplicationId applicationId) throws IOException {
    KillApplicationRequest request = 
        recordFactory.newRecordInstance(KillApplicationRequest.class);
    request.setApplicationId(applicationId);
    applicationsManager.forceKillApplication(request);
    LOG.info("Killing application " + applicationId);
  }


  public ApplicationReport getApplicationReport(ApplicationId appId)
      throws YarnRemoteException {
    GetApplicationReportRequest request = recordFactory
        .newRecordInstance(GetApplicationReportRequest.class);
    request.setApplicationId(appId);
    GetApplicationReportResponse response = applicationsManager
        .getApplicationReport(request);
    ApplicationReport applicationReport = response.getApplicationReport();
    return applicationReport;
  }

  public ApplicationId getApplicationId() {
    return applicationId;
  }
}
