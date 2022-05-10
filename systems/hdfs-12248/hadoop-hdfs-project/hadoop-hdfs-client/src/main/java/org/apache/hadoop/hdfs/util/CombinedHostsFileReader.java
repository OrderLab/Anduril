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

package org.apache.hadoop.hdfs.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.hdfs.protocol.DatanodeAdminProperties;

/**
 * Reader support for JSON based datanode configuration, an alternative
 * to the exclude/include files configuration.
 * The JSON file format is the array of elements where each element
 * in the array describes the properties of a datanode. The properties of
 * a datanode is defined in {@link DatanodeAdminProperties}. For example,
 *
 * {"hostName": "host1"}
 * {"hostName": "host2", "port": 50, "upgradeDomain": "ud0"}
 * {"hostName": "host3", "port": 0, "adminState": "DECOMMISSIONED"}
 */
@InterfaceAudience.LimitedPrivate({"HDFS"})
@InterfaceStability.Unstable
public final class CombinedHostsFileReader {
  private static final ObjectReader READER =
      new ObjectMapper().readerFor(DatanodeAdminProperties.class);
  private static final JsonFactory JSON_FACTORY = new JsonFactory();

  private CombinedHostsFileReader() {
  }

  /**
   * Deserialize a set of DatanodeAdminProperties from a json file.
   * @param hostsFile the input json file to read from.
   * @return the set of DatanodeAdminProperties
   * @throws IOException
   */
  public static Set<DatanodeAdminProperties>
      readFile(final String hostsFile) throws IOException {
    HashSet<DatanodeAdminProperties> allDNs = new HashSet<>();
    try (Reader input =
         new InputStreamReader(new FileInputStream(hostsFile), "UTF-8")) {
      Iterator<DatanodeAdminProperties> iterator =
          READER.readValues(JSON_FACTORY.createParser(input));
      while (iterator.hasNext()) {
        DatanodeAdminProperties properties = iterator.next();
        allDNs.add(properties);
      }
    }
    return allDNs;
  }
}
