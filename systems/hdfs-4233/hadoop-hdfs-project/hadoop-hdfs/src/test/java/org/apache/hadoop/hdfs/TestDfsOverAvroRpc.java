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
package org.apache.hadoop.hdfs;

/** Test for simple signs of life using Avro RPC.  Not an exhaustive test
 * yet, just enough to catch fundamental problems using Avro reflection to
 * infer namenode RPC protocols. */
public class TestDfsOverAvroRpc extends TestLocalDFS {

  // Commenting the test in 0.23. This can be uncommented once
  // HADOOP-7524 and HADOOP-7693 is merged into 0.23
  /*
  public void testWorkingDirectory() throws IOException {
    System.setProperty("hdfs.rpc.engine",
                       "org.apache.hadoop.ipc.AvroRpcEngine");
    super.testWorkingDirectory();
  }
  */

}
