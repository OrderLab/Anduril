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

package org.apache.zookeeper.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZKTestCase;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.server.SyncRequestProcessor;
import org.apache.zookeeper.server.ZKDatabase;
import org.apache.zookeeper.server.persistence.FileTxnSnapLog;
import org.apache.zookeeper.server.quorum.QuorumPeer;
import org.apache.zookeeper.server.quorum.QuorumPeer.ServerState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZkDatabaseCorruptionTest extends ZKTestCase {

    protected static final Logger LOG = LoggerFactory.getLogger(ZkDatabaseCorruptionTest.class);
    public static final long CONNECTION_TIMEOUT = ClientTest.CONNECTION_TIMEOUT;

    private final QuorumBase qb = new QuorumBase();

    @Before
    public void setUp() throws Exception {
        LOG.info("STARTING quorum {}", getClass().getName());
        qb.setUp();
    }

    @After
    public void tearDown() throws Exception {
        LOG.info("STOPPING quorum {}", getClass().getName());
    }

    private void corruptFile(File f) throws IOException {
        RandomAccessFile outFile = new RandomAccessFile(f, "rw");
        outFile.write("fail servers".getBytes());
        outFile.close();
    }

    private void corruptAllSnapshots(File snapDir) throws IOException {
        File[] listFiles = snapDir.listFiles();
        for (File f : listFiles) {
            if (f.getName().startsWith("snapshot")) {
                corruptFile(f);
            }
        }
    }

    private class NoopStringCallback implements AsyncCallback.StringCallback {

        @Override
        public void processResult(int rc, String path, Object ctx, String name) {
        }

    }

    @Test
    public void testAbsentRecentSnapshot() throws IOException {
//        ZKDatabase zkDatabase = new ZKDatabase(new FileTxnSnapLog(new File("foo"), new File("bar")) {
//            @Override
//            public File findMostRecentSnapshot() throws IOException {
//                return null;
//            }
//        });
        ZKDatabase zkDatabase = new ZKDatabase(new FileTxnSnapLog(new File("foo"), new File("bar")));
        assertEquals(97, zkDatabase.calculateTxnLogSizeLimit());
    }

}
