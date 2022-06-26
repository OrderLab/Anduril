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
package org.apache.hadoop.hbase.security.visibility;

import java.io.IOException;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Table;

public class TestVisibilityLabelsOnNewVersionBehaviorTable extends TestVisibilityLabelsWithDeletes {

  protected Table createTable(HColumnDescriptor fam) throws IOException {
    fam.setNewVersionBehavior(true);
    TableName tableName = TableName.valueOf(TEST_NAME.getMethodName());
    HTableDescriptor table = new HTableDescriptor(tableName);
    table.addFamily(fam);
    TEST_UTIL.getHBaseAdmin().createTable(table);
    return TEST_UTIL.getConnection().getTable(tableName);
  }

}
