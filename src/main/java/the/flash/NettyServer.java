package the.flash;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

public class NettyServer {

    private static final int BEGIN_PORT = 8000;

    public static void main(String[] args) {
        NioEventLoopGroup boosGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        final ServerBootstrap serverBootstrap = new ServerBootstrap();
        final AttributeKey<Object> clientKey = AttributeKey.newInstance("clientKey");
        serverBootstrap
                // 指定线程组模型: 1. Serversocket的工作线程组模型. 2. 每条与客户端的Channel所使用的线程组模型
                .group(boosGroup, workerGroup)
                // 这里的channel指定的是ServerSocket的Channel, 是用来accept新连接的
                .channel(NioServerSocketChannel.class)
                // 这里的attr是绑定到ServerSocket的Channel上的
                .attr(AttributeKey.newInstance("serverName"), "nettyServer")
                // 这里的childAttr是绑定到每条新建立的用于与client通信的Channel上的
                .childAttr(clientKey, "clientValue")
                // 为ServerSocket的Channel设置最大连接等待队列长度为1024, 如果等待超过了这个值, 那么拒绝新链接
                .option(ChannelOption.SO_BACKLOG, 1024)
                // 为每条新连接指定是否开启TCP底层心跳机制
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                // 是否启用Nagle算法, true表示关闭, 用于实时性较高的场合; false表示开启. 选择需要根据应用来权衡
                .childOption(ChannelOption.TCP_NODELAY, true)
                // 为与客户端的通信Channel添加handler
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel ch) {
                        System.out.println(ch.attr(clientKey).get());
                    }
                });


        bind(serverBootstrap, BEGIN_PORT);
    }

    /**
     * 自增绑定的实现
     *
     * @param serverBootstrap
     * @param port
     */
    private static void bind(final ServerBootstrap serverBootstrap, final int port) {
        serverBootstrap.bind(port).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("端口[" + port + "]绑定成功!");
            } else {
                System.err.println("端口[" + port + "]绑定失败!");
                bind(serverBootstrap, port + 1);
            }
        });
    }
}
