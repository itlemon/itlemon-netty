package cn.itlemon.netty.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.itlemon.netty.handler.DemoServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2021-04-23
 */
public class DemoNettyServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoNettyServer.class);

    public static void main(String[] args) {
        // 新建两个线程组，boss线程组启动一个线程，监听OP_ACCEPT事件
        // Worker线程组默认启动CPU核数*2的线程
        // 监听客户端连接的OP_READ和OP_WRITE事件，处理IO事件
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // ServerBootstrap为Netty服务启动辅助类
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup);

            // 设置TCP Socket通道为NioServerSocketChannel，
            // 如果是UDP，则设置为DatagramChannel
            serverBootstrap.channel(NioServerSocketChannel.class);

            // 设置TCP的参数
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 128)
                    // 当有客户端链路注册读写事件时，初始化Handler，并将Handler加入管道中
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 以前缀为4B的int类型作为长度的解码器
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                            // 将接收到的ByteBuf数据包转换为String
                            ch.pipeline().addLast(new StringDecoder());
                            // 用户自定义处理器，用于处理输入数据
                            ch.pipeline().addLast(new DemoServerHandler());
                            // 在消息体前面新增4个字节的长度值，并且设置总大小不包含长度值
                            ch.pipeline().addLast(new LengthFieldPrepender(4, false));
                            // 将字符串消息转换成ByteBuf
                            ch.pipeline().addLast(new StringEncoder());
                        }
                    });
            // 绑定同步端口
            ChannelFuture future = serverBootstrap.bind(8080).sync();
            // 阻塞主线程，直到Socket通道被关闭
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }


    }

}
