package com.potarski.jdispatch.smtp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

import java.util.ArrayList;
import java.util.List;

public class SMTPHandler {

    private final ChannelHandlerContext ctx;

    public static final AttributeKey<SMTPHandler> ATTRIBUTE_KEY = AttributeKey.valueOf("SMTP_HANDLER");

    private StringBuilder emailData;

    private enum State {
        INIT,
        MAIL,
        RCPT,
        DATA
    }

    private State state;

    private String from;
    private List<String> to;
    private List<String> cc;
    private String date;
    private String subject;
    private StringBuilder body;

    public SMTPHandler(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        this.emailData = new StringBuilder();
        this.state = State.INIT;

        this.from = null;
        this.to = new ArrayList<>();
        this.cc = new ArrayList<>();
        this.date = null;
        this.subject = null;
        this.body = new StringBuilder();
    }

    public void handle(String smtpLine, ChannelHandlerContext ctx) {

        switch (state) {
            case INIT:
                handleInitState(smtpLine, ctx);
                break;
            case MAIL:
                handleMailState(smtpLine, ctx);
                break;
            case RCPT:
                handleRcptState(smtpLine, ctx);
                break;
            case DATA:
                handleDataState(smtpLine, ctx);
                break;
        }
    }

    private void handleInitState(String smtpLine, ChannelHandlerContext ctx) {
        if (smtpLine.startsWith("HELO") || smtpLine.startsWith("EHLO")) {
            ctx.writeAndFlush("250 Hello " + smtpLine.substring(5).trim() + ", I am glad to meet you\r\n");
            state = State.MAIL;
        } else {
            sendErrorResponse(ctx, "500 Syntax error, command unrecognized");
        }
    }

    private void handleMailState(String smtpLine, ChannelHandlerContext ctx) {
        if (smtpLine.startsWith("MAIL FROM:")) {
            from = smtpLine.substring(10).trim();
            ctx.writeAndFlush("250 Ok\r\n");
            state = State.RCPT;
        } else {
            sendErrorResponse(ctx, "500 Syntax error in MAIL command");
        }
    }

    private void handleRcptState(String smtpLine, ChannelHandlerContext ctx) {
        if (smtpLine.startsWith("RCPT TO:")) {
            to.add(smtpLine.substring(8).trim());
            ctx.writeAndFlush("250 Ok\r\n");
        } else if (smtpLine.equals("DATA")) {
            ctx.writeAndFlush("354 End data with <CR><LF>.<CR><LF>\r\n");
            state = State.DATA;
        } else {
            sendErrorResponse(ctx, "500 Syntax error in RCPT command");
        }
    }

    private void handleDataState(String smtpLine, ChannelHandlerContext ctx) {
        if (smtpLine.equals(".")) {
            handleEndOfEmail(ctx);
            state = State.INIT;
        } else {
            emailData.append(smtpLine).append("\r\n");

            if (from == null && smtpLine.startsWith("From:")) {
                from = smtpLine.substring(5).trim();
            } else if (smtpLine.startsWith("To:")) {
                to.add(smtpLine.substring(3).trim());
            } else if (smtpLine.startsWith("Cc:")) {
                cc.add(smtpLine.substring(3).trim());
            } else if (date == null && smtpLine.startsWith("Date:")) {
                date = smtpLine.substring(5).trim();
            } else if (subject == null && smtpLine.startsWith("Subject:")) {
                subject = smtpLine.substring(8).trim();
            } else if (!smtpLine.startsWith("From:") && !smtpLine.startsWith("To:") &&
                    !smtpLine.startsWith("Cc:") && !smtpLine.startsWith("Date:") &&
                    !smtpLine.startsWith("Subject:")) {
                body.append(smtpLine).append("\r\n");
            }
        }
    }

    private void handleEndOfEmail(ChannelHandlerContext ctx) {

        System.out.println(Thread.currentThread().getId() + " Received email:");
        System.out.println(Thread.currentThread().getId() + " From: " + from);
        System.out.println(Thread.currentThread().getId() + " To: " + to);
        System.out.println(Thread.currentThread().getId() + " Cc: " + cc);
        System.out.println(Thread.currentThread().getId() + " Date: " + date);
        System.out.println(Thread.currentThread().getId() + " Subject: " + subject);
        System.out.println(Thread.currentThread().getId() + " Body: " + body);

        emailData.setLength(0);
        sendResponse(ctx, "250 Ok");
    }

    private void sendResponse(ChannelHandlerContext ctx, String message) {
        ctx.writeAndFlush(message + "\r\n");
    }

    private void sendErrorResponse(ChannelHandlerContext ctx, String errorMessage) {
        String response = "500 " + errorMessage + "\r\n";
        ctx.writeAndFlush(response);
        ctx.close();
    }

}
