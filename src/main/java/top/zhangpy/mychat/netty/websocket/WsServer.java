package top.zhangpy.mychat.netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WsServer {

    private static final Logger log = LogManager.getLogger(WsServer.class);
    @Value("${netty.port}")
    public int port;

    @Value("${netty.host}")
    public String host;

    @Autowired
    private WsChannelInitializer wsChannelInitializer;

    private NioEventLoopGroup bossGroup = new NioEventLoopGroup();
    private NioEventLoopGroup workerGroup = new NioEventLoopGroup();

    public void start() throws InterruptedException{

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(port)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(wsChannelInitializer);

            log.info("netty server start at port: {}", port);
            ChannelFuture future = serverBootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("netty server start error: {}", e.getMessage());
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @PreDestroy
    public void close() {
        log.info("netty server close");
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    @PostConstruct
    public void init() {
        new Thread(() -> {
            try {
                start();
            } catch (InterruptedException e) {
                log.error("netty server start error: {}", e.getMessage());
            }
        }).start();
    }
}
