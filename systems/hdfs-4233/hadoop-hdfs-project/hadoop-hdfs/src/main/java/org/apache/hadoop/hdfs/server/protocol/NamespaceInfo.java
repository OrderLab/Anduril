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

package org.apache.hadoop.hdfs.server.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.hdfs.protocol.HdfsConstants;
import org.apache.hadoop.hdfs.server.common.Storage;
import org.apache.hadoop.hdfs.server.common.StorageInfo;
import org.apache.hadoop.hdfs.server.namenode.NNStorage;
import org.apache.hadoop.hdfs.DeprecatedUTF8;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableFactories;
import org.apache.hadoop.io.WritableFactory;
import org.apache.hadoop.io.WritableUtils;

/**
 * NamespaceInfo is returned by the name-node in reply 
 * to a data-node handshake.
 * 
 */
@InterfaceAudience.Private
@InterfaceStability.Evolving
public class NamespaceInfo extends StorageInfo {
  String  buildVersion;
  int distributedUpgradeVersion;
  String blockPoolID = "";    // id of the block pool

  public NamespaceInfo() {
    super();
    buildVersion = null;
  }
  
  public NamespaceInfo(int nsID, String clusterID, String bpID, 
      long cT, int duVersion) {
    super(HdfsConstants.LAYOUT_VERSION, nsID, clusterID, cT);
    blockPoolID = bpID;
    buildVersion = Storage.getBuildVersion();
    this.distributedUpgradeVersion = duVersion;
  }
  
  public String getBuildVersion() {
    return buildVersion;
  }

  public int getDistributedUpgradeVersion() {
    return distributedUpgradeVersion;
  }
  
  public String getBlockPoolID() {
    return blockPoolID;
  }

  /////////////////////////////////////////////////
  // Writable
  /////////////////////////////////////////////////
  static {                                      // register a ctor
    WritableFactories.setFactory
      (NamespaceInfo.class,
       new WritableFactory() {
         public Writable newInstance() { return new NamespaceInfo(); }
       });
  }

  public void write(DataOutput out) throws IOException {
    DeprecatedUTF8.writeString(out, getBuildVersion());
    super.write(out);
    out.writeInt(getDistributedUpgradeVersion());
    WritableUtils.writeString(out, blockPoolID);
  }

  public void readFields(DataInput in) throws IOException {
    buildVersion = DeprecatedUTF8.readString(in);
    super.readFields(in);
    distributedUpgradeVersion = in.readInt();
    blockPoolID = WritableUtils.readString(in);
  }
  
  public String toString(){
    return super.toString() + ";bpid=" + blockPoolID;
  }

  public void validateStorage(NNStorage storage) throws IOException {
    if (layoutVersion != storage.getLayoutVersion() ||
        namespaceID != storage.getNamespaceID() ||
        cTime != storage.cTime ||
        !clusterID.equals(storage.getClusterID()) ||
        !blockPoolID.equals(storage.getBlockPoolID())) {
      throw new IOException("Inconsistent namespace information:\n" +
          "NamespaceInfo has:\n" +
          "LV=" + layoutVersion + ";" +
          "NS=" + namespaceID + ";" +
          "cTime=" + cTime + ";" +
          "CID=" + clusterID + ";" +
          "BPID=" + blockPoolID +
          ".\nStorage has:\n" +
          "LV=" + storage.getLayoutVersion() + ";" +
          "NS=" + storage.getNamespaceID() + ";" +
          "cTime=" + storage.getCTime() + ";" +
          "CID=" + storage.getClusterID() + ";" +
          "BPID=" + storage.getBlockPoolID() + ".");
    }
  }
}
