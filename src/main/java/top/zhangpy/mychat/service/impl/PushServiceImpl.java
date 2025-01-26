package top.zhangpy.mychat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.netty.channel.Channel;
import jakarta.annotation.PostConstruct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.zhangpy.mychat.configuration.NettyConfig;
import top.zhangpy.mychat.entity.dto.MessageQueue;
import top.zhangpy.mychat.entity.dto.ServerMessage;
import top.zhangpy.mychat.entity.po.ChatMessage;
import top.zhangpy.mychat.entity.po.GroupMember;
import top.zhangpy.mychat.mapper.GroupMemberMapper;
import top.zhangpy.mychat.service.ContactService;
import top.zhangpy.mychat.service.MessageProcessingService;
import top.zhangpy.mychat.service.PushService;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class PushServiceImpl implements PushService {

    private static final Log log = LogFactory.getLog(PushServiceImpl.class);
//    @Autowired
//    private ContactService contactService;

    @Autowired
    private GroupMemberMapper groupMemberMapper;

    @Autowired
    private MessageQueue messageQueue;

    @Autowired
    private MessageProcessingService messageProcessingService;

    @Override
    public List<ServerMessage> transferMessage(ChatMessage chatMessage) {
        Integer senderId = chatMessage.getSenderId();
        Integer receiverId = chatMessage.getReceiverId();
        Integer groupId = chatMessage.getGroupId();
        String receiverType = chatMessage.getReceiverType();
        String content;
        String messageType = chatMessage.getMessageType();

        String filePath;
        String fileName;

        if (!messageType.equals("text")) {
            filePath = chatMessage.getFilePath();
            fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
            File file = new File(filePath);
            content = chatMessage.getMessageId() + ":" + fileName + ":" + file.length();
        } else {
            content = chatMessage.getContent();
        }
        if (receiverType.equals("user")) {
            return List.of(new ServerMessage(senderId, receiverId, 0, content, messageType));
        } else {
            List<Integer> groupMembers = getGroupMembers(groupId);
            return groupMembers.stream().map(memberId -> new ServerMessage(senderId, memberId, groupId, content, messageType)).toList();
        }
    }

    @Override
    public boolean addToMessageQueue(ChatMessage chatMessage) {
        List<ServerMessage> serverMessages = transferMessage(chatMessage);
        for (ServerMessage serverMessage : serverMessages) {
            messageQueue.addMessage(serverMessage.getReceiverId(), serverMessage);
        }
        return true;
    }

    public boolean sendToClient(Integer userId, ServerMessage serverMessage) {
        Channel channel = NettyConfig.getUserChannelMap().get(userId);
        if (channel != null) {
            try {
                channel.writeAndFlush(serverMessage.toString());
                return true;
            } catch (Exception e) {
                log.error("Error sending message to user: " + userId, e);
            }
        } else {
            log.warn("No active channel for user: " + userId);
        }
        return false;
    }

    @PostConstruct
    public void initMessageProcessing() {
        messageProcessingService.startMessageProcessing();
    }

    public List<Integer> getGroupMembers(Integer groupId) {
        QueryWrapper<GroupMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(GroupMember::getGroupId, groupId);
        List<GroupMember> groupMembers = groupMemberMapper.selectList(queryWrapper);
        return groupMembers.stream().map(GroupMember::getUserId).toList();
    }

//    @Async
//    public void startMessageProcessing() {
//        while (!Thread.currentThread().isInterrupted()) {
//            List<Integer> onlineUsers = NettyConfig.getOnlineUsers();
//            if (!onlineUsers.isEmpty()) {
//                for (Integer userId : onlineUsers) {
//                    ConcurrentLinkedQueue<ServerMessage> queue = messageQueue.getMessageQueue().get(userId);
//                    if (queue != null && !queue.isEmpty()) {
//                        synchronized (queue) {
//                            while (!queue.isEmpty()) {
//                                ServerMessage serverMessage = queue.poll();
//                                boolean success = sendToClient(userId, serverMessage);
//                            }
//                        }
//                    }
//                }
//            }
//            try {
//                Thread.sleep(100); // 减少 CPU 空转
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                break;
//            }
//        }
//    }
}
