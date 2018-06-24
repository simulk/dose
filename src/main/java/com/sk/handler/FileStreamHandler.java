package com.sk.handler;

import com.sk.config.AppProperties;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.HEAD;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


/**
 * Handler to stream requested file. Supports only <b><i>GET</i></b> and <b><i>HEAD</i></b> requests
 *
 * Set <code>etag header</code> if <code>etag.enabled</code> property is set to true. It is defaulted to true
 *
 * Uses <code>etag.type</code> property to decide whether to use
 * {@link StrongEtagGenerator} or {@link WeakEtagGenerator}. Default is {@link WeakEtagGenerator}
 *
 * @throws NoSuchAlgorithmException if <code>strong.etag.hash.algorithm</code> is invalid
 * */
public class FileStreamHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
    private final String baseDownloadPath;
    private final Boolean isEtagEnabled;
    private static EtagGenerator etagGenerator;

    public FileStreamHandler(Path path) throws NoSuchAlgorithmException {
        super();
        this.baseDownloadPath = path.toAbsolutePath().toString();

        isEtagEnabled = AppProperties.getBoolean("etag.enabled", "true");
        if (isEtagEnabled) {
            String etagType = AppProperties.getProperty("etag.type", "weak", String.class);
            if ("strong".equalsIgnoreCase(etagType)) {
                etagGenerator = StrongEtagGenerator.getInstance();
            } else {
                etagGenerator = WeakEtagGenerator.getInstance();
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws IOException {
        if (!request.decoderResult().isSuccess()) {
            sendError(ctx, BAD_REQUEST);
            return;
        }

        HttpMethod method = request.method();

        if (GET != method && HEAD != method) {
            sendError(ctx, METHOD_NOT_ALLOWED);
            return;
        }

        final File file = getFile(request.uri());
        if (file == null) {
            sendError(ctx, FORBIDDEN);
            return;
        }

        if (file.isHidden() || !file.exists()) {
            sendError(ctx, NOT_FOUND);
            return;
        }

        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException ignore) {
            sendError(ctx, NOT_FOUND);
            return;
        }

        long fileLength = file.length();
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, fileLength);
        if (isEtagEnabled) {
            response.headers().set(HttpHeaderNames.ETAG, etagGenerator.generateEtag(file));
        }
        if (HttpUtil.isKeepAlive(request)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ChannelFuture lastContentFuture = ctx.write(response);

        if (method == GET) {
            ChannelFuture sendFileFuture;
            if (ctx.pipeline().get(SslHandler.class) == null) {
                sendFileFuture =
                        ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
                lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            } else {
                sendFileFuture =
                        ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 8192)),
                                ctx.newProgressivePromise());
                lastContentFuture = sendFileFuture;
            }

            sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
                @Override
                public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                    if (total < 0) { // total unknown
                        System.err.println(future.channel() + " Transfer progress: " + progress);
                    } else {
                        System.err.println(future.channel() + " Transfer progress: " + progress + " / " + total);
                    }
                }

                @Override
                public void operationComplete(ChannelProgressiveFuture future) {
                    System.err.println(future.channel() + " Transfer complete.");
                }
            });
        }

        if (!HttpUtil.isKeepAlive(request)) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private File getFile(String uri) {
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }

        if (uri.isEmpty() || uri.charAt(0) != '/') {
            return null;
        }

        uri = uri.replace('/', File.separatorChar);

        if (uri.contains(File.separator + '.') ||
                uri.contains('.' + File.separator) ||
                uri.charAt(0) == '.' || uri.charAt(uri.length() - 1) == '.' ||
                INSECURE_URI.matcher(uri).matches()) {
            return null;
        }

        return Paths.get(baseDownloadPath, uri).toFile();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
