package com.potarski.jdispatch.smtp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ReadLineHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private SMTPHandler smtpHandler;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        smtpHandler = new SMTPHandler(ctx);
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        StringBuilder line = new StringBuilder();
        while (msg.isReadable()) {
            char c = (char) msg.readByte();
            if (c == '\n') {
                String smtpLine = line.toString().trim();
                smtpHandler.handle(smtpLine, ctx);
                line.setLength(0);
            } else {
                line.append(c);
            }
        }
    }
}
