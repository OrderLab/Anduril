/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.yarn.server.router.webapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.NotImplementedException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.authorize.AuthorizationException;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.exceptions.YarnRuntimeException;
import org.apache.hadoop.yarn.server.federation.policies.FederationPolicyUtils;
import org.apache.hadoop.yarn.server.federation.policies.RouterPolicyFacade;
import org.apache.hadoop.yarn.server.federation.policies.exceptions.FederationPolicyInitializationException;
import org.apache.hadoop.yarn.server.federation.store.records.ApplicationHomeSubCluster;
import org.apache.hadoop.yarn.server.federation.store.records.SubClusterId;
import org.apache.hadoop.yarn.server.federation.store.records.SubClusterInfo;
import org.apache.hadoop.yarn.server.federation.utils.FederationStateStoreFacade;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.RMWebAppUtil;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.ActivitiesInfo;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.AppActivitiesInfo;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.AppAttemptsInfo;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.AppInfo;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.AppPriority;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.AppQueue;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.AppState;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.AppTimeoutInfo;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.AppTimeoutsInfo;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.ApplicationStatisticsInfo;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.ApplicationSubmissionContextInfo;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.AppsInfo;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.ClusterInfo;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.ClusterMetricsInfo;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.DelegationToken;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.LabelsToNodesInfo;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.NodeInfo;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.NodeLabelsInfo;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.NodeToLabelsEntryList;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.NodeToLabelsInfo;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.NodesInfo;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.ReservationDeleteRequestInfo;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.ReservationSubmissionRequestInfo;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.ReservationUpdateRequestInfo;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.SchedulerTypeInfo;
import org.apache.hadoop.yarn.server.router.RouterMetrics;
import org.apache.hadoop.yarn.server.router.RouterServerUtil;
import org.apache.hadoop.yarn.server.webapp.dao.AppAttemptInfo;
import org.apache.hadoop.yarn.server.webapp.dao.ContainerInfo;
import org.apache.hadoop.yarn.server.webapp.dao.ContainersInfo;
import org.apache.hadoop.yarn.util.Clock;
import org.apache.hadoop.yarn.util.MonotonicClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

/**
 * Extends the {@code AbstractRESTRequestInterceptor} class and provides an
 * implementation for federation of YARN RM and scaling an application across
 * multiple YARN SubClusters. All the federation specific implementation is
 * encapsulated in this class. This is always the last intercepter in the chain.
 */
public class FederationInterceptorREST extends AbstractRESTRequestInterceptor {

  private static final Logger LOG =
      LoggerFactory.getLogger(FederationInterceptorREST.class);

  private int numSubmitRetries;
  private FederationStateStoreFacade federationFacade;
  private Random rand;
  private RouterPolicyFacade policyFacade;
  private RouterMetrics routerMetrics;
  private final Clock clock = new MonotonicClock();

  private Map<SubClusterId, DefaultRequestInterceptorREST> interceptors;

  @Override
  public void init(String user) {
    federationFacade = FederationStateStoreFacade.getInstance();
    rand = new Random(System.currentTimeMillis());

    final Configuration conf = this.getConf();

    try {
      policyFacade = new RouterPolicyFacade(conf, federationFacade,
          this.federationFacade.getSubClusterResolver(), null);
    } catch (FederationPolicyInitializationException e) {
      LOG.error(e.getMessage());
    }

    numSubmitRetries =
        conf.getInt(YarnConfiguration.ROUTER_CLIENTRM_SUBMIT_RETRY,
            YarnConfiguration.DEFAULT_ROUTER_CLIENTRM_SUBMIT_RETRY);

    interceptors = new HashMap<SubClusterId, DefaultRequestInterceptorREST>();
    routerMetrics = RouterMetrics.getMetrics();
  }

  private SubClusterId getRandomActiveSubCluster(
      Map<SubClusterId, SubClusterInfo> activeSubclusters,
      List<SubClusterId> blackListSubClusters) throws YarnException {

    if (activeSubclusters == null || activeSubclusters.size() < 1) {
      RouterServerUtil.logAndThrowException(
          FederationPolicyUtils.NO_ACTIVE_SUBCLUSTER_AVAILABLE, null);
    }
    List<SubClusterId> list = new ArrayList<>(activeSubclusters.keySet());

    FederationPolicyUtils.validateSubClusterAvailability(list,
        blackListSubClusters);

    if (blackListSubClusters != null) {

      // Remove from the active SubClusters from StateStore the blacklisted ones
      for (SubClusterId scId : blackListSubClusters) {
        list.remove(scId);
      }
    }

    return list.get(rand.nextInt(list.size()));
  }

  @VisibleForTesting
  protected DefaultRequestInterceptorREST getInterceptorForSubCluster(
      SubClusterId subClusterId) {
    if (interceptors.containsKey(subClusterId)) {
      return interceptors.get(subClusterId);
    } else {
      LOG.error("The interceptor for SubCluster " + subClusterId
          + " does not exist in the cache.");
      return null;
    }
  }

  private DefaultRequestInterceptorREST createInterceptorForSubCluster(
      SubClusterId subClusterId, String webAppAddress) {

    final Configuration conf = this.getConf();

    String interceptorClassName =
        conf.get(YarnConfiguration.ROUTER_WEBAPP_DEFAULT_INTERCEPTOR_CLASS,
            YarnConfiguration.DEFAULT_ROUTER_WEBAPP_DEFAULT_INTERCEPTOR_CLASS);
    DefaultRequestInterceptorREST interceptorInstance = null;
    try {
      Class<?> interceptorClass = conf.getClassByName(interceptorClassName);
      if (DefaultRequestInterceptorREST.class
          .isAssignableFrom(interceptorClass)) {
        interceptorInstance = (DefaultRequestInterceptorREST) ReflectionUtils
            .newInstance(interceptorClass, conf);

      } else {
        throw new YarnRuntimeException(
            "Class: " + interceptorClassName + " not instance of "
                + DefaultRequestInterceptorREST.class.getCanonicalName());
      }
    } catch (ClassNotFoundException e) {
      throw new YarnRuntimeException(
          "Could not instantiate ApplicationMasterRequestInterceptor: "
              + interceptorClassName,
          e);
    }

    interceptorInstance.setWebAppAddress(webAppAddress);
    interceptorInstance.setSubClusterId(subClusterId);
    interceptors.put(subClusterId, interceptorInstance);
    return interceptorInstance;
  }

  @VisibleForTesting
  protected DefaultRequestInterceptorREST getOrCreateInterceptorForSubCluster(
      SubClusterId subClusterId, String webAppAddress) {
    DefaultRequestInterceptorREST interceptor =
        getInterceptorForSubCluster(subClusterId);
    if (interceptor == null) {
      interceptor = createInterceptorForSubCluster(subClusterId, webAppAddress);
    }
    return interceptor;
  }

  /**
   * Yarn Router forwards every getNewApplication requests to any RM. During
   * this operation there will be no communication with the State Store. The
   * Router will forward the requests to any SubCluster. The Router will retry
   * to submit the request on #numSubmitRetries different SubClusters. The
   * SubClusters are randomly chosen from the active ones.
   * <p>
   * Possible failures and behaviors:
   * <p>
   * Client: identical behavior as {@code RMWebServices}.
   * <p>
   * Router: the Client will timeout and resubmit.
   * <p>
   * ResourceManager: the Router will timeout and contacts another RM.
   * <p>
   * StateStore: not in the execution.
   */
  @Override
  public Response createNewApplication(HttpServletRequest hsr)
      throws AuthorizationException, IOException, InterruptedException {

    long startTime = clock.getTime();

    Map<SubClusterId, SubClusterInfo> subClustersActive;
    try {
      subClustersActive = federationFacade.getSubClusters(true);
    } catch (YarnException e) {
      routerMetrics.incrAppsFailedCreated();
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(e.getLocalizedMessage()).build();
    }

    List<SubClusterId> blacklist = new ArrayList<SubClusterId>();

    for (int i = 0; i < numSubmitRetries; ++i) {

      SubClusterId subClusterId;
      try {
        subClusterId = getRandomActiveSubCluster(subClustersActive, blacklist);
      } catch (YarnException e) {
        routerMetrics.incrAppsFailedCreated();
        return Response.status(Status.SERVICE_UNAVAILABLE)
            .entity(e.getLocalizedMessage()).build();
      }

      LOG.debug(
          "getNewApplication try #" + i + " on SubCluster " + subClusterId);

      DefaultRequestInterceptorREST interceptor =
          getOrCreateInterceptorForSubCluster(subClusterId,
              subClustersActive.get(subClusterId).getRMWebServiceAddress());
      Response response = null;
      try {
        response = interceptor.createNewApplication(hsr);
      } catch (Exception e) {
        LOG.warn("Unable to create a new ApplicationId in SubCluster "
            + subClusterId.getId(), e);
      }

      if (response != null && response.getStatus() == 200) {

        long stopTime = clock.getTime();
        routerMetrics.succeededAppsCreated(stopTime - startTime);

        return response;
      } else {
        // Empty response from the ResourceManager.
        // Blacklist this subcluster for this request.
        blacklist.add(subClusterId);
      }
    }

    String errMsg = "Fail to create a new application.";
    LOG.error(errMsg);
    routerMetrics.incrAppsFailedCreated();
    return Response.status(Status.INTERNAL_SERVER_ERROR).entity(errMsg).build();
  }

  /**
   * Today, in YARN there are no checks of any applicationId submitted.
   * <p>
   * Base scenarios:
   * <p>
   * The Client submits an application to the Router. • The Router selects one
   * SubCluster to forward the request. • The Router inserts a tuple into
   * StateStore with the selected SubCluster (e.g. SC1) and the appId. • The
   * State Store replies with the selected SubCluster (e.g. SC1). • The Router
   * submits the request to the selected SubCluster.
   * <p>
   * In case of State Store failure:
   * <p>
   * The client submits an application to the Router. • The Router selects one
   * SubCluster to forward the request. • The Router inserts a tuple into State
   * Store with the selected SubCluster (e.g. SC1) and the appId. • Due to the
   * State Store down the Router times out and it will retry depending on the
   * FederationFacade settings. • The Router replies to the client with an error
   * message.
   * <p>
   * If State Store fails after inserting the tuple: identical behavior as
   * {@code RMWebServices}.
   * <p>
   * In case of Router failure:
   * <p>
   * Scenario 1 – Crash before submission to the ResourceManager
   * <p>
   * The Client submits an application to the Router. • The Router selects one
   * SubCluster to forward the request. • The Router inserts a tuple into State
   * Store with the selected SubCluster (e.g. SC1) and the appId. • The Router
   * crashes. • The Client timeouts and resubmits the application. • The Router
   * selects one SubCluster to forward the request. • The Router inserts a tuple
   * into State Store with the selected SubCluster (e.g. SC2) and the appId. •
   * Because the tuple is already inserted in the State Store, it returns the
   * previous selected SubCluster (e.g. SC1). • The Router submits the request
   * to the selected SubCluster (e.g. SC1).
   * <p>
   * Scenario 2 – Crash after submission to the ResourceManager
   * <p>
   * • The Client submits an application to the Router. • The Router selects one
   * SubCluster to forward the request. • The Router inserts a tuple into State
   * Store with the selected SubCluster (e.g. SC1) and the appId. • The Router
   * submits the request to the selected SubCluster. • The Router crashes. • The
   * Client timeouts and resubmit the application. • The Router selects one
   * SubCluster to forward the request. • The Router inserts a tuple into State
   * Store with the selected SubCluster (e.g. SC2) and the appId. • The State
   * Store replies with the selected SubCluster (e.g. SC1). • The Router submits
   * the request to the selected SubCluster (e.g. SC1). When a client re-submits
   * the same application to the same RM, it does not raise an exception and
   * replies with operation successful message.
   * <p>
   * In case of Client failure: identical behavior as {@code RMWebServices}.
   * <p>
   * In case of ResourceManager failure:
   * <p>
   * The Client submits an application to the Router. • The Router selects one
   * SubCluster to forward the request. • The Router inserts a tuple into State
   * Store with the selected SubCluster (e.g. SC1) and the appId. • The Router
   * submits the request to the selected SubCluster. • The entire SubCluster is
   * down – all the RMs in HA or the master RM is not reachable. • The Router
   * times out. • The Router selects a new SubCluster to forward the request. •
   * The Router update a tuple into State Store with the selected SubCluster
   * (e.g. SC2) and the appId. • The State Store replies with OK answer. • The
   * Router submits the request to the selected SubCluster (e.g. SC2).
   */
  @Override
  public Response submitApplication(ApplicationSubmissionContextInfo newApp,
      HttpServletRequest hsr)
      throws AuthorizationException, IOException, InterruptedException {

    long startTime = clock.getTime();

    if (newApp == null || newApp.getApplicationId() == null) {
      routerMetrics.incrAppsFailedSubmitted();
      String errMsg = "Missing ApplicationSubmissionContextInfo or "
          + "applicationSubmissionContex information.";
      return Response.status(Status.BAD_REQUEST).entity(errMsg).build();
    }

    ApplicationId applicationId = null;
    try {
      applicationId = ApplicationId.fromString(newApp.getApplicationId());
    } catch (IllegalArgumentException e) {
      routerMetrics.incrAppsFailedSubmitted();
      return Response.status(Status.BAD_REQUEST).entity(e.getLocalizedMessage())
          .build();
    }

    List<SubClusterId> blacklist = new ArrayList<SubClusterId>();

    for (int i = 0; i < numSubmitRetries; ++i) {

      ApplicationSubmissionContext context =
          RMWebAppUtil.createAppSubmissionContext(newApp, this.getConf());

      SubClusterId subClusterId = null;
      try {
        subClusterId = policyFacade.getHomeSubcluster(context, blacklist);
      } catch (YarnException e) {
        routerMetrics.incrAppsFailedSubmitted();
        return Response.status(Status.SERVICE_UNAVAILABLE)
            .entity(e.getLocalizedMessage()).build();
      }
      LOG.info("submitApplication appId" + applicationId + " try #" + i
          + " on SubCluster " + subClusterId);

      ApplicationHomeSubCluster appHomeSubCluster =
          ApplicationHomeSubCluster.newInstance(applicationId, subClusterId);

      if (i == 0) {
        try {
          // persist the mapping of applicationId and the subClusterId which has
          // been selected as its home
          subClusterId =
              federationFacade.addApplicationHomeSubCluster(appHomeSubCluster);
        } catch (YarnException e) {
          routerMetrics.incrAppsFailedSubmitted();
          String errMsg = "Unable to insert the ApplicationId " + applicationId
              + " into the FederationStateStore";
          return Response.status(Status.SERVICE_UNAVAILABLE)
              .entity(errMsg + " " + e.getLocalizedMessage()).build();
        }
      } else {
        try {
          // update the mapping of applicationId and the home subClusterId to
          // the new subClusterId we have selected
          federationFacade.updateApplicationHomeSubCluster(appHomeSubCluster);
        } catch (YarnException e) {
          String errMsg = "Unable to update the ApplicationId " + applicationId
              + " into the FederationStateStore";
          SubClusterId subClusterIdInStateStore;
          try {
            subClusterIdInStateStore =
                federationFacade.getApplicationHomeSubCluster(applicationId);
          } catch (YarnException e1) {
            routerMetrics.incrAppsFailedSubmitted();
            return Response.status(Status.SERVICE_UNAVAILABLE)
                .entity(e1.getLocalizedMessage()).build();
          }
          if (subClusterId == subClusterIdInStateStore) {
            LOG.info("Application " + applicationId
                + " already submitted on SubCluster " + subClusterId);
          } else {
            routerMetrics.incrAppsFailedSubmitted();
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(errMsg)
                .build();
          }
        }
      }

      SubClusterInfo subClusterInfo;
      try {
        subClusterInfo = federationFacade.getSubCluster(subClusterId);
      } catch (YarnException e) {
        routerMetrics.incrAppsFailedSubmitted();
        return Response.status(Status.SERVICE_UNAVAILABLE)
            .entity(e.getLocalizedMessage()).build();
      }

      Response response = null;
      try {
        response = getOrCreateInterceptorForSubCluster(subClusterId,
            subClusterInfo.getRMWebServiceAddress()).submitApplication(newApp,
                hsr);
      } catch (Exception e) {
        LOG.warn("Unable to submit the application " + applicationId
            + "to SubCluster " + subClusterId.getId(), e);
      }

      if (response != null && response.getStatus() == 202) {
        LOG.info("Application " + context.getApplicationName() + " with appId "
            + applicationId + " submitted on " + subClusterId);

        long stopTime = clock.getTime();
        routerMetrics.succeededAppsSubmitted(stopTime - startTime);

        return response;
      } else {
        // Empty response from the ResourceManager.
        // Blacklist this subcluster for this request.
        blacklist.add(subClusterId);
      }
    }

    routerMetrics.incrAppsFailedSubmitted();
    String errMsg = "Application " + newApp.getApplicationName()
        + " with appId " + applicationId + " failed to be submitted.";
    LOG.error(errMsg);
    return Response.status(Status.SERVICE_UNAVAILABLE).entity(errMsg).build();
  }

  /**
   * The Yarn Router will forward to the respective Yarn RM in which the AM is
   * running.
   * <p>
   * Possible failure:
   * <p>
   * Client: identical behavior as {@code RMWebServices}.
   * <p>
   * Router: the Client will timeout and resubmit the request.
   * <p>
   * ResourceManager: the Router will timeout and the call will fail.
   * <p>
   * State Store: the Router will timeout and it will retry depending on the
   * FederationFacade settings - if the failure happened before the select
   * operation.
   */
  @Override
  public AppInfo getApp(HttpServletRequest hsr, String appId,
      Set<String> unselectedFields) {

    long startTime = clock.getTime();

    ApplicationId applicationId = null;
    try {
      applicationId = ApplicationId.fromString(appId);
    } catch (IllegalArgumentException e) {
      routerMetrics.incrAppsFailedRetrieved();
      return null;
    }

    SubClusterInfo subClusterInfo = null;
    SubClusterId subClusterId = null;
    try {
      subClusterId =
          federationFacade.getApplicationHomeSubCluster(applicationId);
      if (subClusterId == null) {
        routerMetrics.incrAppsFailedRetrieved();
        return null;
      }
      subClusterInfo = federationFacade.getSubCluster(subClusterId);
    } catch (YarnException e) {
      routerMetrics.incrAppsFailedRetrieved();
      return null;
    }

    AppInfo response = getOrCreateInterceptorForSubCluster(subClusterId,
        subClusterInfo.getRMWebServiceAddress()).getApp(hsr, appId,
            unselectedFields);

    long stopTime = clock.getTime();
    routerMetrics.succeededAppsRetrieved(stopTime - startTime);

    return response;
  }

  /**
   * The Yarn Router will forward to the respective Yarn RM in which the AM is
   * running.
   * <p>
   * Possible failures and behaviors:
   * <p>
   * Client: identical behavior as {@code RMWebServices}.
   * <p>
   * Router: the Client will timeout and resubmit the request.
   * <p>
   * ResourceManager: the Router will timeout and the call will fail.
   * <p>
   * State Store: the Router will timeout and it will retry depending on the
   * FederationFacade settings - if the failure happened before the select
   * operation.
   */
  @Override
  public Response updateAppState(AppState targetState, HttpServletRequest hsr,
      String appId) throws AuthorizationException, YarnException,
      InterruptedException, IOException {

    long startTime = clock.getTime();

    ApplicationId applicationId = null;
    try {
      applicationId = ApplicationId.fromString(appId);
    } catch (IllegalArgumentException e) {
      routerMetrics.incrAppsFailedKilled();
      return Response.status(Status.BAD_REQUEST).entity(e.getLocalizedMessage())
          .build();
    }

    SubClusterInfo subClusterInfo = null;
    SubClusterId subClusterId = null;
    try {
      subClusterId =
          federationFacade.getApplicationHomeSubCluster(applicationId);
      subClusterInfo = federationFacade.getSubCluster(subClusterId);
    } catch (YarnException e) {
      routerMetrics.incrAppsFailedKilled();
      return Response.status(Status.BAD_REQUEST).entity(e.getLocalizedMessage())
          .build();
    }

    Response response = getOrCreateInterceptorForSubCluster(subClusterId,
        subClusterInfo.getRMWebServiceAddress()).updateAppState(targetState,
            hsr, appId);

    long stopTime = clock.getTime();
    routerMetrics.succeededAppsRetrieved(stopTime - startTime);

    return response;
  }

  @Override
  public ClusterInfo get() {
    return getClusterInfo();
  }

  @Override
  public ClusterInfo getClusterInfo() {
    throw new NotImplementedException();
  }

  @Override
  public ClusterMetricsInfo getClusterMetricsInfo() {
    throw new NotImplementedException();
  }

  @Override
  public SchedulerTypeInfo getSchedulerInfo() {
    throw new NotImplementedException();
  }

  @Override
  public String dumpSchedulerLogs(String time, HttpServletRequest hsr)
      throws IOException {
    throw new NotImplementedException();
  }

  @Override
  public NodesInfo getNodes(String states) {
    throw new NotImplementedException();
  }

  @Override
  public NodeInfo getNode(String nodeId) {
    throw new NotImplementedException();
  }

  @Override
  public ActivitiesInfo getActivities(HttpServletRequest hsr, String nodeId) {
    throw new NotImplementedException();
  }

  @Override
  public AppActivitiesInfo getAppActivities(HttpServletRequest hsr,
      String appId, String time) {
    throw new NotImplementedException();
  }

  @Override
  public ApplicationStatisticsInfo getAppStatistics(HttpServletRequest hsr,
      Set<String> stateQueries, Set<String> typeQueries) {
    throw new NotImplementedException();
  }

  @Override
  public AppsInfo getApps(HttpServletRequest hsr, String stateQuery,
      Set<String> statesQuery, String finalStatusQuery, String userQuery,
      String queueQuery, String count, String startedBegin, String startedEnd,
      String finishBegin, String finishEnd, Set<String> applicationTypes,
      Set<String> applicationTags, Set<String> unselectedFields) {
    throw new NotImplementedException();
  }

  @Override
  public AppState getAppState(HttpServletRequest hsr, String appId)
      throws AuthorizationException {
    throw new NotImplementedException();
  }

  @Override
  public NodeToLabelsInfo getNodeToLabels(HttpServletRequest hsr)
      throws IOException {
    throw new NotImplementedException();
  }

  @Override
  public LabelsToNodesInfo getLabelsToNodes(Set<String> labels)
      throws IOException {
    throw new NotImplementedException();
  }

  @Override
  public Response replaceLabelsOnNodes(NodeToLabelsEntryList newNodeToLabels,
      HttpServletRequest hsr) throws IOException {
    throw new NotImplementedException();
  }

  @Override
  public Response replaceLabelsOnNode(Set<String> newNodeLabelsName,
      HttpServletRequest hsr, String nodeId) throws Exception {
    throw new NotImplementedException();
  }

  @Override
  public NodeLabelsInfo getClusterNodeLabels(HttpServletRequest hsr)
      throws IOException {
    throw new NotImplementedException();
  }

  @Override
  public Response addToClusterNodeLabels(NodeLabelsInfo newNodeLabels,
      HttpServletRequest hsr) throws Exception {
    throw new NotImplementedException();
  }

  @Override
  public Response removeFromCluserNodeLabels(Set<String> oldNodeLabels,
      HttpServletRequest hsr) throws Exception {
    throw new NotImplementedException();
  }

  @Override
  public NodeLabelsInfo getLabelsOnNode(HttpServletRequest hsr, String nodeId)
      throws IOException {
    throw new NotImplementedException();
  }

  @Override
  public AppPriority getAppPriority(HttpServletRequest hsr, String appId)
      throws AuthorizationException {
    throw new NotImplementedException();
  }

  @Override
  public Response updateApplicationPriority(AppPriority targetPriority,
      HttpServletRequest hsr, String appId) throws AuthorizationException,
      YarnException, InterruptedException, IOException {
    throw new NotImplementedException();
  }

  @Override
  public AppQueue getAppQueue(HttpServletRequest hsr, String appId)
      throws AuthorizationException {
    throw new NotImplementedException();
  }

  @Override
  public Response updateAppQueue(AppQueue targetQueue, HttpServletRequest hsr,
      String appId) throws AuthorizationException, YarnException,
      InterruptedException, IOException {
    throw new NotImplementedException();
  }

  @Override
  public Response postDelegationToken(DelegationToken tokenData,
      HttpServletRequest hsr) throws AuthorizationException, IOException,
      InterruptedException, Exception {
    throw new NotImplementedException();
  }

  @Override
  public Response postDelegationTokenExpiration(HttpServletRequest hsr)
      throws AuthorizationException, IOException, InterruptedException,
      Exception {
    throw new NotImplementedException();
  }

  @Override
  public Response cancelDelegationToken(HttpServletRequest hsr)
      throws AuthorizationException, IOException, InterruptedException,
      Exception {
    throw new NotImplementedException();
  }

  @Override
  public Response createNewReservation(HttpServletRequest hsr)
      throws AuthorizationException, IOException, InterruptedException {
    throw new NotImplementedException();
  }

  @Override
  public Response submitReservation(ReservationSubmissionRequestInfo resContext,
      HttpServletRequest hsr)
      throws AuthorizationException, IOException, InterruptedException {
    throw new NotImplementedException();
  }

  @Override
  public Response updateReservation(ReservationUpdateRequestInfo resContext,
      HttpServletRequest hsr)
      throws AuthorizationException, IOException, InterruptedException {
    throw new NotImplementedException();
  }

  @Override
  public Response deleteReservation(ReservationDeleteRequestInfo resContext,
      HttpServletRequest hsr)
      throws AuthorizationException, IOException, InterruptedException {
    throw new NotImplementedException();
  }

  @Override
  public Response listReservation(String queue, String reservationId,
      long startTime, long endTime, boolean includeResourceAllocations,
      HttpServletRequest hsr) throws Exception {
    throw new NotImplementedException();
  }

  @Override
  public AppTimeoutInfo getAppTimeout(HttpServletRequest hsr, String appId,
      String type) throws AuthorizationException {
    throw new NotImplementedException();
  }

  @Override
  public AppTimeoutsInfo getAppTimeouts(HttpServletRequest hsr, String appId)
      throws AuthorizationException {
    throw new NotImplementedException();
  }

  @Override
  public Response updateApplicationTimeout(AppTimeoutInfo appTimeout,
      HttpServletRequest hsr, String appId) throws AuthorizationException,
      YarnException, InterruptedException, IOException {
    throw new NotImplementedException();
  }

  @Override
  public AppAttemptsInfo getAppAttempts(HttpServletRequest hsr, String appId) {
    throw new NotImplementedException();
  }

  @Override
  public AppAttemptInfo getAppAttempt(HttpServletRequest req,
      HttpServletResponse res, String appId, String appAttemptId) {
    throw new NotImplementedException();
  }

  @Override
  public ContainersInfo getContainers(HttpServletRequest req,
      HttpServletResponse res, String appId, String appAttemptId) {
    throw new NotImplementedException();
  }

  @Override
  public ContainerInfo getContainer(HttpServletRequest req,
      HttpServletResponse res, String appId, String appAttemptId,
      String containerId) {
    throw new NotImplementedException();
  }

  @Override
  public void setNextInterceptor(RESTRequestInterceptor next) {
    throw new YarnRuntimeException("setNextInterceptor is being called on "
        + "FederationInterceptorREST, which should be the last one "
        + "in the chain. Check if the interceptor pipeline configuration "
        + "is correct");
  }
}