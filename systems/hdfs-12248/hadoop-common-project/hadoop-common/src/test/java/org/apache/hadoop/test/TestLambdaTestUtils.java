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

package org.apache.hadoop.test;

import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import static org.apache.hadoop.test.LambdaTestUtils.*;
import static org.apache.hadoop.test.GenericTestUtils.*;

/**
 * Test the logic in {@link LambdaTestUtils}.
 * This test suite includes Java 8 and Java 7 code; the Java 8 code exists
 * to verify that the API is easily used with Lambda expressions.
 */
public class TestLambdaTestUtils extends Assert {

  public static final int INTERVAL = 10;
  public static final int TIMEOUT = 50;
  private FixedRetryInterval retry = new FixedRetryInterval(INTERVAL);
  // counter for lambda expressions to use
  private int count;

  /**
   * Always evaluates to true.
   */
  public static final Callable<Boolean> ALWAYS_TRUE =
      new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          return true;
        }
      };

  /**
   * Always evaluates to false.
   */
  public static final Callable<Boolean> ALWAYS_FALSE =
      new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          return false;
        }
      };

  /**
   * Text in the raised FNFE.
   */
  public static final String MISSING = "not found";

  /**
   * A predicate that always throws a FileNotFoundException.
   */
  public static final Callable<Boolean> ALWAYS_FNFE =
      new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          throw new FileNotFoundException(MISSING);
        }
      };

  /**
   * reusable timeout handler.
   */
  public static final GenerateTimeout
      TIMEOUT_FAILURE_HANDLER = new GenerateTimeout();

  /**
   * Always evaluates to 3L.
   */
  public static final Callable<Long> EVAL_3L = new Callable<Long>() {
    @Override
    public Long call() throws Exception {
      return 3L;
    }
  };

  /**
   * Always raises a {@code FileNotFoundException}.
   */
  public static final Callable<Long> EVAL_FNFE = new Callable<Long>() {
    @Override
    public Long call() throws Exception {
      throw new FileNotFoundException(MISSING);
    }
  };

  /**
   * Assert the retry count is as expected.
   * @param expected expected value
   */
  protected void assertRetryCount(int expected) {
    assertEquals(retry.toString(), expected, retry.getInvocationCount());
  }

  /**
   * Assert the retry count is as expected.
   * @param minCount minimum value
   */
  protected void assertMinRetryCount(int minCount) {
    assertTrue("retry count of " + retry + " is not >= " + minCount,
        minCount <= retry.getInvocationCount());
  }

  @Test
  public void testAwaitAlwaysTrue() throws Throwable {
    await(TIMEOUT,
        ALWAYS_TRUE,
        new FixedRetryInterval(INTERVAL),
        TIMEOUT_FAILURE_HANDLER);
  }

  @Test
  public void testAwaitAlwaysFalse() throws Throwable {
    try {
      await(TIMEOUT,
          ALWAYS_FALSE,
          retry,
          TIMEOUT_FAILURE_HANDLER);
      fail("should not have got here");
    } catch (TimeoutException e) {
      assertTrue(retry.getInvocationCount() > 4);
    }
  }

  @Test
  public void testAwaitLinearRetry() throws Throwable {
    ProportionalRetryInterval linearRetry =
        new ProportionalRetryInterval(INTERVAL * 2, TIMEOUT * 2);
    try {
      await(TIMEOUT,
          ALWAYS_FALSE,
          linearRetry,
          TIMEOUT_FAILURE_HANDLER);
      fail("should not have got here");
    } catch (TimeoutException e) {
      assertEquals(linearRetry.toString(),
          2, linearRetry.getInvocationCount());
    }
  }

  @Test
  public void testAwaitFNFE() throws Throwable {
    try {
      await(TIMEOUT,
          ALWAYS_FNFE,
          retry,
          TIMEOUT_FAILURE_HANDLER);
      fail("should not have got here");
    } catch (TimeoutException e) {
      // inner clause is included
      assertTrue(retry.getInvocationCount() > 0);
      assertTrue(e.getCause() instanceof FileNotFoundException);
      assertExceptionContains(MISSING, e);
    }
  }

  @Test
  public void testRetryInterval() throws Throwable {
    ProportionalRetryInterval interval =
        new ProportionalRetryInterval(200, 1000);
    assertEquals(200, (int) interval.call());
    assertEquals(400, (int) interval.call());
    assertEquals(600, (int) interval.call());
    assertEquals(800, (int) interval.call());
    assertEquals(1000, (int) interval.call());
    assertEquals(1000, (int) interval.call());
    assertEquals(1000, (int) interval.call());
  }

  @Test
  public void testInterceptSuccess() throws Throwable {
    IOException ioe = intercept(IOException.class, ALWAYS_FNFE);
    assertExceptionContains(MISSING, ioe);
  }

  @Test
  public void testInterceptContains() throws Throwable {
    intercept(IOException.class, MISSING, ALWAYS_FNFE);
  }

  @Test
  public void testInterceptContainsWrongString() throws Throwable {
    try {
      FileNotFoundException e =
          intercept(FileNotFoundException.class, "404", ALWAYS_FNFE);
      assertNotNull(e);
      throw e;
    } catch (AssertionError expected) {
      assertExceptionContains(MISSING, expected);
    }
  }

  @Test
  public void testInterceptVoidCallable() throws Throwable {
    intercept(AssertionError.class,
        NULL_RESULT,
        new Callable<IOException>() {
          @Override
          public IOException call() throws Exception {
            return intercept(IOException.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    return null;
                  }
                });
          }
        });
  }

  @Test
  public void testEventually() throws Throwable {
    long result = eventually(TIMEOUT, EVAL_3L, retry);
    assertEquals(3, result);
    assertEquals(0, retry.getInvocationCount());
  }

  @Test
  public void testEventuallyFailuresRetry() throws Throwable {
    try {
      eventually(TIMEOUT, EVAL_FNFE, retry);
      fail("should not have got here");
    } catch (IOException expected) {
      // expected
      assertMinRetryCount(1);
    }
  }

  /*
   * Java 8 Examples go below this line.
   */

  @Test
  public void testInterceptFailure() throws Throwable {
    try {
      IOException ioe = intercept(IOException.class, () -> "hello");
      assertNotNull(ioe);
      throw ioe;
    } catch (AssertionError expected) {
      assertExceptionContains("hello", expected);
    }
  }

  @Test
  public void testInterceptInterceptLambda() throws Throwable {
    // here we use intercept() to test itself.
    intercept(AssertionError.class,
        MISSING,
        () -> intercept(FileNotFoundException.class, "404", ALWAYS_FNFE));
  }

  @Test
  public void testInterceptInterceptVoidResultLambda() throws Throwable {
    // see what happens when a null is returned; type inference -> Void
    intercept(AssertionError.class,
        NULL_RESULT,
        () -> intercept(IOException.class, () -> null));
  }

  @Test
  public void testInterceptInterceptStringResultLambda() throws Throwable {
    // see what happens when a string is returned; it should be used
    // in the message
    intercept(AssertionError.class,
        "hello, world",
        () -> intercept(IOException.class,
            () -> "hello, world"));
  }

  @Test
  public void testAwaitNoTimeoutLambda() throws Throwable {
    await(0,
        () -> true,
        retry,
        (timeout, ex) -> ex != null ? ex : new Exception("timeout"));
    assertRetryCount(0);
  }

  @Test
  public void testAwaitLambdaRepetitions() throws Throwable {
    count = 0;

    // lambda expression which will succeed after exactly 4 probes
    int reps = await(TIMEOUT,
        () -> ++count == 4,
        () -> 10,
        (timeout, ex) -> ex != null ? ex : new Exception("timeout"));
    assertEquals(4, reps);
  }

  @Test
  public void testInterceptAwaitLambdaException() throws Throwable {
    count = 0;
    IOException ioe = intercept(IOException.class,
        () -> await(
            TIMEOUT,
            () -> {
              throw new IOException("inner " + ++count);
            },
            retry,
            (timeout, ex) -> ex));
    assertRetryCount(count - 1);
    // verify that the exception returned was the last one raised
    assertExceptionContains(Integer.toString(count), ioe);
  }

  @Test
  public void testInterceptAwaitLambdaDiagnostics() throws Throwable {
    intercept(IOException.class, "generated",
        () -> await(5,
            () -> false,
            () -> -1,  // force checks -1 timeout probes
            (timeout, ex) -> new IOException("generated")));
  }

  @Test
  public void testInterceptAwaitFailFastLambda() throws Throwable {
    intercept(FailFastException.class,
        () -> await(TIMEOUT,
            () -> {
              throw new FailFastException("ffe");
            },
            retry,
            (timeout, ex) -> ex));
    assertRetryCount(0);
  }

  @Test
  public void testEventuallyOnceLambda() throws Throwable {
    String result = eventually(0, () -> "hello", retry);
    assertEquals("hello", result);
    assertEquals(0, retry.getInvocationCount());
  }

  @Test
  public void testEventuallyLambda() throws Throwable {
    long result = eventually(TIMEOUT, () -> 3, retry);
    assertEquals(3, result);
    assertRetryCount(0);
  }

  @Test
  public void testInterceptEventuallyLambdaFailures() throws Throwable {
    intercept(IOException.class,
        "oops",
        () -> eventually(TIMEOUT,
            () -> {
              throw new IOException("oops");
            },
            retry));
    assertMinRetryCount(1);
  }

  @Test
  public void testInterceptEventuallyambdaFailuresNegativeRetry()
      throws Throwable {
    intercept(FileNotFoundException.class,
        () -> eventually(TIMEOUT, EVAL_FNFE, () -> -1));
  }

  @Test
  public void testInterceptEventuallyLambdaFailFast() throws Throwable {
    intercept(FailFastException.class, "oops",
        () -> eventually(
            TIMEOUT,
            () -> {
              throw new FailFastException("oops");
            },
            retry));
    assertRetryCount(0);
  }

}
