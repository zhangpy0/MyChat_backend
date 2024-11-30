package top.zhangpy.mychat.service;

import top.zhangpy.mychat.entity.dto.ServerMessage;
import top.zhangpy.mychat.entity.po.ChatMessage;

import java.util.List;

public interface PushService {
    List<ServerMessage> transferMessage(ChatMessage chatMessage);

    boolean addToMessageQueue(ChatMessage chatMessage);
}
