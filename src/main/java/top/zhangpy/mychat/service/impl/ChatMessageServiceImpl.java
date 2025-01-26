package top.zhangpy.mychat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String targetDir = dest.getParent();
        String fileName = dest.getName();
        File targetFile = dest;
        String targetPath = targetFile.getAbsolutePath();

        while (targetFile.exists()) {
            // 检查是否已有类似 "(1)" 的数字后缀
            int dotIndex = fileName.lastIndexOf(".");
            String baseName;
            String extension = "";

            if (dotIndex != -1) {
                baseName = fileName.substring(0, dotIndex);
                extension = fileName.substring(dotIndex);
            } else {
                baseName = fileName;
            }

            // 使用正则匹配 "(数字)" 后缀
            String pattern = "^(.*)\\((\\d+)\\)$";
            Pattern regex = java.util.regex.Pattern.compile(pattern);
            Matcher matcher = regex.matcher(baseName);

            int number = 1;
            if (matcher.matches()) {
                // 如果已包含数字后缀，提取基础名和数字
                baseName = matcher.group(1).trim();
                number = Integer.parseInt(matcher.group(2)) + 1;
            }

            // 添加或更新数字后缀
            fileName = baseName + "(" + number + ")" + extension;
            targetFile = new File(targetDir, fileName);
            targetPath = targetFile.getAbsolutePath();
        }
        Path targetPathObj = Path.of(targetPath);
        try {
            file.transferTo(targetFile);
        } catch (IOException e) {
            log.error("Failed to save file", e);
        }
        return targetPathObj;
    }
}
