package com.potarski.jdispatch.smtp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Server {
    private final int PORT = 25;
    private final Logger LOG = LoggerFactory.getLogger(Server.class);

    private Channel serverChannel;
    public synchronized void start() throws InterruptedException {


        if (!isClosed()) {
            throw new IllegalStateException("Server already started");
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8));
                            ch.pipeline().addLast(new ReadLineHandler());
                            LOG.info("Accepted connection from: {}", ch.remoteAddress().toString());
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.bind(PORT).sync();
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
        }
    }

    public boolean isClosed() {
        return serverChannel == null || !serverChannel.isActive();
    }

    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
