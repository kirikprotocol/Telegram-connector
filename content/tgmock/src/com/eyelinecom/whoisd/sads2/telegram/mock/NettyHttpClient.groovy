package com.eyelinecom.whoisd.sads2.telegram.mock

import groovy.transform.PackageScope
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.DefaultFullHttpRequest
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpObject
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpVersion
import io.netty.handler.codec.http.LastHttpContent
import io.netty.util.CharsetUtil

import java.util.concurrent.Executor
import java.util.concurrent.ThreadFactory

@PackageScope
class NettyHttpClient {

  private final EventLoopGroup group

  private final ChannelInitializer channelInitializer = new ChannelInitializer<SocketChannel>() {
    @Override
    void initChannel(SocketChannel ch) {
      final p = ch.pipeline()
      p.addLast(new HttpClientCodec())
      p.addLast(new SimpleChannelInboundHandler<HttpObject>() {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
          if (msg instanceof LastHttpContent) ctx.close()
        }

        @Override
        void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
          cause.printStackTrace()
          ctx.close()
        }
      })
    }
  }

  NettyHttpClient(int nActors, Executor executor) {
    group = new NioEventLoopGroup(nActors, new ThreadFactory() {
      volatile int nClient = 0

      @Override Thread newThread(Runnable r) { new Thread(r, "client-${nClient++}") }
    })
  }

  void stop() {
    group.shutdownGracefully()
  }

  void request(String host, int port, String path, String content) {
    final b = new Bootstrap()
    b.group(group)
        .channel(NioSocketChannel)
        .handler(channelInitializer)

    final Channel ch = b.connect(host, port).sync().channel()

    // Prepare the HTTP request.
    final buf = Unpooled.copiedBuffer content, CharsetUtil.UTF_8

    HttpRequest request = new DefaultFullHttpRequest(
        HttpVersion.HTTP_1_1, HttpMethod.POST, path)

    request.headers().set(HttpHeaderNames.HOST, host)
    request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
    request.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes())
    request.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8")

    request.content().writeBytes(buf)

    ch.writeAndFlush(request)
  }

}
