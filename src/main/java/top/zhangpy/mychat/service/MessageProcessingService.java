package top.zhangpy.mychat.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.zhangpy.mychat.configuration.NettyConfig;
import top.zhangpy.mychat.entity.dto.MessageQueue;
import top.zhangpy.mychat.entity.dto.ServerMessage;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class MessageProcessingService {

    private static final Log log = LogFactory.getLog(MessageProcessingService.class);

    @Autowired
    private MessageQueue messageQueue;

    @Async
    public void startMessageProcessing() {
        while (!Thread.currentThread().isInterrupted()) {
            List<Integer> onlineUsers = NettyConfig.getOnlineUsers();
            if (!onlineUsers.isEmpty()) {
                for (Integer userId : onlineUsers) {
                    ConcurrentLinkedQueue<ServerMessage> queue = messageQueue.getMessageQueue().get(userId);
                    if (queue != null && !queue.isEmpty()) {
                        synchronized (queue) {
                            while (!queue.isEmpty()) {
                                ServerMessage serverMessage = queue.poll();
                                boolean success = sendToClient(userId, serverMessage);
                            }
                        }
                    }
                }
            }
            try {
                Thread.sleep(100); // 减少 CPU 空转
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

        }
    }

    public boolean sendToClient(Integer userId, ServerMessage serverMessage) {
        Channel channel = NettyConfig.getUserChannelMap().get(userId);
        if (channel != null) {
            try {
                channel.writeAndFlush(new TextWebSocketFrame(serverMessage.toString()));
                return true;
            } catch (Exception e) {
                log.error("Error sending message to user: " + userId, e);
            }
        } else {
            log.warn("No active channel for user: " + userId);
        }
        return false;
    }

}
