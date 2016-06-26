package com.eyelinecom.whoisd.sads2.telegram.mock

import groovy.transform.PackageScope
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.HttpContent
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.codec.http.HttpResponseEncoder
import io.netty.util.CharsetUtil

import static io.netty.handler.codec.http.HttpResponseStatus.OK
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1

@PackageScope
class NettyHttpServer {

  private final EventLoopGroup bossGroup
  private final EventLoopGroup workerGroup

  private final Channel ch

  NettyHttpServer(int port, RequestHandler handler) {
    bossGroup = new NioEventLoopGroup(1)
    workerGroup = new NioEventLoopGroup()

    final bootstrap = new ServerBootstrap()
    bootstrap
        .group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG, 10_240)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000)
        .childHandler(new ChannelInitializer<SocketChannel>() {

      @Override
      protected void initChannel(SocketChannel ch) {
        final pipeline = ch.pipeline()

        pipeline.addLast(new HttpRequestDecoder())
        pipeline.addLast(new HttpResponseEncoder())
        pipeline.addLast(new Handler(handler))
      }
    })

    ch = bootstrap.bind(port).sync().channel()
  }

  void await() {
    ch.closeFuture().sync()
  }

  private static class Handler extends SimpleChannelInboundHandler<Object> {
    private final RequestHandler handler

    Handler(RequestHandler handler) {
      this.handler = handler
    }

    @Override
    void channelReadComplete(ChannelHandlerContext ctx) {
      ctx.flush()
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
      if (msg instanceof HttpContent) {
        final httpContent = msg as HttpContent

        final content = httpContent.content()

        final contentString = content.toString(CharsetUtil.UTF_8)
        final response = handler.handle contentString

        writeResponse(ctx, response)
        ctx
            .writeAndFlush(Unpooled.EMPTY_BUFFER)
            .addListener(ChannelFutureListener.CLOSE)
      }
    }

    private static void writeResponse(ChannelHandlerContext ctx,
                                         String content) {

      final response = new DefaultFullHttpResponse(
          HTTP_1_1, OK,
          Unpooled.copiedBuffer(content, CharsetUtil.UTF_8)
      )
      ctx.write(response)
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      cause.printStackTrace()
      ctx.close()
    }
  }

  void stop() {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }

  static interface RequestHandler extends EventListener {

    /**
     * Called on each incoming request.
     *
     * @param content  Full HTTP request payload.
     * @return Desired response.
     */
    String handle(String content)
  }
}
