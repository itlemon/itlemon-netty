package cn.itlemon.netty.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.itlemon.netty.model.RequestFuture;
import cn.itlemon.netty.model.Response;
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
        // 获取客户端发送的请求，并将其转换为RequestFuture对象
        // 由于经过了StringDecoder解码器，所以msg变成了String类型
        JSONObject jsonObject = JSONUtil.parseObj(msg.toString());
        RequestFuture request = jsonObject.toBean(RequestFuture.class);
        long id = request.getId();
        LOGGER.info("请求ID为: {}", id);
        // 构建相应结果
        Response response = new Response();
        response.setId(id);
        response.setResult("success");
        // 将相应结果返回给客户端
        ctx.channel().writeAndFlush(JSONUtil.toJsonStr(response));
    }

}
