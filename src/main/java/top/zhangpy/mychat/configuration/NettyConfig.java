package top.zhangpy.mychat.configuration;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class NettyConfig {

    @Getter
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Getter
    private static ConcurrentHashMap<Integer, Channel> userChannelMap = new ConcurrentHashMap<>();

    private NettyConfig() {
    }

    public static List<Integer> getOnlineUsers() {
        return List.copyOf(userChannelMap.keySet());
    }
}
