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
 *
 */

package org.apache.hadoop.fs.adl;

import com.squareup.okhttp.mockwebserver.MockResponse;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Time;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * This class is responsible for testing local listStatus implementation to
 * cover correct parsing of successful and error JSON response from the server.
 * Adls ListStatus functionality is in detail covered in
 * org.apache.hadoop.fs.adl.live testing package.
 */
public class TestListStatus extends AdlMockWebServer {

  private static final Logger LOG = LoggerFactory
      .getLogger(TestListStatus.class);

  @Test
  public void listStatusReturnsAsExpected() throws IOException {
    getMockServer().enqueue(new MockResponse().setResponseCode(200)
        .setBody(TestADLResponseData.getListFileStatusJSONResponse(10)));
    long startTime = Time.monotonicNow();
    FileStatus[] ls = getMockAdlFileSystem()
        .listStatus(new Path("/test1/test2"));
    long endTime = Time.monotonicNow();
    LOG.debug("Time : " + (endTime - startTime));
    Assert.assertEquals(10, ls.length);

    getMockServer().enqueue(new MockResponse().setResponseCode(200)
        .setBody(TestADLResponseData.getListFileStatusJSONResponse(200)));
    startTime = Time.monotonicNow();
    ls = getMockAdlFileSystem().listStatus(new Path("/test1/test2"));
    endTime = Time.monotonicNow();
    LOG.debug("Time : " + (endTime - startTime));
    Assert.assertEquals(200, ls.length);

    getMockServer().enqueue(new MockResponse().setResponseCode(200)
        .setBody(TestADLResponseData.getListFileStatusJSONResponse(2048)));
    startTime = Time.monotonicNow();
    ls = getMockAdlFileSystem().listStatus(new Path("/test1/test2"));
    endTime = Time.monotonicNow();
    LOG.debug("Time : " + (endTime - startTime));
    Assert.assertEquals(2048, ls.length);
  }

  @Test
  public void listStatusOnFailure() throws IOException {
    getMockServer().enqueue(new MockResponse().setResponseCode(403).setBody(
        TestADLResponseData.getErrorIllegalArgumentExceptionJSONResponse()));
    FileStatus[] ls = null;
    long startTime = Time.monotonicNow();
    try {
      ls = getMockAdlFileSystem().listStatus(new Path("/test1/test2"));
    } catch (IOException e) {
      Assert.assertTrue(e.getMessage().contains("Invalid"));
    }
    long endTime = Time.monotonicNow();
    LOG.debug("Time : " + (endTime - startTime));

    // SDK may increase number of retry attempts before error is propagated
    // to caller. Adding max 10 error responses in the queue to align with SDK.
    for (int i = 0; i < 10; ++i) {
      getMockServer().enqueue(new MockResponse().setResponseCode(500).setBody(
          TestADLResponseData.getErrorInternalServerExceptionJSONResponse()));
    }

    startTime = Time.monotonicNow();
    try {
      ls = getMockAdlFileSystem().listStatus(new Path("/test1/test2"));
    } catch (IOException e) {
      Assert.assertTrue(e.getMessage().contains("Internal Server Error"));
    }
    endTime = Time.monotonicNow();
    LOG.debug("Time : " + (endTime - startTime));
  }

  @Test
  public void listStatusAcl()
          throws URISyntaxException, IOException {
    // With ACLBIT set to true
    getMockServer().enqueue(new MockResponse().setResponseCode(200)
            .setBody(TestADLResponseData.getListFileStatusJSONResponse(true)));
    FileStatus[] ls = null;
    long startTime = Time.monotonicNow();
    ls = getMockAdlFileSystem()
            .listStatus(new Path("/test1/test2"));
    long endTime = Time.monotonicNow();
    LOG.debug("Time : " + (endTime - startTime));
    for (int i = 0; i < ls.length; i++) {
      Assert.assertTrue(ls[i].isDirectory());
      Assert.assertTrue(ls[i].hasAcl());
      Assert.assertTrue(ls[i].getPermission().getAclBit());
    }

    // With ACLBIT set to false
    ls = null;
    getMockServer().enqueue(new MockResponse().setResponseCode(200)
            .setBody(TestADLResponseData.getListFileStatusJSONResponse(false)));
    startTime = Time.monotonicNow();
    ls = getMockAdlFileSystem()
            .listStatus(new Path("/test1/test2"));
    endTime = Time.monotonicNow();
    LOG.debug("Time : " + (endTime - startTime));
    for (int i = 0; i < ls.length; i++) {
      Assert.assertTrue(ls[i].isDirectory());
      Assert.assertFalse(ls[i].hasAcl());
      Assert.assertFalse(ls[i].getPermission().getAclBit());
    }
  }
}
