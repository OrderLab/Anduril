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

package org.apache.hadoop.hdfs.server.datanode;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.hdfs.protocol.Block;
import org.apache.hadoop.hdfs.protocol.ExtendedBlock;
import org.apache.hadoop.hdfs.server.protocol.DatanodeRegistration;

/**
 * Utility class for accessing package-private DataNode information during tests.
 *
 */
public class DataNodeTestUtils {
  public static DatanodeRegistration 
  getDNRegistrationByMachineName(DataNode dn, String mName) {
    return dn.getDNRegistrationByMachineName(mName);
  }
  
  public static void triggerHeartbeat(DataNode dn) throws IOException {
    for (BPOfferService bpos : dn.getAllBpOs()) {
      bpos.triggerHeartbeatForTests();
    } 
  }
  
  public static DatanodeRegistration 
  getDNRegistrationForBP(DataNode dn, String bpid) throws IOException {
    return dn.getDNRegistrationForBP(bpid);
  }

  public static File getFile(DataNode dn, String bpid, long bid) {
    return ((FSDataset)dn.getFSDataset()).getFile(bpid, bid);
  }

  public static File getBlockFile(DataNode dn, String bpid, Block b
      ) throws IOException {
    return ((FSDataset)dn.getFSDataset()).getBlockFile(bpid, b);
  }

  public static boolean unlinkBlock(DataNode dn, ExtendedBlock block, int numLinks
      ) throws IOException {
    ReplicaInfo info = ((FSDataset)dn.getFSDataset()).getReplicaInfo(block);
    return info.unlinkBlock(numLinks);
  }
}
