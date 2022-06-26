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
package org.apache.hadoop.hbase.favored;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.ServerName;

@InterfaceAudience.Private
public interface FavoredNodesPromoter {

  /* Try and assign regions even if favored nodes are dead */
  String FAVORED_ALWAYS_ASSIGN_REGIONS = "hbase.favored.assignment.always.assign";

  void generateFavoredNodesForDaughter(List<ServerName> servers,
      HRegionInfo parent, HRegionInfo hriA, HRegionInfo hriB) throws IOException;

  void generateFavoredNodesForMergedRegion(HRegionInfo merged, HRegionInfo hriA,
      HRegionInfo hriB) throws IOException;
}
