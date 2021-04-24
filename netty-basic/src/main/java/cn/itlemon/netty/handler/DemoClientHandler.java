package cn.itlemon.netty.handler;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.itlemon.netty.model.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Promise;

/**
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2021-04-24
 */
public class DemoClientHandler extends ChannelInboundHandlerAdapter {

    private Promise<Response> promise;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 读取服务端响应的结果，并将其转换为Response对象
        // 由于经过了StringDecoder解码器，所以msg为String类型
        JSONObject jsonObject = JSONUtil.parseObj(msg.toString());
        Response response = jsonObject.toBean(Response.class);
        promise.setSuccess(response);
    }

    public Promise<Response> getPromise() {
        return promise;
    }

    public void setPromise(Promise<Response> promise) {
        this.promise = promise;
    }
}
