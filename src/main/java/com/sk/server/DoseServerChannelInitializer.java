package com.sk.server;

import com.sk.config.AppProperties;
import com.sk.handler.FileStreamHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;


/**
 *  Initialize channel
 *
 *  Add {@link FileStreamHandler} into the {@link ChannelPipeline}.
 *  Pass <code>base.download.folder</code> as param as {@link FileStreamHandler}
 *
 * */
public class DoseServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws NoSuchAlgorithmException {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new ChunkedWriteHandler());

        String baseDownloadFolderPath = AppProperties.getProperty("base.download.folder", String.class);
        pipeline.addLast(new FileStreamHandler(Paths.get(baseDownloadFolderPath)));
    }
}
