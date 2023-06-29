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
package org.apache.hadoop.hbase.util;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.testclassification.MediumTests;
import org.apache.hadoop.hbase.testclassification.MiscTests;
import org.apache.hadoop.hbase.util.RegionSplitter.DecimalStringSplit;
import org.apache.hadoop.hbase.util.RegionSplitter.HexStringSplit;
import org.apache.hadoop.hbase.util.RegionSplitter.SplitAlgorithm;
import org.apache.hadoop.hbase.util.RegionSplitter.UniformSplit;
import org.apache.hadoop.hbase.zookeeper.MiniZooKeeperCluster;
import org.apache.hbase.thirdparty.com.google.common.io.Closeables;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * Tests for {@link RegionSplitter}, which can create a pre-split table or do a
 * rolling split of an existing table.
 */
@Category({MiscTests.class, MediumTests.class})
public class TestRegionSplitter {

  @ClassRule
  public static final HBaseClassTestRule CLASS_RULE =
      HBaseClassTestRule.forClass(TestRegionSplitter.class);

    private final static Logger LOG = LoggerFactory.getLogger(TestRegionSplitter.class);
    private final static HBaseTestingUtility UTIL = new HBaseTestingUtility();
    private final static String CF_NAME = "SPLIT_TEST_CF";
    private final static byte xFF = (byte) 0xff;

    @Rule
    public TestName name = new TestName();

    @BeforeClass
    public static void setup() throws Exception {
        UTIL.startMiniZKCluster();
        MiniZooKeeperCluster miniZK = UTIL.getZkCluster();
        UTIL.startMiniCluster(2);
    }

    @AfterClass
    public static void teardown() throws Exception {
        UTIL.shutdownMiniCluster();
    }

    public static final byte[][] KEYS_FOR_HBA_CREATE_TABLE = { Bytes.toBytes("bbb"),
            Bytes.toBytes("ccc"), Bytes.toBytes("ddd"), Bytes.toBytes("eee"), Bytes.toBytes("fff"),
            Bytes.toBytes("ggg"), Bytes.toBytes("hhh"), Bytes.toBytes("iii"), Bytes.toBytes("jjj"),
            Bytes.toBytes("kkk"), Bytes.toBytes("lll"), Bytes.toBytes("mmm"), Bytes.toBytes("nnn"),
            Bytes.toBytes("ooo"), Bytes.toBytes("ppp"), Bytes.toBytes("qqq"), Bytes.toBytes("rrr"),
            Bytes.toBytes("sss"), Bytes.toBytes("ttt"), Bytes.toBytes("uuu"), Bytes.toBytes("vvv"),
            Bytes.toBytes("www"), Bytes.toBytes("xxx"), Bytes.toBytes("yyy"), Bytes.toBytes("zzz") };

    @Test
    public void testSplittingLog() throws Exception {
        Connection connection1 = ConnectionFactory.createConnection(UTIL.getConfiguration());
        Admin hbaseAdmin = connection1.getAdmin();
        // Create the table
        final TableName tableName = TableName.valueOf("test");
        final byte[] famName = Bytes.toBytes("f");
        final byte[] noRepfamName = Bytes.toBytes("norep");
        TableDescriptor table = TableDescriptorBuilder.newBuilder(tableName)
                .setColumnFamily(ColumnFamilyDescriptorBuilder.newBuilder(famName).setMaxVersions(100)
                        .setScope(HConstants.REPLICATION_SCOPE_GLOBAL).build())
                .setColumnFamily(ColumnFamilyDescriptorBuilder.of(noRepfamName)).build();
        UTIL.createTable(table, KEYS_FOR_HBA_CREATE_TABLE);
        Table htable1 = UTIL.getConnection().getTable(tableName);
        // Put some data
        final byte[] row = Bytes.toBytes("row");
        //Put put1 = new Put(row);
        //put1.addColumn(famName, row, row);

        //htable1.put(put1);

        //Put put2 = new Put(Bytes.toBytes(row+"0"));
        //put2.addColumn(famName, row, row);
        //htable1.put(put2);
        int batch_size = 100;

        for (int i = 0; i < batch_size; i++) {
            Put put = new Put(Bytes.toBytes("" + i));
            put.addColumn(famName, row, row);
            htable1.put(put);
        }

        Scan scan1 = new Scan();

        ResultScanner scanner1 = htable1.getScanner(scan1);
        Result[] res1 = scanner1.next(batch_size);
        scanner1.close();
        assertEquals(batch_size, res1.length);

        // ROll the log writer


        // Restart to trigger SplitLog
        LOG.info("Restarting....");
        //Closeables.close(hbaseAdmin, true);
        //Closeables.close(htable1, true);
        UTIL.getMiniHBaseCluster().abortRegionServer(0);
        //UTIL.shutdownMiniHBaseCluster();
        //UTIL.restartHBaseCluster(2);
        //hbaseAdmin = UTIL.getAdmin();
        //connection1 = UTIL.getConnection();
        //htable1 = connection1.getTable(tableName);

        Thread.sleep(10000);
        //UTIL.getMiniHBaseCluster().abortMaster(0);
        //UTIL.getMiniHBaseCluster().startMaster();

        Scan scan2 = new Scan();
        ResultScanner scanner2 = htable1.getScanner(scan2);
        Result[] res2 = scanner2.next(batch_size);
        scanner2.close();
        assertEquals(batch_size, res2.length);
        /**
        Get get = new Get(row);
        for (int i = 0; i < 50; i++) {
            if (i == 50 - 1) {
                fail("Waited too much time for put replication");
            }
            Result res = htable1.get(get);
            if (res.isEmpty()) {
                LOG.info("Row not available");
                Thread.sleep(500);
            } else {
                assertArrayEquals(row, res.value());
                break;
            }
        }
         **/

    }



    /**
     * Test creating a pre-split table using the HexStringSplit algorithm.
     */
    //@Test
    public void testCreatePresplitTableHex() throws Exception {
      final List<byte[]> expectedBounds = new ArrayList<>(17);
      expectedBounds.add(ArrayUtils.EMPTY_BYTE_ARRAY);
      expectedBounds.add("10000000".getBytes());
      expectedBounds.add("20000000".getBytes());
      expectedBounds.add("30000000".getBytes());
      expectedBounds.add("40000000".getBytes());
      expectedBounds.add("50000000".getBytes());
      expectedBounds.add("60000000".getBytes());
      expectedBounds.add("70000000".getBytes());
      expectedBounds.add("80000000".getBytes());
      expectedBounds.add("90000000".getBytes());
      expectedBounds.add("a0000000".getBytes());
      expectedBounds.add("b0000000".getBytes());
      expectedBounds.add("c0000000".getBytes());
      expectedBounds.add("d0000000".getBytes());
      expectedBounds.add("e0000000".getBytes());
      expectedBounds.add("f0000000".getBytes());
          expectedBounds.add(ArrayUtils.EMPTY_BYTE_ARRAY);

          // Do table creation/pre-splitting and verification of region boundaries
    preSplitTableAndVerify(expectedBounds,
        HexStringSplit.class.getSimpleName(),
        TableName.valueOf(name.getMethodName()));
    }

    /**
     * Test creating a pre-split table using the UniformSplit algorithm.
     */
    //@Test
    public void testCreatePresplitTableUniform() throws Exception {
      List<byte[]> expectedBounds = new ArrayList<>(17);
      expectedBounds.add(ArrayUtils.EMPTY_BYTE_ARRAY);
      expectedBounds.add(new byte[] {      0x10, 0, 0, 0, 0, 0, 0, 0});
      expectedBounds.add(new byte[] {      0x20, 0, 0, 0, 0, 0, 0, 0});
      expectedBounds.add(new byte[] {      0x30, 0, 0, 0, 0, 0, 0, 0});
      expectedBounds.add(new byte[] {      0x40, 0, 0, 0, 0, 0, 0, 0});
      expectedBounds.add(new byte[] { 0x50, 0, 0, 0, 0, 0, 0, 0 });
      expectedBounds.add(new byte[] { 0x60, 0, 0, 0, 0, 0, 0, 0 });
      expectedBounds.add(new byte[] { 0x70, 0, 0, 0, 0, 0, 0, 0 });
      expectedBounds.add(new byte[] { (byte) 0x80, 0, 0, 0, 0, 0, 0, 0 });
      expectedBounds.add(new byte[] { (byte) 0x90, 0, 0, 0, 0, 0, 0, 0 });
      expectedBounds.add(new byte[] {(byte)0xa0, 0, 0, 0, 0, 0, 0, 0});
      expectedBounds.add(new byte[] { (byte) 0xb0, 0, 0, 0, 0, 0, 0, 0 });
      expectedBounds.add(new byte[] { (byte) 0xc0, 0, 0, 0, 0, 0, 0, 0 });
      expectedBounds.add(new byte[] { (byte) 0xd0, 0, 0, 0, 0, 0, 0, 0 });
      expectedBounds.add(new byte[] {(byte)0xe0, 0, 0, 0, 0, 0, 0, 0});
      expectedBounds.add(new byte[] { (byte) 0xf0, 0, 0, 0, 0, 0, 0, 0 });
      expectedBounds.add(ArrayUtils.EMPTY_BYTE_ARRAY);

      // Do table creation/pre-splitting and verification of region boundaries
      preSplitTableAndVerify(expectedBounds, UniformSplit.class.getSimpleName(),
          TableName.valueOf(name.getMethodName()));
    }

    /**
     * Unit tests for the HexStringSplit algorithm. Makes sure it divides up the
     * space of keys in the way that we expect.
     */
    //@Test
    public void unitTestHexStringSplit() {
        HexStringSplit splitter = new HexStringSplit();
        // Check splitting while starting from scratch

        byte[][] twoRegionsSplits = splitter.split(2);
        assertEquals(1, twoRegionsSplits.length);
        assertArrayEquals("80000000".getBytes(), twoRegionsSplits[0]);

        byte[][] threeRegionsSplits = splitter.split(3);
        assertEquals(2, threeRegionsSplits.length);
        byte[] expectedSplit0 = "55555555".getBytes();
        assertArrayEquals(expectedSplit0, threeRegionsSplits[0]);
        byte[] expectedSplit1 = "aaaaaaaa".getBytes();
        assertArrayEquals(expectedSplit1, threeRegionsSplits[1]);

        // Check splitting existing regions that have start and end points
        byte[] splitPoint = splitter.split("10000000".getBytes(), "30000000".getBytes());
        assertArrayEquals("20000000".getBytes(), splitPoint);

        byte[] lastRow = "ffffffff".getBytes();
        assertArrayEquals(lastRow, splitter.lastRow());
        byte[] firstRow = "00000000".getBytes();
        assertArrayEquals(firstRow, splitter.firstRow());

        // Halfway between 00... and 20... should be 10...
        splitPoint = splitter.split(firstRow, "20000000".getBytes());
        assertArrayEquals("10000000".getBytes(), splitPoint);

        // Halfway between df... and ff... should be ef....
        splitPoint = splitter.split("dfffffff".getBytes(), lastRow);
        assertArrayEquals("efffffff".getBytes(), splitPoint);

        // Check splitting region with multiple mappers per region
        byte[][] splits = splitter.split("00000000".getBytes(), "30000000".getBytes(), 3, false);
        assertEquals(2, splits.length);
        assertArrayEquals("10000000".getBytes(), splits[0]);
        assertArrayEquals("20000000".getBytes(), splits[1]);

        splits = splitter.split("00000000".getBytes(), "20000000".getBytes(), 2, true);
        assertEquals(3, splits.length);
        assertArrayEquals("10000000".getBytes(), splits[1]);
    }

    /**
     * Unit tests for the DecimalStringSplit algorithm. Makes sure it divides up the
     * space of keys in the way that we expect.
     */
    //@Test
    public void unitTestDecimalStringSplit() {
        DecimalStringSplit splitter = new DecimalStringSplit();
        // Check splitting while starting from scratch

        byte[][] twoRegionsSplits = splitter.split(2);
        assertEquals(1, twoRegionsSplits.length);
        assertArrayEquals("50000000".getBytes(), twoRegionsSplits[0]);

        byte[][] threeRegionsSplits = splitter.split(3);
        assertEquals(2, threeRegionsSplits.length);
        byte[] expectedSplit0 = "33333333".getBytes();
        assertArrayEquals(expectedSplit0, threeRegionsSplits[0]);
        byte[] expectedSplit1 = "66666666".getBytes();
        assertArrayEquals(expectedSplit1, threeRegionsSplits[1]);

        // Check splitting existing regions that have start and end points
        byte[] splitPoint = splitter.split("10000000".getBytes(), "30000000".getBytes());
        assertArrayEquals("20000000".getBytes(), splitPoint);

        byte[] lastRow = "99999999".getBytes();
        assertArrayEquals(lastRow, splitter.lastRow());
        byte[] firstRow = "00000000".getBytes();
        assertArrayEquals(firstRow, splitter.firstRow());

        // Halfway between 00... and 20... should be 10...
        splitPoint = splitter.split(firstRow, "20000000".getBytes());
        assertArrayEquals("10000000".getBytes(), splitPoint);

        // Halfway between 00... and 19... should be 09...
        splitPoint = splitter.split(firstRow, "19999999".getBytes());
        assertArrayEquals("09999999".getBytes(), splitPoint);

        // Halfway between 79... and 99... should be 89....
        splitPoint = splitter.split("79999999".getBytes(), lastRow);
        assertArrayEquals("89999999".getBytes(), splitPoint);

        // Check splitting region with multiple mappers per region
        byte[][] splits = splitter.split("00000000".getBytes(), "30000000".getBytes(), 3, false);
        assertEquals(2, splits.length);
        assertArrayEquals("10000000".getBytes(), splits[0]);
        assertArrayEquals("20000000".getBytes(), splits[1]);

        splits = splitter.split("00000000".getBytes(), "20000000".getBytes(), 2, true);
        assertEquals(3, splits.length);
        assertArrayEquals("10000000".getBytes(), splits[1]);
    }

    /**
     * Unit tests for the UniformSplit algorithm. Makes sure it divides up the space of
     * keys in the way that we expect.
     */
    //@Test
    public void unitTestUniformSplit() {
        UniformSplit splitter = new UniformSplit();

        // Check splitting while starting from scratch
        try {
            splitter.split(1);
            throw new AssertionError("Splitting into <2 regions should have thrown exception");
        } catch (IllegalArgumentException e) { }

        byte[][] twoRegionsSplits = splitter.split(2);
        assertEquals(1, twoRegionsSplits.length);
        assertArrayEquals(twoRegionsSplits[0], new byte[] { (byte) 0x80, 0, 0, 0, 0, 0, 0, 0 });

        byte[][] threeRegionsSplits = splitter.split(3);
        assertEquals(2, threeRegionsSplits.length);
        byte[] expectedSplit0 = new byte[] {0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55};
        assertArrayEquals(expectedSplit0, threeRegionsSplits[0]);
        byte[] expectedSplit1 = new byte[] {(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA,
                (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA};
        assertArrayEquals(expectedSplit1, threeRegionsSplits[1]);

        // Check splitting existing regions that have start and end points
        byte[] splitPoint = splitter.split(new byte[] {0x10}, new byte[] {0x30});
        assertArrayEquals(new byte[] { 0x20 }, splitPoint);

        byte[] lastRow = new byte[] {xFF, xFF, xFF, xFF, xFF, xFF, xFF, xFF};
        assertArrayEquals(lastRow, splitter.lastRow());
        byte[] firstRow = ArrayUtils.EMPTY_BYTE_ARRAY;
        assertArrayEquals(firstRow, splitter.firstRow());

        splitPoint = splitter.split(firstRow, new byte[] {0x20});
        assertArrayEquals(splitPoint, new byte[] { 0x10 });

        splitPoint = splitter.split(new byte[] {(byte)0xdf, xFF, xFF, xFF, xFF,
                xFF, xFF, xFF}, lastRow);
        assertArrayEquals(splitPoint, new byte[] { (byte) 0xef, xFF, xFF, xFF, xFF, xFF, xFF, xFF
        });

        splitPoint = splitter.split(new byte[] {'a', 'a', 'a'}, new byte[] {'a', 'a', 'b'});
        assertArrayEquals(splitPoint, new byte[] { 'a', 'a', 'a', (byte) 0x80 });

        // Check splitting region with multiple mappers per region
        byte[][] splits = splitter.split(new byte[] {'a', 'a', 'a'}, new byte[] {'a', 'a', 'd'}, 3, false);
        assertEquals(2, splits.length);
        assertArrayEquals(splits[0], new byte[]{'a', 'a', 'b'});
        assertArrayEquals(splits[1], new byte[]{'a', 'a', 'c'});

        splits = splitter.split(new byte[] {'a', 'a', 'a'}, new byte[] {'a', 'a', 'e'}, 2, true);
        assertEquals(3, splits.length);
        assertArrayEquals(splits[1], new byte[] { 'a', 'a', 'c'});
    }

  //@Test
  public void testUserInput() {
    SplitAlgorithm algo = new HexStringSplit();
    assertFalse(splitFailsPrecondition(algo)); // default settings are fine
    assertFalse(splitFailsPrecondition(algo, "00", "AA")); // custom is fine
    assertTrue(splitFailsPrecondition(algo, "AA", "00")); // range error
    assertTrue(splitFailsPrecondition(algo, "AA", "AA")); // range error
    assertFalse(splitFailsPrecondition(algo, "0", "2", 3)); // should be fine
    assertFalse(splitFailsPrecondition(algo, "0", "A", 11)); // should be fine
    assertTrue(splitFailsPrecondition(algo, "0", "A", 12)); // too granular

    algo = new DecimalStringSplit();
    assertFalse(splitFailsPrecondition(algo)); // default settings are fine
    assertFalse(splitFailsPrecondition(algo, "00", "99")); // custom is fine
    assertTrue(splitFailsPrecondition(algo, "99", "00")); // range error
    assertTrue(splitFailsPrecondition(algo, "99", "99")); // range error
    assertFalse(splitFailsPrecondition(algo, "0", "2", 3)); // should be fine
    assertFalse(splitFailsPrecondition(algo, "0", "9", 10)); // should be fine
    assertTrue(splitFailsPrecondition(algo, "0", "9", 11)); // too granular

    algo = new UniformSplit();
    assertFalse(splitFailsPrecondition(algo)); // default settings are fine
    assertFalse(splitFailsPrecondition(algo, "\\x00", "\\xAA")); // custom is fine
    assertTrue(splitFailsPrecondition(algo, "\\xAA", "\\x00")); // range error
    assertTrue(splitFailsPrecondition(algo, "\\xAA", "\\xAA")); // range error
    assertFalse(splitFailsPrecondition(algo, "\\x00", "\\x02", 3)); // should be fine
    assertFalse(splitFailsPrecondition(algo, "\\x00", "\\x0A", 11)); // should be fine
    assertFalse(splitFailsPrecondition(algo, "\\x00", "\\x0A", 12)); // should be fine
  }

  private boolean splitFailsPrecondition(SplitAlgorithm algo) {
    return splitFailsPrecondition(algo, 100);
  }

  private boolean splitFailsPrecondition(SplitAlgorithm algo, String firstRow,
      String lastRow) {
    return splitFailsPrecondition(algo, firstRow, lastRow, 100);
  }

  private boolean splitFailsPrecondition(SplitAlgorithm algo, String firstRow,
      String lastRow, int numRegions) {
    algo.setFirstRow(firstRow);
    algo.setLastRow(lastRow);
    return splitFailsPrecondition(algo, numRegions);
  }

  private boolean splitFailsPrecondition(SplitAlgorithm algo, int numRegions) {
    try {
      byte[][] s = algo.split(numRegions);
      LOG.debug("split algo = " + algo);
      if (s != null) {
        StringBuilder sb = new StringBuilder();
        for (byte[] b : s) {
          sb.append(Bytes.toStringBinary(b) + "  ");
        }
        LOG.debug(sb.toString());
      }
      return false;
    } catch (IllegalArgumentException e) {
      return true;
    } catch (IllegalStateException e) {
      return true;
    } catch (IndexOutOfBoundsException e) {
      return true;
    }
  }


    private void preSplitTableAndVerify(List<byte[]> expectedBounds,
            String splitClass, TableName tableName) throws Exception {
        final int numRegions = expectedBounds.size()-1;
        final Configuration conf = UTIL.getConfiguration();
        conf.setInt("split.count", numRegions);
        SplitAlgorithm splitAlgo = RegionSplitter.newSplitAlgoInstance(conf, splitClass);
        RegionSplitter.createPresplitTable(tableName, splitAlgo, new String[] { CF_NAME }, conf);
        verifyBounds(expectedBounds, tableName);
    }

  //@Test
  public void noopRollingSplit() throws Exception {
    final List<byte[]> expectedBounds = new ArrayList<>(1);
    expectedBounds.add(ArrayUtils.EMPTY_BYTE_ARRAY);
    rollingSplitAndVerify(TableName.valueOf(TestRegionSplitter.class.getSimpleName()),
        "UniformSplit", expectedBounds);
  }

    private void rollingSplitAndVerify(TableName tableName, String splitClass,
            List<byte[]> expectedBounds)  throws Exception {
        final Configuration conf = UTIL.getConfiguration();

        // Set this larger than the number of splits so RegionSplitter won't block
        conf.setInt("split.outstanding", 5);
        SplitAlgorithm splitAlgo = RegionSplitter.newSplitAlgoInstance(conf, splitClass);
        RegionSplitter.rollingSplit(tableName, splitAlgo, conf);
        verifyBounds(expectedBounds, tableName);
    }

    private void verifyBounds(List<byte[]> expectedBounds, TableName tableName)
            throws Exception {
      // Get region boundaries from the cluster and verify their endpoints
      final int numRegions = expectedBounds.size()-1;
      try (Table table = UTIL.getConnection().getTable(tableName);
          RegionLocator locator = UTIL.getConnection().getRegionLocator(tableName)) {
        final List<HRegionLocation> regionInfoMap = locator.getAllRegionLocations();
        assertEquals(numRegions, regionInfoMap.size());
        for (HRegionLocation entry : regionInfoMap) {
          final HRegionInfo regionInfo = entry.getRegionInfo();
          byte[] regionStart = regionInfo.getStartKey();
          byte[] regionEnd = regionInfo.getEndKey();

          // This region's start key should be one of the region boundaries
          int startBoundaryIndex = indexOfBytes(expectedBounds, regionStart);
          assertNotSame(-1, startBoundaryIndex);

          // This region's end key should be the region boundary that comes
          // after the starting boundary.
          byte[] expectedRegionEnd = expectedBounds.get(startBoundaryIndex + 1);
          assertEquals(0, Bytes.compareTo(regionEnd, expectedRegionEnd));
        }
      }
    }

    /**
     * List.indexOf() doesn't really work for a List<byte[]>, because byte[]
     * doesn't override equals(). This method checks whether a list contains
     * a given element by checking each element using the byte array
     * comparator.
     * @return the index of the first element that equals compareTo, or -1
     * if no elements are equal.
     */
    static private int indexOfBytes(List<byte[]> list,  byte[] compareTo) {
        int listIndex = 0;
        for(byte[] elem: list) {
            if(Bytes.BYTES_COMPARATOR.compare(elem, compareTo) == 0) {
                return listIndex;
            }
            listIndex++;
        }
        return -1;
    }

}

