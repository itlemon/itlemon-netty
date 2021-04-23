package cn.itlemon.netty.handler;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2021-04-23
 */
public class DemoServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoServerHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuffer) {
            LOGGER.info(msg.toString());
        }
        ctx.channel().writeAndFlush("msg has received!");
    }
}
