package top.zhangpy.mychat.netty.websocket;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.zhangpy.mychat.configuration.NettyConfig;
import top.zhangpy.mychat.util.auth.JWTUtils;

@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class AuthHandler extends ChannelInboundHandlerAdapter {

    private static final Log log = LogFactory.getLog(AuthHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            try {
                HttpHeaders headers = request.headers();
                if (headers.isEmpty()) {
                    ctx.channel().close();
                    return;
                }
                String token = headers.get("token");
                if (token == null) {
                    ctx.channel().close();
                    return;
                }
                log.info("token: " + token);
                String userIdFromToken;
                try {
                    userIdFromToken = JWTUtils.getClaimByName(token, "userId").asString();
                } catch (RuntimeException e) {
                    log.error("Token is invalid: " + e.getMessage());
                    ctx.channel().close();
                    return;
                }
                AttributeKey<String> key = AttributeKey.valueOf("userId");
                ctx.channel().attr(key).set(userIdFromToken);
                NettyConfig.getUserChannelMap().put(Integer.valueOf(userIdFromToken) ,ctx.channel());
//                // 构造认证成功的 HTTP 响应
//                DefaultFullHttpResponse response = new DefaultFullHttpResponse(
//                        HttpVersion.HTTP_1_1, HttpResponseStatus.OK, ctx.alloc().buffer());
//                response.content().writeBytes("auth success".getBytes());
//                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
//                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
//
//                ctx.writeAndFlush(response);

                // 初始化 WebSocket 握手
                WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                        request.uri(), null, true);
                WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(request);
                if (handshaker == null) {
                    WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
                } else {
                    handshaker.handshake(ctx.channel(), request);
                }
//                ctx.pipeline().remove("chunk-handler");
                // 牛魔debug两天
                // 猜测: 上面用了WebSocketServerHandshaker，pipeline就自动添加 WebSocketServerProtocolHandshakeHandler
                // WebSocketServerProtocolHandshakeHandler channelRead 会强转 HttpObject httpObject = (HttpObject)msg;
                // 所以这里要移除 WebSocketServerProtocolHandshakeHandler
                ctx.pipeline().remove("io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandshakeHandler");
                ctx.pipeline().remove(this);
//                ctx.fireChannelRead(msg);
            } catch (NumberFormatException e) {
                throw new RuntimeException(e);
            } finally {
                request.release();
            }
        } else {
            log.error("AuthHandler error: " + "msg is not FullHttpRequest");
            ctx.channel().close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("AuthHandler error: " + cause);
        ctx.channel().close();
    }
}
