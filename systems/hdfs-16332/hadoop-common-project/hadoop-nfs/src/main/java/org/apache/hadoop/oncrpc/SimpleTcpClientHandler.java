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
package org.apache.hadoop.oncrpc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple TCP based RPC client handler used by {@link SimpleTcpServer}.
 */
public class SimpleTcpClientHandler extends ChannelInboundHandlerAdapter {
  public static final Logger LOG =
      LoggerFactory.getLogger(SimpleTcpClient.class);
  protected final XDR request;

  public SimpleTcpClientHandler(XDR request) {
    this.request = request;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    // Send the request
    if (LOG.isDebugEnabled()) {
      LOG.debug("sending PRC request");
    }
    ByteBuf outBuf = XDR.writeMessageTcp(request, true);
    ctx.channel().writeAndFlush(outBuf);
  }

  /**
   * Shutdown connection by default. Subclass can override this method to do
   * more interaction with the server.
   */
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    ctx.channel().close();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    LOG.warn("Unexpected exception from downstream: ", cause.getCause());
    ctx.channel().close();
  }
}
