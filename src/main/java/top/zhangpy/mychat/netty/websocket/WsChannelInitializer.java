package top.zhangpy.mychat.netty.websocket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WsChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Autowired
    private AuthHandler authHandler;

    @Autowired
    private WsChannelHandler wsChannelHandler;

    @Autowired
    private HeartBeatHandler heartBeatHandler;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("http-codec",new HttpServerCodec());
        pipeline.addLast("chunk-handler", new ChunkedWriteHandler());
        pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
        pipeline.addLast("auth-handler", authHandler);
        pipeline.addLast("websocket-handler", new WebSocketServerProtocolHandler("/ws", "WebSocket", true, 65536));
        pipeline.addLast(new IdleStateHandler(10, 0, 0));
        pipeline.addLast("heart-beat-handler",new HeartBeatHandler());
        pipeline.addLast("ws-handler", wsChannelHandler);
    }
}
