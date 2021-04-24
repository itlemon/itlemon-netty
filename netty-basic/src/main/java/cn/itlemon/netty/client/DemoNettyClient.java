package cn.itlemon.netty.client;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.json.JSONUtil;
import cn.itlemon.netty.handler.DemoClientHandler;
import cn.itlemon.netty.model.RequestFuture;
import cn.itlemon.netty.model.Response;
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
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

/**
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2021-04-24
 */
public class DemoNettyClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoNettyClient.class);

    public static EventLoopGroup group;
    public static Bootstrap bootstrap;

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
    }

    public static void main(String[] args) {
        try {
            // 新建一个promise对象
            Promise<Response> promise = new DefaultPromise<>(group.next());
            final DemoClientHandler clientHandler = new DemoClientHandler();
            clientHandler.setPromise(promise);
            // 把Handler对象加入到管道中
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
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
            ChannelFuture future = bootstrap.connect("localhost", 8080).sync();
            // 构建请求参数
            RequestFuture request = new RequestFuture();
            request.setId(1);
            request.setResult("hello netty!");
            future.channel().writeAndFlush(JSONUtil.toJsonStr(request));
            // 同步阻塞等待响应结果
            Response response = promise.get();
            LOGGER.info("response: {}", response);
        } catch (Exception e) {
            LOGGER.error("demo netty client get exception.", e);
        }
    }

}
