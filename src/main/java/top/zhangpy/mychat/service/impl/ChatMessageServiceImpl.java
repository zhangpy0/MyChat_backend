package top.zhangpy.mychat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.zhangpy.mychat.entity.po.ChatMessage;
import top.zhangpy.mychat.mapper.ChatMessageMapper;
import top.zhangpy.mychat.service.ChatMessageService;
import top.zhangpy.mychat.service.PushService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.List;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    private static final Log log = LogFactory.getLog(ChatMessageServiceImpl.class);

    @Value("${project.folder}")
    String fileRootPath;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private PushService pushService;

    @Transactional
    public boolean addMessage(ChatMessage chatMessage) {
        return chatMessageMapper.insert(chatMessage) > 0 && pushService.addToMessageQueue(chatMessage);
    }

    @Transactional
    @Override
    public boolean addMessage(ChatMessage chatMessage, MultipartFile file) {
        String userId = chatMessage.getSenderId().toString();
        String receiverId = chatMessage.getReceiverId().toString();
        String groupId = chatMessage.getGroupId().toString();
        String receiverType = chatMessage.getReceiverType();
        Path path = getPath(userId, receiverId, groupId, receiverType, file);
        Path savedPath = saveFile(file, path);
        chatMessage.setFilePath(savedPath.toString());
        return addMessage(chatMessage);
    }

    @Override
    public List<ChatMessage> getChatMessagesAfterTime(Integer userId, Timestamp time) {
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiver_id", userId)
                .gt("UNIX_TIMESTAMP(send_time)", time.getTime());
        return chatMessageMapper.selectList(queryWrapper);
    }

    @Override
    public File getFileByMessageId(Integer messageId) {
        ChatMessage chatMessage = chatMessageMapper.selectById(messageId);
        if (chatMessage == null) {
            return null;
        }
        if (chatMessage.getFilePath() == null || chatMessage.getFilePath().isEmpty()) {
            return null;
        }
        return new File(chatMessage.getFilePath());
    }

    private Path getPath(String userId, String receiverId, String groupId, String receiverType, MultipartFile file) {
        Path chatBasePath = Path.of(fileRootPath, "file", "chat");
        if (receiverType.equals("user")) {
            Path path = Path.of(chatBasePath.toString(), "user", receiverId, userId, file.getOriginalFilename());
            // 上级目录不存在则创建
            if (!path.getParent().toFile().exists()) {
                try {
                    Files.createDirectories(path.getParent());
                } catch (IOException e) {
                    log.error("Failed to create directories", e);
                }
            }
            return path;
        } else {
            Path path = Path.of(chatBasePath.toString(), "group", groupId, file.getOriginalFilename());
            if (!path.getParent().toFile().exists()) {
                try {
                    Files.createDirectories(path.getParent());
                } catch (IOException e) {
                    log.error("Failed to create directories", e);
                }
            }
            return path;
        }
    }

    private Path saveFile(MultipartFile file, Path path) {
        File dest = path.toFile();
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        int i = 1;
        while (dest.exists()) {
            String front = path.toString().split("\\.")[0];
            String back = path.toString().split("\\.")[1];
            dest = new File(front + "(" + i + ")." + back);
            i++;
        }
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("Failed to save file", e);
        }
        return dest.toPath();
    }
}
