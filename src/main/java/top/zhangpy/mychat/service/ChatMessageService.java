package top.zhangpy.mychat.service;

import org.springframework.web.multipart.MultipartFile;
import top.zhangpy.mychat.entity.po.ChatMessage;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;

public interface ChatMessageService {

    boolean addMessage(ChatMessage chatMessage);

    boolean addMessage(ChatMessage chatMessage, MultipartFile file);

    List<ChatMessage> getChatMessagesAfterTime(Integer userId, Timestamp time);

    File getFileByMessageId(Integer messageId);
}
