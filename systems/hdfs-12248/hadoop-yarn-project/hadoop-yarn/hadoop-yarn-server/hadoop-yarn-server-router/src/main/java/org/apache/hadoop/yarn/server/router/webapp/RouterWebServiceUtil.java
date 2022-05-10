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

package org.apache.hadoop.yarn.server.router.webapp;

import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.RMWebAppUtil;
import org.apache.hadoop.yarn.webapp.BadRequestException;
import org.apache.hadoop.yarn.webapp.ForbiddenException;
import org.apache.hadoop.yarn.webapp.NotFoundException;

import com.sun.jersey.api.ConflictException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * The Router webservice util class.
 */
public final class RouterWebServiceUtil {

  private static String user = "YarnRouter";

  private static final Log LOG =
      LogFactory.getLog(RouterWebServiceUtil.class.getName());

  /** Disable constructor. */
  private RouterWebServiceUtil() {
  }

  /**
   * Creates and performs a REST call to a specific WebService.
   *
   * @param webApp the address of the remote webap
   * @param hsr the servlet request
   * @param returnType the return type of the REST call
   * @param <T> Type of return object.
   * @param method the HTTP method of the REST call
   * @param targetPath additional path to add to the webapp address
   * @param formParam the form parameters as input for a specific REST call
   * @param additionalParam the query parameters as input for a specific REST
   *          call in case the call has no servlet request
   * @return the retrieved entity from the REST call
   */
  protected static <T> T genericForward(String webApp, HttpServletRequest hsr,
      final Class<T> returnType, HTTPMethods method, String targetPath,
      Object formParam, Map<String, String[]> additionalParam) {

    UserGroupInformation callerUGI = null;

    if (hsr != null) {
      callerUGI = RMWebAppUtil.getCallerUserGroupInformation(hsr, true);
    } else {
      // user not required
      callerUGI = UserGroupInformation.createRemoteUser(user);

    }

    if (callerUGI == null) {
      LOG.error("Unable to obtain user name, user not authenticated");
      return null;
    }

    try {
      return callerUGI.doAs(new PrivilegedExceptionAction<T>() {
        @SuppressWarnings("unchecked")
        @Override
        public T run() {

          Map<String, String[]> paramMap = null;

          // We can have hsr or additionalParam. There are no case with both.
          if (hsr != null) {
            paramMap = hsr.getParameterMap();
          } else if (additionalParam != null) {
            paramMap = additionalParam;
          }

          ClientResponse response = RouterWebServiceUtil.invokeRMWebService(
              webApp, targetPath, method,
              (hsr == null) ? null : hsr.getPathInfo(), paramMap, formParam);
          if (Response.class.equals(returnType)) {
            return (T) RouterWebServiceUtil.clientResponseToResponse(response);
          }
          // YARN RM can answer with Status.OK or it throws an exception
          if (response.getStatus() == 200) {
            return response.getEntity(returnType);
          }
          RouterWebServiceUtil.retrieveException(response);
          return null;
        }
      });
    } catch (InterruptedException e) {
      return null;
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Performs an invocation of a REST call on a remote RMWebService.
   *
   * @param additionalParam
   */
  private static ClientResponse invokeRMWebService(String webApp, String path,
      HTTPMethods method, String additionalPath,
      Map<String, String[]> queryParams, Object formParam) {
    Client client = Client.create();

    WebResource webResource = client.resource(webApp).path(path);

    if (additionalPath != null && !additionalPath.isEmpty()) {
      webResource = webResource.path(additionalPath);
    }

    if (queryParams != null && !queryParams.isEmpty()) {
      MultivaluedMap<String, String> paramMap = new MultivaluedMapImpl();

      for (Entry<String, String[]> param : queryParams.entrySet()) {
        String[] values = param.getValue();
        for (int i = 0; i < values.length; i++) {
          paramMap.add(param.getKey(), values[i]);
        }
      }
      webResource = webResource.queryParams(paramMap);
    }

    // I can forward the call in JSON or XML since the Router will convert it
    // again in Object before send it back to the client
    Builder builder = null;
    if (formParam != null) {
      builder = webResource.entity(formParam, MediaType.APPLICATION_XML);
      builder = builder.accept(MediaType.APPLICATION_XML);
    } else {
      builder = webResource.accept(MediaType.APPLICATION_XML);
    }

    ClientResponse response = null;

    switch (method) {
    case DELETE:
      response = builder.delete(ClientResponse.class);
      break;
    case GET:
      response = builder.get(ClientResponse.class);
      break;
    case POST:
      response = builder.post(ClientResponse.class);
      break;
    case PUT:
      response = builder.put(ClientResponse.class);
      break;
    default:
      break;
    }

    return response;
  }

  public static Response clientResponseToResponse(ClientResponse r) {
    if (r == null) {
      return null;
    }
    // copy the status code
    ResponseBuilder rb = Response.status(r.getStatus());
    // copy all the headers
    for (Entry<String, List<String>> entry : r.getHeaders().entrySet()) {
      for (String value : entry.getValue()) {
        rb.header(entry.getKey(), value);
      }
    }
    // copy the entity
    rb.entity(r.getEntityInputStream());
    // return the response
    return rb.build();
  }

  public static void retrieveException(ClientResponse response) {
    String serverErrorMsg = response.getEntity(String.class);
    int status = response.getStatus();
    if (status == 400) {
      throw new BadRequestException(serverErrorMsg);
    }
    if (status == 403) {
      throw new ForbiddenException(serverErrorMsg);
    }
    if (status == 404) {
      throw new NotFoundException(serverErrorMsg);
    }
    if (status == 409) {
      throw new ConflictException(serverErrorMsg);
    }

  }

}