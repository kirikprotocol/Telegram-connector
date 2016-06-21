package com.eyelinecom.whoisd.sads2.telegram.mock

import groovy.transform.PackageScope
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.channel.EventLoopGroup
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpContent
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.codec.http.HttpResponseEncoder
import io.netty.handler.codec.http.HttpUtil
import io.netty.handler.codec.http.LastHttpContent
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

    ServerBootstrap b = new ServerBootstrap()
    b.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new ChannelInitializer<SocketChannel>() {

      @Override
      protected void initChannel(SocketChannel ch) {
        final ChannelPipeline p = ch.pipeline()

        p.addLast(new HttpRequestDecoder())
        p.addLast(new HttpResponseEncoder())
        p.addLast(new Handler(handler))
      }
    })

    ch = b.bind(port).sync().channel()
  }

  void await() {
    ch.closeFuture().sync()
  }

  static class Handler extends SimpleChannelInboundHandler<Object> {
    private HttpRequest request
    private final RequestHandler handler

    Handler(RequestHandler handler) {
      this.handler = handler
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
      ctx.flush()
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
      if (msg instanceof HttpRequest) {
        this.request = msg as HttpRequest
      }

      if (msg instanceof HttpContent) {
        HttpContent httpContent = (HttpContent) msg

        final ByteBuf content = httpContent.content()

        final contentString = content.toString(CharsetUtil.UTF_8)
        final String response = handler.handle contentString

        if (msg instanceof LastHttpContent) {
          if (!writeResponse(ctx, request, response)) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
          }
        }
      }
    }

    private static boolean writeResponse(ChannelHandlerContext ctx,
                                         HttpRequest request,
                                         String content) {

      boolean keepAlive = HttpUtil.isKeepAlive(request)

      FullHttpResponse response = new DefaultFullHttpResponse(
          HTTP_1_1, OK,
          Unpooled.copiedBuffer(content, CharsetUtil.UTF_8))

//      response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain charset=UTF-8")

      if (keepAlive) {
        // Add 'Content-Length' header only for a keep-alive connection.
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes())
        // Add keep alive header as per:
        // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
      }

      // Write the response.
      ctx.write(response)

      return keepAlive
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
    String handle(String content)
  }
}
