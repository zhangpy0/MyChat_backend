package top.zhangpy.mychat.netty.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import top.zhangpy.mychat.configuration.NettyConfig;

@Component
@ChannelHandler.Sharable
public class WsChannelHandler extends SimpleChannelInboundHandler<Object> {

    private static final Log log = LogFactory.getLog(WsChannelHandler.class);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        log.info("channel id: " + channel.id().asLongText());
        NettyConfig.getChannelGroup().add(channel);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        log.info("client: " + channel.remoteAddress() + " connected");
        log.info("channel id: " + channel.id().asLongText());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        log.info("client: " + channel.remoteAddress() + " disconnected");
        log.info("channel id: " + channel.id().asLongText());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame) o;
        String content = textWebSocketFrame.text();
        // 取消开发日志输出
//        log.info("receive message from: " + channelHandlerContext.channel().remoteAddress() + " . content: " + content);
//        channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame("server received: " + content));
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        log.info("channel id: " + channel.id().asLongText());
        NettyConfig.getChannelGroup().remove(channel);
        removeUserId(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("WsChannelHandler error: " + cause);
        NettyConfig.getChannelGroup().remove(ctx.channel());
        removeUserId(ctx);
        ctx.close();
    }

    private void removeUserId(ChannelHandlerContext ctx) {
        AttributeKey<String> key = AttributeKey.valueOf("userId");
        String userId = ctx.channel().attr(key).get();
        NettyConfig.getUserChannelMap().remove(Integer.valueOf(userId));
    }
}
