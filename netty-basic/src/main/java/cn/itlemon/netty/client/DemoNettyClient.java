package cn.itlemon.netty.client;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.json.JSONUtil;
import cn.itlemon.netty.handler.DemoClientHandler;
import cn.itlemon.netty.model.RequestFuture;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2021-04-24
 */
public class DemoNettyClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoNettyClient.class);

    public static EventLoopGroup group;
    public static Bootstrap bootstrap;
    public static ChannelFuture future;

    static {
        // 客户端启动辅助类
        bootstrap = new Bootstrap();
        // 开启一个线程组
        group = new NioEventLoopGroup();
        // 设置socket通道
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(group);
        // 设置内存分配器
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        final DemoClientHandler clientHandler = new DemoClientHandler();
        // 把Handler对象加入到管道中
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) {
                ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                // 将接收到的ByteBuf数据包转换成String
                ch.pipeline().addLast(new StringDecoder());
                // 业务逻辑处理Handler
                ch.pipeline().addLast(clientHandler);
                ch.pipeline().addLast(new LengthFieldPrepender(4, false));
                // 将字符串消息转换成ByteBuf
                ch.pipeline().addLast(new StringEncoder(StandardCharsets.UTF_8));
            }
        });
        // 连接服务器
        try {
            future = bootstrap.connect("localhost", 8080).sync();
        } catch (Exception e) {
            LOGGER.error("demo netty client get exception.", e);
        }
    }

    public static void main(String[] args) {
        DemoNettyClient demoNettyClient = new DemoNettyClient();
        for (int i = 0; i < 100; i++) {
            LOGGER.info("result: {}", demoNettyClient.sendRequest("hello netty!"));
        }
    }

    public Object sendRequest(Object msg) {
        try {
            // 构建request
            RequestFuture request = new RequestFuture();
            request.setRequest(msg);

            future.channel().writeAndFlush(JSONUtil.toJsonStr(request));
            // 同步等待结果
            return request.get();
        } catch (Exception e) {
            LOGGER.error("send request fail.", e);
            throw e;
        }
    }

}
