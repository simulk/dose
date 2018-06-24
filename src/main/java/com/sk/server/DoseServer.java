package com.sk.server;

import com.sk.config.AppProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;


/**
 * Start DOSE server.
 *
 * It will listen on <code>server.port</code> property which is defaulted to <b>8080</b>
 *
 * @throw   IllegalArgumentException    if <code>base.download.folder</code> property not provided
 * */
public class DoseServer {
    public DoseServer() {}

    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new DoseServerChannelInitializer())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            Integer port = AppProperties.getProperty("server.port", 8080, Integer.class);
            String baseDownloadFolderPath = AppProperties.getProperty("base.download.folder", String.class);
            if (baseDownloadFolderPath == null) {
                throw new IllegalArgumentException("Base download folder path cannot be null");
            }
            System.out.println("Base download folder path::" + baseDownloadFolderPath);
            ChannelFuture f = b.bind(port).sync();

            System.out.println("DOSE server listening at port::" + port);
            f.channel().closeFuture().sync();
        } catch (Exception ex) {
            throw ex;
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
