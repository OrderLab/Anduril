/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.hadoop.yarn.service.conf;

import org.apache.hadoop.yarn.service.api.records.Configuration;

public class YarnServiceConf {

  private static final String YARN_SERVICE_PREFIX = "yarn.service.";

  // Retry settings for the ServiceClient to talk to Service AppMaster
  public static final String CLIENT_AM_RETRY_MAX_WAIT_MS = "yarn.service.client-am.retry.max-wait-ms";
  public static final String CLIENT_AM_RETRY_MAX_INTERVAL_MS = "yarn.service.client-am.retry-interval-ms";

  // Retry settings for container failures
  public static final String CONTAINER_RETRY_MAX = "yarn.service.container-failure.retry.max";
  public static final String CONTAINER_RETRY_INTERVAL = "yarn.service.container-failure.retry-interval-ms";

  public static final String AM_RESTART_MAX = "yarn.service.am-restart.max-attempts";
  public static final String AM_RESOURCE_MEM = "yarn.service.am-resource.memory";
  public static final long DEFAULT_KEY_AM_RESOURCE_MEM = 1024;

  public static final String YARN_QUEUE = "yarn.service.queue";

  public static final String API_SERVER_ADDRESS = "yarn.service.api-server.address";
  public static final String DEFAULT_API_SERVER_ADDRESS = "0.0.0.0:";
  public static final int DEFAULT_API_SERVER_PORT = 9191;

  public static final String FINAL_LOG_INCLUSION_PATTERN = "yarn.service.log.include-pattern";
  public static final String FINAL_LOG_EXCLUSION_PATTERN = "yarn.service.log.exclude-pattern";

  public static final String ROLLING_LOG_INCLUSION_PATTERN = "yarn.service.rolling-log.include-pattern";
  public static final String ROLLING_LOG_EXCLUSION_PATTERN = "yarn.service.rolling-log.exclude-pattern";


  /**
   * The yarn service base path:
   * Defaults to HomeDir/.yarn/
   */
  public static final String YARN_SERVICE_BASE_PATH = "yarn.service.base.path";

  /**
   * maximum number of failed containers (in a single component)
   * before the app exits
   */
  public static final String CONTAINER_FAILURE_THRESHOLD =
      "yarn.service.container-failure-per-component.threshold";
  /**
   * Maximum number of container failures on a node before the node is blacklisted
   */
  public static final String NODE_BLACKLIST_THRESHOLD =
      "yarn.service.node-blacklist.threshold";

  /**
   * The failure count for CONTAINER_FAILURE_THRESHOLD and NODE_BLACKLIST_THRESHOLD
   * gets reset periodically, the unit is seconds.
   */
  public static final String CONTAINER_FAILURE_WINDOW =
      "yarn.service.failure-count-reset.window";

  /**
   * interval between readiness checks.
   */
  public static final String READINESS_CHECK_INTERVAL = "yarn.service.readiness-check-interval.seconds";
  public static final int DEFAULT_READINESS_CHECK_INTERVAL = 30; // seconds

  /**
   * JVM opts.
   */
  public static final String JVM_OPTS = "yarn.service.am.java.opts";

  /**
   * How long to wait until a container is considered dead.
   */
  public static final String CONTAINER_RECOVERY_TIMEOUT_MS =
      YARN_SERVICE_PREFIX + "container-recovery.timeout.ms";

  public static final int DEFAULT_CONTAINER_RECOVERY_TIMEOUT_MS = 120000;

  /**
   * The dependency tarball file location.
   */
  public static final String DEPENDENCY_TARBALL_PATH = YARN_SERVICE_PREFIX
      + "framework.path";

  /**
   * Get long value for the property. First get from the userConf, if not
   * present, get from systemConf.
   *
   * @param name name of the property
   * @param defaultValue default value of the property, if it is not defined in
   *                     userConf and systemConf.
   * @param userConf Configuration provided by client in the JSON definition
   * @param systemConf The YarnConfiguration in the system.
   * @return long value for the property
   */
  public static long getLong(String name, long defaultValue,
      Configuration userConf, org.apache.hadoop.conf.Configuration systemConf) {
    return userConf.getPropertyLong(name, systemConf.getLong(name, defaultValue));
  }

  public static int getInt(String name, int defaultValue,
      Configuration userConf, org.apache.hadoop.conf.Configuration systemConf) {
    return userConf.getPropertyInt(name, systemConf.getInt(name, defaultValue));
  }

  public static String get(String name, String defaultVal,
      Configuration userConf, org.apache.hadoop.conf.Configuration systemConf) {
    return userConf.getProperty(name, systemConf.get(name, defaultVal));
  }
}
