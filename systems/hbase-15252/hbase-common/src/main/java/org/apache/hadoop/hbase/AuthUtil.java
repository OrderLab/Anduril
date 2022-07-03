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

package org.apache.hadoop.hbase;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.classification.InterfaceAudience;
import org.apache.hadoop.hbase.classification.InterfaceStability;
import org.apache.hadoop.hbase.security.UserProvider;
import org.apache.hadoop.hbase.util.DNS;
import org.apache.hadoop.hbase.util.Strings;
import org.apache.hadoop.security.UserGroupInformation;

/**
 * Utility methods for helping with security tasks.
 */
@InterfaceAudience.Private
@InterfaceStability.Evolving
public class AuthUtil {
  private static final Log LOG = LogFactory.getLog(AuthUtil.class);

  /** Prefix character to denote group names */
  public static final String GROUP_PREFIX = "@";

  private AuthUtil() {
    super();
  }

  /**
   * Checks if security is enabled and if so, launches chore for refreshing kerberos ticket.
   */
  public static ScheduledChore getAuthChore(Configuration conf) throws IOException {
    UserProvider userProvider = UserProvider.instantiate(conf);
    // login the principal (if using secure Hadoop)
    boolean securityEnabled =
        userProvider.isHadoopSecurityEnabled() && userProvider.isHBaseSecurityEnabled();
    if (!securityEnabled) return null;
    String host = null;
    try {
      host = Strings.domainNamePointerToHostName(DNS.getDefaultHost(
          conf.get("hbase.client.dns.interface", "default"),
          conf.get("hbase.client.dns.nameserver", "default")));
      userProvider.login("hbase.client.keytab.file", "hbase.client.kerberos.principal", host);
    } catch (UnknownHostException e) {
      LOG.error("Error resolving host name: " + e.getMessage(), e);
      throw e;
    } catch (IOException e) {
      LOG.error("Error while trying to perform the initial login: " + e.getMessage(), e);
      throw e;
    }

    final UserGroupInformation ugi = userProvider.getCurrent().getUGI();
    Stoppable stoppable = new Stoppable() {
      private volatile boolean isStopped = false;

      @Override
      public void stop(String why) {
        isStopped = true;
      }

      @Override
      public boolean isStopped() {
        return isStopped;
      }
    };

    // if you're in debug mode this is useful to avoid getting spammed by the getTGT()
    // you can increase this, keeping in mind that the default refresh window is 0.8
    // e.g. 5min tgt * 0.8 = 4min refresh so interval is better be way less than 1min
    final int CHECK_TGT_INTERVAL = 30 * 1000; // 30sec

    ScheduledChore refreshCredentials =
        new ScheduledChore("RefreshCredentials", stoppable, CHECK_TGT_INTERVAL) {
      @Override
      protected void chore() {
        try {
          ugi.checkTGTAndReloginFromKeytab();
        } catch (IOException e) {
          LOG.error("Got exception while trying to refresh credentials: " + e.getMessage(), e);
        }
      }
    };

    return refreshCredentials;
  }

  /**
   * Returns whether or not the given name should be interpreted as a group
   * principal.  Currently this simply checks if the name starts with the
   * special group prefix character ("@").
   */
  public static boolean isGroupPrincipal(String name) {
    return name != null && name.startsWith(GROUP_PREFIX);
  }

  /**
   * Returns the actual name for a group principal (stripped of the
   * group prefix).
   */
  public static String getGroupName(String aclKey) {
    if (!isGroupPrincipal(aclKey)) {
      return aclKey;
    }

    return aclKey.substring(GROUP_PREFIX.length());
  }

  /**
   * Returns the group entry with the group prefix for a group principal.
   */
  public static String toGroupEntry(String name) {
    return GROUP_PREFIX + name;
  }
}
