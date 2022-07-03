/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hbase.spark

import org.apache.hadoop.hbase.client.{Put, ConnectionFactory}
import org.apache.hadoop.hbase.spark.datasources.HBaseSparkConf
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{TableNotFoundException, TableName, HBaseTestingUtility}
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.{SparkConf, SparkContext, Logging}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}

class DefaultSourceSuite extends FunSuite with
BeforeAndAfterEach with BeforeAndAfterAll with Logging {
  @transient var sc: SparkContext = null
  var TEST_UTIL: HBaseTestingUtility = new HBaseTestingUtility

  val t1TableName = "t1"
  val t2TableName = "t2"
  val columnFamily = "c"

  var sqlContext:SQLContext = null
  var df:DataFrame = null

  override def beforeAll() {

    TEST_UTIL.startMiniCluster

    logInfo(" - minicluster started")
    try
      TEST_UTIL.deleteTable(TableName.valueOf(t1TableName))
    catch {
      case e: Exception => logInfo(" - no table " + t1TableName + " found")
    }
    try
      TEST_UTIL.deleteTable(TableName.valueOf(t2TableName))
    catch {
      case e: Exception => logInfo(" - no table " + t2TableName + " found")
    }
    logInfo(" - creating table " + t1TableName)
    TEST_UTIL.createTable(TableName.valueOf(t1TableName), Bytes.toBytes(columnFamily))
    logInfo(" - created table")
    logInfo(" - creating table " + t2TableName)
    TEST_UTIL.createTable(TableName.valueOf(t2TableName), Bytes.toBytes(columnFamily))
    logInfo(" - created table")
    val sparkConf = new SparkConf
    sparkConf.set(HBaseSparkConf.BLOCK_CACHE_ENABLE, "true")
    sparkConf.set(HBaseSparkConf.BATCH_NUM, "100")
    sparkConf.set(HBaseSparkConf.CACHE_SIZE, "100")
    sc  = new SparkContext("local", "test", sparkConf)

    val connection = ConnectionFactory.createConnection(TEST_UTIL.getConfiguration)
    try {
      val t1Table = connection.getTable(TableName.valueOf("t1"))

      try {
        var put = new Put(Bytes.toBytes("get1"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("a"), Bytes.toBytes("foo1"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("b"), Bytes.toBytes("1"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("i"), Bytes.toBytes(1))
        t1Table.put(put)
        put = new Put(Bytes.toBytes("get2"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("a"), Bytes.toBytes("foo2"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("b"), Bytes.toBytes("4"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("i"), Bytes.toBytes(4))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("z"), Bytes.toBytes("FOO"))
        t1Table.put(put)
        put = new Put(Bytes.toBytes("get3"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("a"), Bytes.toBytes("foo3"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("b"), Bytes.toBytes("8"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("i"), Bytes.toBytes(8))
        t1Table.put(put)
        put = new Put(Bytes.toBytes("get4"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("a"), Bytes.toBytes("foo4"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("b"), Bytes.toBytes("10"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("i"), Bytes.toBytes(10))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("z"), Bytes.toBytes("BAR"))
        t1Table.put(put)
        put = new Put(Bytes.toBytes("get5"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("a"), Bytes.toBytes("foo5"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("b"), Bytes.toBytes("8"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("i"), Bytes.toBytes(8))
        t1Table.put(put)
      } finally {
        t1Table.close()
      }

      val t2Table = connection.getTable(TableName.valueOf("t2"))

      try {
        var put = new Put(Bytes.toBytes(1))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("a"), Bytes.toBytes("foo1"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("b"), Bytes.toBytes("1"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("i"), Bytes.toBytes(1))
        t2Table.put(put)
        put = new Put(Bytes.toBytes(2))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("a"), Bytes.toBytes("foo2"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("b"), Bytes.toBytes("4"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("i"), Bytes.toBytes(4))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("z"), Bytes.toBytes("FOO"))
        t2Table.put(put)
        put = new Put(Bytes.toBytes(3))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("a"), Bytes.toBytes("foo3"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("b"), Bytes.toBytes("8"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("i"), Bytes.toBytes(8))
        t2Table.put(put)
        put = new Put(Bytes.toBytes(4))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("a"), Bytes.toBytes("foo4"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("b"), Bytes.toBytes("10"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("i"), Bytes.toBytes(10))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("z"), Bytes.toBytes("BAR"))
        t2Table.put(put)
        put = new Put(Bytes.toBytes(5))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("a"), Bytes.toBytes("foo5"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("b"), Bytes.toBytes("8"))
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("i"), Bytes.toBytes(8))
        t2Table.put(put)
      } finally {
        t2Table.close()
      }
    } finally {
      connection.close()
    }

    new HBaseContext(sc, TEST_UTIL.getConfiguration)
    sqlContext = new SQLContext(sc)

    df = sqlContext.load("org.apache.hadoop.hbase.spark",
      Map("hbase.columns.mapping" ->
        "KEY_FIELD STRING :key, A_FIELD STRING c:a, B_FIELD STRING c:b,",
        "hbase.table" -> "t1"))

    df.registerTempTable("hbaseTable1")

    df = sqlContext.load("org.apache.hadoop.hbase.spark",
      Map("hbase.columns.mapping" ->
        "KEY_FIELD INT :key, A_FIELD STRING c:a, B_FIELD STRING c:b,",
        "hbase.table" -> "t2"))

    df.registerTempTable("hbaseTable2")
  }

  override def afterAll() {
    TEST_UTIL.deleteTable(TableName.valueOf(t1TableName))
    logInfo("shuting down minicluster")
    TEST_UTIL.shutdownMiniCluster()

    sc.stop()
  }

  override def beforeEach(): Unit = {
    DefaultSourceStaticUtils.lastFiveExecutionRules.clear()
  }


  /**
   * A example of query three fields and also only using rowkey points for the filter
   */
  test("Test rowKey point only rowKey query") {
    val results = sqlContext.sql("SELECT KEY_FIELD, B_FIELD, A_FIELD FROM hbaseTable1 " +
      "WHERE " +
      "(KEY_FIELD = 'get1' or KEY_FIELD = 'get2' or KEY_FIELD = 'get3')").take(10)

    val executionRules = DefaultSourceStaticUtils.lastFiveExecutionRules.poll()

    assert(results.length == 3)

    assert(executionRules.dynamicLogicExpression.toExpressionString.
      equals("( ( KEY_FIELD == 0 OR KEY_FIELD == 1 ) OR KEY_FIELD == 2 )"))

    assert(executionRules.rowKeyFilter.points.size == 3)
    assert(executionRules.rowKeyFilter.ranges.size == 0)
  }

  /**
   * A example of query three fields and also only using cell points for the filter
   */
  test("Test cell point only rowKey query") {
    val results = sqlContext.sql("SELECT KEY_FIELD, B_FIELD, A_FIELD FROM hbaseTable1 " +
      "WHERE " +
      "(B_FIELD = '4' or B_FIELD = '10' or A_FIELD = 'foo1')").take(10)

    val executionRules = DefaultSourceStaticUtils.lastFiveExecutionRules.poll()

    assert(results.length == 3)

    assert(executionRules.dynamicLogicExpression.toExpressionString.
      equals("( ( B_FIELD == 0 OR B_FIELD == 1 ) OR A_FIELD == 2 )"))
  }

  /**
   * A example of a OR merge between to ranges the result is one range
   * Also an example of less then and greater then
   */
  test("Test two range rowKey query") {
    val results = sqlContext.sql("SELECT KEY_FIELD, B_FIELD, A_FIELD FROM hbaseTable1 " +
      "WHERE " +
      "( KEY_FIELD < 'get2' or KEY_FIELD > 'get3')").take(10)

    val executionRules = DefaultSourceStaticUtils.lastFiveExecutionRules.poll()

    assert(results.length == 3)

    assert(executionRules.dynamicLogicExpression.toExpressionString.
      equals("( KEY_FIELD < 0 OR KEY_FIELD > 1 )"))

    assert(executionRules.rowKeyFilter.points.size == 0)
    assert(executionRules.rowKeyFilter.ranges.size == 2)

    val scanRange1 = executionRules.rowKeyFilter.ranges.get(0).get
    assert(Bytes.equals(scanRange1.lowerBound,Bytes.toBytes("")))
    assert(Bytes.equals(scanRange1.upperBound,Bytes.toBytes("get2")))
    assert(scanRange1.isLowerBoundEqualTo)
    assert(!scanRange1.isUpperBoundEqualTo)

    val scanRange2 = executionRules.rowKeyFilter.ranges.get(1).get
    assert(Bytes.equals(scanRange2.lowerBound,Bytes.toBytes("get3")))
    assert(scanRange2.upperBound == null)
    assert(!scanRange2.isLowerBoundEqualTo)
    assert(scanRange2.isUpperBoundEqualTo)
  }

  /**
   * A example of a OR merge between to ranges the result is one range
   * Also an example of less then and greater then
   *
   * This example makes sure the code works for a int rowKey
   */
  test("Test two range rowKey query where the rowKey is Int and there is a range over lap") {
    val results = sqlContext.sql("SELECT KEY_FIELD, B_FIELD, A_FIELD FROM hbaseTable2 " +
      "WHERE " +
      "( KEY_FIELD < 4 or KEY_FIELD > 2)").take(10)

    val executionRules = DefaultSourceStaticUtils.lastFiveExecutionRules.poll()



    assert(executionRules.dynamicLogicExpression.toExpressionString.
      equals("( KEY_FIELD < 0 OR KEY_FIELD > 1 )"))

    assert(executionRules.rowKeyFilter.points.size == 0)
    assert(executionRules.rowKeyFilter.ranges.size == 1)

    val scanRange1 = executionRules.rowKeyFilter.ranges.get(0).get
    assert(Bytes.equals(scanRange1.lowerBound,Bytes.toBytes("")))
    assert(scanRange1.upperBound == null)
    assert(scanRange1.isLowerBoundEqualTo)
    assert(scanRange1.isUpperBoundEqualTo)

    assert(results.length == 5)
  }

  /**
   * A example of a OR merge between to ranges the result is two ranges
   * Also an example of less then and greater then
   *
   * This example makes sure the code works for a int rowKey
   */
  test("Test two range rowKey query where the rowKey is Int and the ranges don't over lap") {
    val results = sqlContext.sql("SELECT KEY_FIELD, B_FIELD, A_FIELD FROM hbaseTable2 " +
      "WHERE " +
      "( KEY_FIELD < 2 or KEY_FIELD > 4)").take(10)

    val executionRules = DefaultSourceStaticUtils.lastFiveExecutionRules.poll()

    assert(executionRules.dynamicLogicExpression.toExpressionString.
      equals("( KEY_FIELD < 0 OR KEY_FIELD > 1 )"))

    assert(executionRules.rowKeyFilter.points.size == 0)

    assert(executionRules.rowKeyFilter.ranges.size == 2)

    val scanRange1 = executionRules.rowKeyFilter.ranges.get(0).get
    assert(Bytes.equals(scanRange1.lowerBound,Bytes.toBytes("")))
    assert(Bytes.equals(scanRange1.upperBound, Bytes.toBytes(2)))
    assert(scanRange1.isLowerBoundEqualTo)
    assert(!scanRange1.isUpperBoundEqualTo)

    val scanRange2 = executionRules.rowKeyFilter.ranges.get(1).get
    assert(Bytes.equals(scanRange2.lowerBound, Bytes.toBytes(4)))
    assert(scanRange2.upperBound == null)
    assert(!scanRange2.isLowerBoundEqualTo)
    assert(scanRange2.isUpperBoundEqualTo)

    assert(results.length == 2)
  }

  /**
   * A example of a AND merge between to ranges the result is one range
   * Also an example of less then and equal to and greater then and equal to
   */
  test("Test one combined range rowKey query") {
    val results = sqlContext.sql("SELECT KEY_FIELD, B_FIELD, A_FIELD FROM hbaseTable1 " +
      "WHERE " +
      "(KEY_FIELD <= 'get3' and KEY_FIELD >= 'get2')").take(10)

    val executionRules = DefaultSourceStaticUtils.lastFiveExecutionRules.poll()

    assert(results.length == 2)

    assert(executionRules.dynamicLogicExpression.toExpressionString.
      equals("( KEY_FIELD <= 0 AND KEY_FIELD >= 1 )"))

    assert(executionRules.rowKeyFilter.points.size == 0)
    assert(executionRules.rowKeyFilter.ranges.size == 1)

    val scanRange1 = executionRules.rowKeyFilter.ranges.get(0).get
    assert(Bytes.equals(scanRange1.lowerBound,Bytes.toBytes("get2")))
    assert(Bytes.equals(scanRange1.upperBound, Bytes.toBytes("get3")))
    assert(scanRange1.isLowerBoundEqualTo)
    assert(scanRange1.isUpperBoundEqualTo)

  }

  /**
   * Do a select with no filters
   */
  test("Test select only query") {

    val results = df.select("KEY_FIELD").take(10)
    assert(results.length == 5)

    val executionRules = DefaultSourceStaticUtils.lastFiveExecutionRules.poll()

    assert(executionRules.dynamicLogicExpression == null)

  }

  /**
   * A complex query with one point and one range for both the
   * rowKey and the a column
   */
  test("Test SQL point and range combo") {
    val results = sqlContext.sql("SELECT KEY_FIELD FROM hbaseTable1 " +
      "WHERE " +
      "(KEY_FIELD = 'get1' and B_FIELD < '3') or " +
      "(KEY_FIELD >= 'get3' and B_FIELD = '8')").take(5)

    val executionRules = DefaultSourceStaticUtils.lastFiveExecutionRules.poll()

    assert(executionRules.dynamicLogicExpression.toExpressionString.
      equals("( ( KEY_FIELD == 0 AND B_FIELD < 1 ) OR " +
      "( KEY_FIELD >= 2 AND B_FIELD == 3 ) )"))

    assert(executionRules.rowKeyFilter.points.size == 1)
    assert(executionRules.rowKeyFilter.ranges.size == 1)

    val scanRange1 = executionRules.rowKeyFilter.ranges.get(0).get
    assert(Bytes.equals(scanRange1.lowerBound,Bytes.toBytes("get3")))
    assert(scanRange1.upperBound == null)
    assert(scanRange1.isLowerBoundEqualTo)
    assert(scanRange1.isUpperBoundEqualTo)


    assert(results.length == 3)
  }

  /**
   * A complex query with two complex ranges that doesn't merge into one
   */
  test("Test two complete range non merge rowKey query") {

    val results = sqlContext.sql("SELECT KEY_FIELD, B_FIELD, A_FIELD FROM hbaseTable2 " +
      "WHERE " +
      "( KEY_FIELD >= 1 and KEY_FIELD <= 2) or" +
      "( KEY_FIELD > 3 and KEY_FIELD <= 5)").take(10)


    assert(results.length == 4)
    val executionRules = DefaultSourceStaticUtils.lastFiveExecutionRules.poll()
    assert(executionRules.dynamicLogicExpression.toExpressionString.
      equals("( ( KEY_FIELD >= 0 AND KEY_FIELD <= 1 ) OR " +
      "( KEY_FIELD > 2 AND KEY_FIELD <= 3 ) )"))

    assert(executionRules.rowKeyFilter.points.size == 0)
    assert(executionRules.rowKeyFilter.ranges.size == 2)

    val scanRange1 = executionRules.rowKeyFilter.ranges.get(0).get
    assert(Bytes.equals(scanRange1.lowerBound,Bytes.toBytes(1)))
    assert(Bytes.equals(scanRange1.upperBound, Bytes.toBytes(2)))
    assert(scanRange1.isLowerBoundEqualTo)
    assert(scanRange1.isUpperBoundEqualTo)

    val scanRange2 = executionRules.rowKeyFilter.ranges.get(1).get
    assert(Bytes.equals(scanRange2.lowerBound,Bytes.toBytes(3)))
    assert(Bytes.equals(scanRange2.upperBound, Bytes.toBytes(5)))
    assert(!scanRange2.isLowerBoundEqualTo)
    assert(scanRange2.isUpperBoundEqualTo)

  }

  /**
   * A complex query with two complex ranges that does merge into one
   */
  test("Test two complete range merge rowKey query") {
    val results = sqlContext.sql("SELECT KEY_FIELD, B_FIELD, A_FIELD FROM hbaseTable1 " +
      "WHERE " +
      "( KEY_FIELD >= 'get1' and KEY_FIELD <= 'get2') or" +
      "( KEY_FIELD > 'get3' and KEY_FIELD <= 'get5')").take(10)

    val executionRules = DefaultSourceStaticUtils.lastFiveExecutionRules.poll()

    assert(results.length == 4)

    assert(executionRules.dynamicLogicExpression.toExpressionString.
      equals("( ( KEY_FIELD >= 0 AND KEY_FIELD <= 1 ) OR " +
      "( KEY_FIELD > 2 AND KEY_FIELD <= 3 ) )"))

    assert(executionRules.rowKeyFilter.points.size == 0)
    assert(executionRules.rowKeyFilter.ranges.size == 2)

    val scanRange1 = executionRules.rowKeyFilter.ranges.get(0).get
    assert(Bytes.equals(scanRange1.lowerBound,Bytes.toBytes("get1")))
    assert(Bytes.equals(scanRange1.upperBound, Bytes.toBytes("get2")))
    assert(scanRange1.isLowerBoundEqualTo)
    assert(scanRange1.isUpperBoundEqualTo)

    val scanRange2 = executionRules.rowKeyFilter.ranges.get(1).get
    assert(Bytes.equals(scanRange2.lowerBound, Bytes.toBytes("get3")))
    assert(Bytes.equals(scanRange2.upperBound, Bytes.toBytes("get5")))
    assert(!scanRange2.isLowerBoundEqualTo)
    assert(scanRange2.isUpperBoundEqualTo)
  }

  test("Test OR logic with a one RowKey and One column") {

    val results = sqlContext.sql("SELECT KEY_FIELD, B_FIELD, A_FIELD FROM hbaseTable1 " +
      "WHERE " +
      "( KEY_FIELD >= 'get1' or A_FIELD <= 'foo2') or" +
      "( KEY_FIELD > 'get3' or B_FIELD <= '4')").take(10)

    val executionRules = DefaultSourceStaticUtils.lastFiveExecutionRules.poll()

    assert(results.length == 5)

    assert(executionRules.dynamicLogicExpression.toExpressionString.
      equals("( ( KEY_FIELD >= 0 OR A_FIELD <= 1 ) OR " +
      "( KEY_FIELD > 2 OR B_FIELD <= 3 ) )"))

    assert(executionRules.rowKeyFilter.points.size == 0)
    assert(executionRules.rowKeyFilter.ranges.size == 1)

    val scanRange1 = executionRules.rowKeyFilter.ranges.get(0).get
    //This is the main test for 14406
    //Because the key is joined through a or with a qualifier
    //There is no filter on the rowKey
    assert(Bytes.equals(scanRange1.lowerBound,Bytes.toBytes("")))
    assert(scanRange1.upperBound == null)
    assert(scanRange1.isLowerBoundEqualTo)
    assert(scanRange1.isUpperBoundEqualTo)
  }

  test("Test OR logic with a two columns") {
    val results = sqlContext.sql("SELECT KEY_FIELD, B_FIELD, A_FIELD FROM hbaseTable1 " +
      "WHERE " +
      "( B_FIELD > '4' or A_FIELD <= 'foo2') or" +
      "( A_FIELD > 'foo2' or B_FIELD < '4')").take(10)

    val executionRules = DefaultSourceStaticUtils.lastFiveExecutionRules.poll()

    assert(results.length == 5)

    assert(executionRules.dynamicLogicExpression.toExpressionString.
      equals("( ( B_FIELD > 0 OR A_FIELD <= 1 ) OR " +
      "( A_FIELD > 2 OR B_FIELD < 3 ) )"))

    assert(executionRules.rowKeyFilter.points.size == 0)
    assert(executionRules.rowKeyFilter.ranges.size == 1)

    val scanRange1 = executionRules.rowKeyFilter.ranges.get(0).get
    assert(Bytes.equals(scanRange1.lowerBound,Bytes.toBytes("")))
    assert(scanRange1.upperBound == null)
    assert(scanRange1.isLowerBoundEqualTo)
    assert(scanRange1.isUpperBoundEqualTo)

  }

  test("Test single RowKey Or Column logic") {
    val results = sqlContext.sql("SELECT KEY_FIELD, B_FIELD, A_FIELD FROM hbaseTable1 " +
      "WHERE " +
      "( KEY_FIELD >= 'get4' or A_FIELD <= 'foo2' )").take(10)

    val executionRules = DefaultSourceStaticUtils.lastFiveExecutionRules.poll()

    assert(results.length == 4)

    assert(executionRules.dynamicLogicExpression.toExpressionString.
      equals("( KEY_FIELD >= 0 OR A_FIELD <= 1 )"))

    assert(executionRules.rowKeyFilter.points.size == 0)
    assert(executionRules.rowKeyFilter.ranges.size == 1)

    val scanRange1 = executionRules.rowKeyFilter.ranges.get(0).get
    assert(Bytes.equals(scanRange1.lowerBound,Bytes.toBytes("")))
    assert(scanRange1.upperBound == null)
    assert(scanRange1.isLowerBoundEqualTo)
    assert(scanRange1.isUpperBoundEqualTo)
  }


  test("Test table that doesn't exist") {
    intercept[TableNotFoundException] {
      df = sqlContext.load("org.apache.hadoop.hbase.spark",
        Map("hbase.columns.mapping" ->
          "KEY_FIELD STRING :key, A_FIELD STRING c:a, B_FIELD STRING c:b,",
          "hbase.table" -> "t1NotThere"))

      df.registerTempTable("hbaseNonExistingTmp")

      sqlContext.sql("SELECT KEY_FIELD, B_FIELD, A_FIELD FROM hbaseNonExistingTmp " +
        "WHERE " +
        "( KEY_FIELD >= 'get1' and KEY_FIELD <= 'get3') or" +
        "( KEY_FIELD > 'get3' and KEY_FIELD <= 'get5')").count()
    }
    DefaultSourceStaticUtils.lastFiveExecutionRules.poll()
  }

  test("Test table with column that doesn't exist") {
    df = sqlContext.load("org.apache.hadoop.hbase.spark",
      Map("hbase.columns.mapping" ->
        "KEY_FIELD STRING :key, A_FIELD STRING c:a, B_FIELD STRING c:b, C_FIELD STRING c:c,",
        "hbase.table" -> "t1"))

    df.registerTempTable("hbaseFactColumnTmp")

    val result = sqlContext.sql("SELECT KEY_FIELD, " +
      "B_FIELD, A_FIELD FROM hbaseFactColumnTmp")

    assert(result.count() == 5)

    val executionRules = DefaultSourceStaticUtils.lastFiveExecutionRules.poll()
    assert(executionRules.dynamicLogicExpression == null)

  }

  test("Test table with INT column") {
    df = sqlContext.load("org.apache.hadoop.hbase.spark",
      Map("hbase.columns.mapping" ->
        "KEY_FIELD STRING :key, A_FIELD STRING c:a, B_FIELD STRING c:b, I_FIELD INT c:i,",
        "hbase.table" -> "t1"))

    df.registerTempTable("hbaseIntTmp")

    val result = sqlContext.sql("SELECT KEY_FIELD, B_FIELD, I_FIELD FROM hbaseIntTmp"+
    " where I_FIELD > 4 and I_FIELD < 10")

    val localResult = result.take(5)

    assert(localResult.length == 2)
    assert(localResult(0).getInt(2) == 8)

    val executionRules = DefaultSourceStaticUtils.lastFiveExecutionRules.poll()
    assert(executionRules.dynamicLogicExpression.toExpressionString.
      equals("( I_FIELD > 0 AND I_FIELD < 1 )"))

  }

  test("Test table with INT column defined at wrong type") {
    df = sqlContext.load("org.apache.hadoop.hbase.spark",
      Map("hbase.columns.mapping" ->
        "KEY_FIELD STRING :key, A_FIELD STRING c:a, B_FIELD STRING c:b, I_FIELD STRING c:i,",
        "hbase.table" -> "t1"))

    df.registerTempTable("hbaseIntWrongTypeTmp")

    val result = sqlContext.sql("SELECT KEY_FIELD, " +
      "B_FIELD, I_FIELD FROM hbaseIntWrongTypeTmp")

    val localResult = result.take(10)
    assert(localResult.length == 5)

    val executionRules = DefaultSourceStaticUtils.lastFiveExecutionRules.poll()
    assert(executionRules.dynamicLogicExpression == null)

    assert(localResult(0).getString(2).length == 4)
    assert(localResult(0).getString(2).charAt(0).toByte == 0)
    assert(localResult(0).getString(2).charAt(1).toByte == 0)
    assert(localResult(0).getString(2).charAt(2).toByte == 0)
    assert(localResult(0).getString(2).charAt(3).toByte == 1)
  }

  test("Test improperly formatted column mapping") {
    intercept[IllegalArgumentException] {
      df = sqlContext.load("org.apache.hadoop.hbase.spark",
        Map("hbase.columns.mapping" ->
          "KEY_FIELD,STRING,:key, A_FIELD,STRING,c:a, B_FIELD,STRING,c:b, I_FIELD,STRING,c:i,",
          "hbase.table" -> "t1"))

      df.registerTempTable("hbaseBadTmp")

      val result = sqlContext.sql("SELECT KEY_FIELD, " +
        "B_FIELD, I_FIELD FROM hbaseBadTmp")

      val executionRules = DefaultSourceStaticUtils.lastFiveExecutionRules.poll()
      assert(executionRules.dynamicLogicExpression == null)

      result.take(5)
    }
  }


  test("Test bad column type") {
    intercept[IllegalArgumentException] {
      df = sqlContext.load("org.apache.hadoop.hbase.spark",
        Map("hbase.columns.mapping" ->
          "KEY_FIELD FOOBAR :key, A_FIELD STRING c:a, B_FIELD STRING c:b, I_FIELD STRING c:i,",
          "hbase.table" -> "t1"))

      df.registerTempTable("hbaseIntWrongTypeTmp")

      val result = sqlContext.sql("SELECT KEY_FIELD, " +
        "B_FIELD, I_FIELD FROM hbaseIntWrongTypeTmp")

      val localResult = result.take(10)
      assert(localResult.length == 5)

      val executionRules = DefaultSourceStaticUtils.lastFiveExecutionRules.poll()
      assert(executionRules.dynamicLogicExpression == null)

    }
  }

  test("Test HBaseSparkConf matching") {
    val df = sqlContext.load("org.apache.hadoop.hbase.spark.HBaseTestSource",
      Map("cacheSize" -> "100",
        "batchNum" -> "100",
        "blockCacheingEnable" -> "true", "rowNum" -> "10"))
    assert(df.count() == 10)

    val df1 = sqlContext.load("org.apache.hadoop.hbase.spark.HBaseTestSource",
      Map("cacheSize" -> "1000",
        "batchNum" -> "100", "blockCacheingEnable" -> "true", "rowNum" -> "10"))
    intercept[Exception] {
      assert(df1.count() == 10)
    }

    val df2 = sqlContext.load("org.apache.hadoop.hbase.spark.HBaseTestSource",
      Map("cacheSize" -> "100",
        "batchNum" -> "1000", "blockCacheingEnable" -> "true", "rowNum" -> "10"))
    intercept[Exception] {
      assert(df2.count() == 10)
    }

    val df3 = sqlContext.load("org.apache.hadoop.hbase.spark.HBaseTestSource",
      Map("cacheSize" -> "100",
        "batchNum" -> "100", "blockCacheingEnable" -> "false", "rowNum" -> "10"))
    intercept[Exception] {
      assert(df3.count() == 10)
    }
  }

  test("Test table with sparse column") {
    df = sqlContext.load("org.apache.hadoop.hbase.spark",
      Map("hbase.columns.mapping" ->
        "KEY_FIELD STRING :key, A_FIELD STRING c:a, B_FIELD STRING c:b, Z_FIELD STRING c:z,",
        "hbase.table" -> "t1"))

    df.registerTempTable("hbaseZTmp")

    val result = sqlContext.sql("SELECT KEY_FIELD, B_FIELD, Z_FIELD FROM hbaseZTmp")

    val localResult = result.take(10)
    assert(localResult.length == 5)

    assert(localResult(0).getString(2) == null)
    assert(localResult(1).getString(2) == "FOO")
    assert(localResult(2).getString(2) == null)
    assert(localResult(3).getString(2) == "BAR")
    assert(localResult(4).getString(2) == null)

    val executionRules = DefaultSourceStaticUtils.lastFiveExecutionRules.poll()
    assert(executionRules.dynamicLogicExpression == null)
  }

  test("Test with column logic disabled") {
    df = sqlContext.load("org.apache.hadoop.hbase.spark",
      Map("hbase.columns.mapping" ->
        "KEY_FIELD STRING :key, A_FIELD STRING c:a, B_FIELD STRING c:b, Z_FIELD STRING c:z,",
        "hbase.table" -> "t1",
        "hbase.push.down.column.filter" -> "false"))

    df.registerTempTable("hbaseNoPushDownTmp")

    val results = sqlContext.sql("SELECT KEY_FIELD, B_FIELD, A_FIELD FROM hbaseNoPushDownTmp " +
      "WHERE " +
      "(KEY_FIELD <= 'get3' and KEY_FIELD >= 'get2')").take(10)

    val executionRules = DefaultSourceStaticUtils.lastFiveExecutionRules.poll()

    assert(results.length == 2)

    assert(executionRules.dynamicLogicExpression == null)
  }
}
