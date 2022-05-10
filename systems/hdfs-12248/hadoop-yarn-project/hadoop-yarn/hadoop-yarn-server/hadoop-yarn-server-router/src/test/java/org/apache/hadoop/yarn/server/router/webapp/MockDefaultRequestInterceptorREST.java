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

import org.apache.hadoop.security.authorize.AuthorizationException;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.exceptions.ApplicationNotFoundException;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.server.federation.store.records.SubClusterId;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.*;
import org.apache.hadoop.yarn.webapp.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.net.ConnectException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class mocks the RESTRequestInterceptor.
 */
public class MockDefaultRequestInterceptorREST
    extends DefaultRequestInterceptorREST {

  private static final Logger LOG =
      LoggerFactory.getLogger(MockDefaultRequestInterceptorREST.class);
  final private AtomicInteger applicationCounter = new AtomicInteger(0);
  // True if the Mock RM is running, false otherwise.
  // This property allows us to write tests for specific scenario as Yarn RM
  // down e.g. network issue, failover.
  private boolean isRunning = true;
  private HashSet<ApplicationId> applicationMap = new HashSet<>();

  private void validateRunning() throws ConnectException {
    if (!isRunning) {
      throw new ConnectException("RM is stopped");
    }
  }

  @Override
  public Response createNewApplication(HttpServletRequest hsr)
      throws AuthorizationException, IOException, InterruptedException {
    validateRunning();

    ApplicationId applicationId =
        ApplicationId.newInstance(Integer.valueOf(getSubClusterId().getId()),
            applicationCounter.incrementAndGet());
    NewApplication appId =
        new NewApplication(applicationId.toString(), new ResourceInfo());
    return Response.status(Status.OK).entity(appId).build();
  }

  @Override
  public Response submitApplication(ApplicationSubmissionContextInfo newApp,
      HttpServletRequest hsr)
      throws AuthorizationException, IOException, InterruptedException {
    validateRunning();

    ApplicationId appId = ApplicationId.fromString(newApp.getApplicationId());
    LOG.info("Application submitted: " + appId);
    applicationMap.add(appId);
    return Response.status(Status.ACCEPTED).header(HttpHeaders.LOCATION, "")
        .entity(getSubClusterId()).build();
  }

  @Override
  public AppInfo getApp(HttpServletRequest hsr, String appId,
      Set<String> unselectedFields) {
    if (!isRunning) {
      throw new RuntimeException("RM is stopped");
    }

    ApplicationId applicationId = ApplicationId.fromString(appId);
    if (!applicationMap.contains(applicationId)) {
      throw new NotFoundException("app with id: " + appId + " not found");
    }

    return new AppInfo();
  }

  @Override
  public Response updateAppState(AppState targetState, HttpServletRequest hsr,
      String appId) throws AuthorizationException, YarnException,
      InterruptedException, IOException {
    validateRunning();

    ApplicationId applicationId = ApplicationId.fromString(appId);
    if (!applicationMap.remove(applicationId)) {
      throw new ApplicationNotFoundException(
          "Trying to kill an absent application: " + appId);
    }

    if (targetState == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }

    LOG.info("Force killing application: " + appId);
    AppState ret = new AppState();
    ret.setState(targetState.toString());
    return Response.status(Status.OK).entity(ret).build();
  }

  public void setSubClusterId(int subClusterId) {
    setSubClusterId(SubClusterId.newInstance(Integer.toString(subClusterId)));
  }

  public boolean isRunning() {
    return isRunning;
  }

  public void setRunning(boolean runningMode) {
    this.isRunning = runningMode;
  }
}