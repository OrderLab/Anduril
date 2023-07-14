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
package org.apache.hadoop.hbase.ipc;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.MethodDescriptor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.CellScannable;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.MetricsConnection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.RowMutations;
import org.apache.hadoop.hbase.codec.Codec;
import org.apache.hadoop.hbase.ipc.protobuf.generated.TestProtos.EchoRequestProto;
import org.apache.hadoop.hbase.protobuf.RequestConverter;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos.MutationProto;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos.RegionAction;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.RegionSpecifierType;
import org.apache.hadoop.hbase.security.User;
import org.apache.hadoop.hbase.testclassification.RPCTests;
import org.apache.hadoop.hbase.testclassification.SmallTests;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
@Category({ RPCTests.class, SmallTests.class })
public class TestAsyncIPC extends AbstractTestIPC {

  private static final Log LOG = LogFactory.getLog(TestAsyncIPC.class);

  @Parameters
  public static Collection<Object[]> parameters() {
    List<Object[]> paramList = new ArrayList<>();
    paramList.add(new Object[] { false, false });
    paramList.add(new Object[] { false, true });
    paramList.add(new Object[] { true, false });
    paramList.add(new Object[] { true, true });
    return paramList;
  }

  private final boolean useNativeTransport;

  private final boolean useGlobalEventLoopGroup;

  public TestAsyncIPC(boolean useNativeTransport, boolean useGlobalEventLoopGroup) {
    this.useNativeTransport = useNativeTransport;
    this.useGlobalEventLoopGroup = useGlobalEventLoopGroup;
  }

  private void setConf(Configuration conf) {
    conf.setBoolean(AsyncRpcClient.USE_NATIVE_TRANSPORT, useNativeTransport);
    conf.setBoolean(AsyncRpcClient.USE_GLOBAL_EVENT_LOOP_GROUP, useGlobalEventLoopGroup);
    if (useGlobalEventLoopGroup && AsyncRpcClient.GLOBAL_EVENT_LOOP_GROUP != null) {
      if (useNativeTransport
          && !(AsyncRpcClient.GLOBAL_EVENT_LOOP_GROUP.getFirst() instanceof EpollEventLoopGroup)
          || (!useNativeTransport
          && !(AsyncRpcClient.GLOBAL_EVENT_LOOP_GROUP.getFirst() instanceof NioEventLoopGroup))) {
        AsyncRpcClient.GLOBAL_EVENT_LOOP_GROUP.getFirst().shutdownGracefully();
        AsyncRpcClient.GLOBAL_EVENT_LOOP_GROUP = null;
      }
    }
  }

  @Override
  protected AsyncRpcClient createRpcClientNoCodec(Configuration conf) {
    setConf(conf);
    return new AsyncRpcClient(conf) {

      @Override
      Codec getCodec() {
        return null;
      }

    };
  }

  @Override
  protected AsyncRpcClient createRpcClient(Configuration conf) {
    setConf(conf);
    return new AsyncRpcClient(conf);
  }

  @Override
  protected AsyncRpcClient createRpcClientRTEDuringConnectionSetup(Configuration conf) {
    setConf(conf);
    return new AsyncRpcClient(conf, new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addFirst(new ChannelOutboundHandlerAdapter() {
              @Override
              public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
                  throws Exception {
                promise.setFailure(new RuntimeException("Injected fault"));
              }
            });
          }
        });
  }

  public static void main(String[] args) throws IOException, SecurityException,
      NoSuchMethodException, InterruptedException {
    if (args.length != 2) {
      System.out.println("Usage: TestAsyncIPC <CYCLES> <CELLS_PER_CYCLE>");
      return;
    }
    // ((Log4JLogger)HBaseServer.LOG).getLogger().setLevel(Level.INFO);
    // ((Log4JLogger)HBaseClient.LOG).getLogger().setLevel(Level.INFO);
    int cycles = Integer.parseInt(args[0]);
    int cellcount = Integer.parseInt(args[1]);
    Configuration conf = HBaseConfiguration.create();
    TestRpcServer rpcServer = new TestRpcServer();
    MethodDescriptor md = SERVICE.getDescriptorForType().findMethodByName("echo");
    EchoRequestProto param = EchoRequestProto.newBuilder().setMessage("hello").build();
    KeyValue kv = BIG_CELL;
    Put p = new Put(CellUtil.cloneRow(kv));
    for (int i = 0; i < cellcount; i++) {
      p.add(kv);
    }
    RowMutations rm = new RowMutations(CellUtil.cloneRow(kv));
    rm.add(p);
    try (AsyncRpcClient client = new AsyncRpcClient(conf)) {
      rpcServer.start();
      InetSocketAddress address = rpcServer.getListenerAddress();
      if (address == null) {
        throw new IOException("Listener channel is closed");
      }
      long startTime = System.currentTimeMillis();
      User user = User.getCurrent();
      for (int i = 0; i < cycles; i++) {
        List<CellScannable> cells = new ArrayList<>();
        // Message param = RequestConverter.buildMultiRequest(HConstants.EMPTY_BYTE_ARRAY, rm);
        ClientProtos.RegionAction.Builder builder =
            RequestConverter.buildNoDataRegionAction(HConstants.EMPTY_BYTE_ARRAY, rm, cells,
                RegionAction.newBuilder(), ClientProtos.Action.newBuilder(),
                MutationProto.newBuilder());
        builder.setRegion(RegionSpecifier
            .newBuilder()
            .setType(RegionSpecifierType.REGION_NAME)
            .setValue(
                ByteString.copyFrom(HRegionInfo.FIRST_META_REGIONINFO.getEncodedNameAsBytes())));
        if (i % 100000 == 0) {
          LOG.info("" + i);
          // Uncomment this for a thread dump every so often.
          // ReflectionUtils.printThreadInfo(new PrintWriter(System.out),
          // "Thread dump " + Thread.currentThread().getName());
        }
        PayloadCarryingRpcController pcrc =
            new PayloadCarryingRpcController(CellUtil.createCellScanner(cells));
        // Pair<Message, CellScanner> response =
        client.call(pcrc, md, builder.build(), param, user, address,
            new MetricsConnection.CallStats());
        /*
         * int count = 0; while (p.getSecond().advance()) { count++; } assertEquals(cells.size(),
         * count);
         */
      }
      LOG.info("Cycled " + cycles + " time(s) with " + cellcount + " cell(s) in "
          + (System.currentTimeMillis() - startTime) + "ms");
    } finally {
      rpcServer.stop();
    }
  }
}
