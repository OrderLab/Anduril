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
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.server.federation.policies.FederationPolicyUtils;
import org.apache.hadoop.yarn.server.federation.policies.manager.UniformBroadcastPolicyManager;
import org.apache.hadoop.yarn.server.federation.store.impl.MemoryFederationStateStore;
import org.apache.hadoop.yarn.server.federation.store.records.GetApplicationHomeSubClusterRequest;
import org.apache.hadoop.yarn.server.federation.store.records.SubClusterId;
import org.apache.hadoop.yarn.server.federation.utils.FederationStateStoreFacade;
import org.apache.hadoop.yarn.server.federation.utils.FederationStateStoreTestUtil;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.ApplicationSubmissionContextInfo;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.NewApplication;
import org.apache.hadoop.yarn.server.router.clientrm.PassThroughClientRequestInterceptor;
import org.apache.hadoop.yarn.server.router.clientrm.TestableFederationClientInterceptor;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends the {@code BaseRouterWebServicesTest} and overrides methods in order
 * to use the {@code RouterWebServices} pipeline test cases for testing the
 * {@code FederationInterceptorREST} class. The tests for
 * {@code RouterWebServices} has been written cleverly so that it can be reused
 * to validate different request interceptor chains.
 * <p>
 * It tests the case with SubClusters down and the Router logic of retries. We
 * have 1 good SubCluster and 2 bad ones for all the tests.
 */
public class TestFederationInterceptorRESTRetry
    extends BaseRouterWebServicesTest {
  private static final Logger LOG =
      LoggerFactory.getLogger(TestFederationInterceptorRESTRetry.class);
  private static final int SERVICE_UNAVAILABLE = 503;
  private static final int ACCEPTED = 202;
  private static final int OK = 200;
  // running and registered
  private static SubClusterId good;
  // registered but not running
  private static SubClusterId bad1;
  private static SubClusterId bad2;
  private static List<SubClusterId> scs = new ArrayList<SubClusterId>();
  private TestableFederationInterceptorREST interceptor;
  private MemoryFederationStateStore stateStore;
  private FederationStateStoreTestUtil stateStoreUtil;
  private String user = "test-user";

  @Override
  public void setUp() {
    super.setUpConfig();
    interceptor = new TestableFederationInterceptorREST();

    stateStore = new MemoryFederationStateStore();
    stateStore.init(this.getConf());
    FederationStateStoreFacade.getInstance().reinitialize(stateStore,
        getConf());
    stateStoreUtil = new FederationStateStoreTestUtil(stateStore);

    interceptor.setConf(this.getConf());
    interceptor.init(user);

    // Create SubClusters
    good = SubClusterId.newInstance("0");
    bad1 = SubClusterId.newInstance("1");
    bad2 = SubClusterId.newInstance("2");
    scs.add(good);
    scs.add(bad1);
    scs.add(bad2);

    // The mock RM will not start in these SubClusters, this is done to simulate
    // a SubCluster down

    interceptor.registerBadSubCluster(bad1);
    interceptor.registerBadSubCluster(bad2);
  }

  @Override
  public void tearDown() {
    interceptor.shutdown();
    super.tearDown();
  }

  private void setupCluster(List<SubClusterId> scsToRegister)
      throws YarnException {

    try {
      // Clean up the StateStore before every test
      stateStoreUtil.deregisterAllSubClusters();

      for (SubClusterId sc : scsToRegister) {
        stateStoreUtil.registerSubCluster(sc);
      }
    } catch (YarnException e) {
      LOG.error(e.getMessage());
      Assert.fail();
    }
  }

  @Override
  protected YarnConfiguration createConfiguration() {
    YarnConfiguration conf = new YarnConfiguration();
    conf.setBoolean(YarnConfiguration.FEDERATION_ENABLED, true);

    conf.set(YarnConfiguration.ROUTER_WEBAPP_DEFAULT_INTERCEPTOR_CLASS,
        MockDefaultRequestInterceptorREST.class.getName());

    String mockPassThroughInterceptorClass =
        PassThroughClientRequestInterceptor.class.getName();

    // Create a request intercepter pipeline for testing. The last one in the
    // chain is the federation intercepter that calls the mock resource manager.
    // The others in the chain will simply forward it to the next one in the
    // chain
    conf.set(YarnConfiguration.ROUTER_CLIENTRM_INTERCEPTOR_CLASS_PIPELINE,
        mockPassThroughInterceptorClass + ","
            + TestableFederationClientInterceptor.class.getName());

    conf.set(YarnConfiguration.FEDERATION_POLICY_MANAGER,
        UniformBroadcastPolicyManager.class.getName());

    // Disable StateStoreFacade cache
    conf.setInt(YarnConfiguration.FEDERATION_CACHE_TIME_TO_LIVE_SECS, 0);

    return conf;
  }

  /**
   * This test validates the correctness of GetNewApplication in case the
   * cluster is composed of only 1 bad SubCluster.
   */
  @Test
  public void testGetNewApplicationOneBadSC()
      throws YarnException, IOException, InterruptedException {

    setupCluster(Arrays.asList(bad2));

    Response response = interceptor.createNewApplication(null);
    Assert.assertEquals(SERVICE_UNAVAILABLE, response.getStatus());
    Assert.assertEquals(FederationPolicyUtils.NO_ACTIVE_SUBCLUSTER_AVAILABLE,
        response.getEntity());
  }

  /**
   * This test validates the correctness of GetNewApplication in case the
   * cluster is composed of only 2 bad SubClusters.
   */
  @Test
  public void testGetNewApplicationTwoBadSCs()
      throws YarnException, IOException, InterruptedException {
    setupCluster(Arrays.asList(bad1, bad2));

    Response response = interceptor.createNewApplication(null);
    Assert.assertEquals(SERVICE_UNAVAILABLE, response.getStatus());
    Assert.assertEquals(FederationPolicyUtils.NO_ACTIVE_SUBCLUSTER_AVAILABLE,
        response.getEntity());
  }

  /**
   * This test validates the correctness of GetNewApplication in case the
   * cluster is composed of only 1 bad SubCluster and 1 good one.
   */
  @Test
  public void testGetNewApplicationOneBadOneGood()
      throws YarnException, IOException, InterruptedException {
    System.out.println("Test getNewApplication with one bad, one good SC");
    setupCluster(Arrays.asList(good, bad2));
    Response response = interceptor.createNewApplication(null);

    Assert.assertEquals(OK, response.getStatus());

    NewApplication newApp = (NewApplication) response.getEntity();
    ApplicationId appId = ApplicationId.fromString(newApp.getApplicationId());

    Assert.assertEquals(Integer.parseInt(good.getId()),
        appId.getClusterTimestamp());
  }

  /**
   * This test validates the correctness of SubmitApplication in case the
   * cluster is composed of only 1 bad SubCluster.
   */
  @Test
  public void testSubmitApplicationOneBadSC()
      throws YarnException, IOException, InterruptedException {

    setupCluster(Arrays.asList(bad2));

    ApplicationId appId =
        ApplicationId.newInstance(System.currentTimeMillis(), 1);
    ApplicationSubmissionContextInfo context =
        new ApplicationSubmissionContextInfo();
    context.setApplicationId(appId.toString());

    Response response = interceptor.submitApplication(context, null);
    Assert.assertEquals(SERVICE_UNAVAILABLE, response.getStatus());
    Assert.assertEquals(FederationPolicyUtils.NO_ACTIVE_SUBCLUSTER_AVAILABLE,
        response.getEntity());
  }

  /**
   * This test validates the correctness of SubmitApplication in case the
   * cluster is composed of only 2 bad SubClusters.
   */
  @Test
  public void testSubmitApplicationTwoBadSCs()
      throws YarnException, IOException, InterruptedException {
    setupCluster(Arrays.asList(bad1, bad2));

    ApplicationId appId =
        ApplicationId.newInstance(System.currentTimeMillis(), 1);
    ApplicationSubmissionContextInfo context =
        new ApplicationSubmissionContextInfo();
    context.setApplicationId(appId.toString());

    Response response = interceptor.submitApplication(context, null);
    Assert.assertEquals(SERVICE_UNAVAILABLE, response.getStatus());
    Assert.assertEquals(FederationPolicyUtils.NO_ACTIVE_SUBCLUSTER_AVAILABLE,
        response.getEntity());
  }

  /**
   * This test validates the correctness of SubmitApplication in case the
   * cluster is composed of only 1 bad SubCluster and a good one.
   */
  @Test
  public void testSubmitApplicationOneBadOneGood()
      throws YarnException, IOException, InterruptedException {
    System.out.println("Test submitApplication with one bad, one good SC");
    setupCluster(Arrays.asList(good, bad2));

    ApplicationId appId =
        ApplicationId.newInstance(System.currentTimeMillis(), 1);
    ApplicationSubmissionContextInfo context =
        new ApplicationSubmissionContextInfo();
    context.setApplicationId(appId.toString());
    Response response = interceptor.submitApplication(context, null);

    Assert.assertEquals(ACCEPTED, response.getStatus());

    Assert.assertEquals(good,
        stateStore
            .getApplicationHomeSubCluster(
                GetApplicationHomeSubClusterRequest.newInstance(appId))
            .getApplicationHomeSubCluster().getHomeSubCluster());
  }

}