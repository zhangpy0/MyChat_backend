package top.zhangpy.mychat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.zhangpy.mychat.entity.po.ChatMessage;
import top.zhangpy.mychat.entity.vo.Result;
import top.zhangpy.mychat.service.ChatMessageService;
import top.zhangpy.mychat.util.auth.JWTUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private static final Log log = LogFactory.getLog(ChatController.class);
    @Autowired
    private ChatMessageService chatMessageService;

    @Operation(summary = "发送消息")
    @PostMapping(value = "/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result sendMsg(
            @Parameter(description = "用户id", name = "userId", required = true) @RequestParam("userId") String userId,
            @Parameter(description = "接收者id", name = "receiverId" , required = false) @RequestParam(value = "receiverId", required = false) String receiverId,
            @Parameter(description = "群组id", name = "groupId", required = false) @RequestParam(value = "groupId", required = false) String groupId,
            @Parameter(description = "接收者类型", name = "receiverType", required = true) @RequestParam("receiverType") String receiverType,
            @Parameter(description = "消息内容", name = "content", required = false) @RequestParam(value = "content", required = false) String content,
            @Parameter(description = "消息类型", name = "messageType", required = true) @RequestParam("messageType") String messageType,
            @Parameter(description = "文件", name = "file", required = false) @RequestParam(value = "file", required = false) MultipartFile file,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) {
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        Integer senderId = Integer.valueOf(userId);
        Integer receiver = null;
        if (receiverId != null && !receiverId.isEmpty() && !receiverId.equals("null")) {
            receiver = Integer.valueOf(receiverId);
        }
        if (groupId == null) {
            groupId = "0";
        }
        Integer group = Integer.valueOf(groupId);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(senderId);
        chatMessage.setReceiverId(receiver);
        chatMessage.setGroupId(group);
        chatMessage.setReceiverType(receiverType);
        if (messageType.equals("text")) {
            chatMessage.setContent(content);
        } else {
            chatMessage.setContent("");
        }
        chatMessage.setMessageType(messageType);
        String filePath = "";
        if (messageType.equals("text")) {
            if (chatMessageService.addMessage(chatMessage)) {
                return Result.ok(null, "success");
            } else {
                return Result.fail(701, "Failed to send message", null);
            }
        } else {
            if (file == null) {
                return Result.fail(501, "File is required", null);
            }
            if (chatMessageService.addMessage(chatMessage, file)) {
                return Result.ok(null, "success");
            } else {
                return Result.fail(701, "Failed to send message", null);
            }
        }
    }

    @Operation(summary = "获取消息")
    @PostMapping("/update")
    public Result getMsg(
            @Parameter(description = "用户id", name = "userId", required = true) @RequestParam("userId") String userId,
            @Parameter(description = "unix时间戳(s)", name = "time", required = true) @RequestParam("time") Long time,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) {
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        Timestamp timestamp = Timestamp.from(Instant.ofEpochMilli(time));
        List<ChatMessage> chatMessages = chatMessageService.getChatMessagesAfterTime(Integer.valueOf(userId), timestamp);
        List<Map<String, String>> messages = new ArrayList<>();
        for (ChatMessage chatMessage : chatMessages) {
            chatMessage.nullToEmpty();
            chatMessage.setFilePath("");
            messages.add(chatMessage.toMap());
        }
        return Result.ok(messages, "success");
    }

    // TODO 加userId与messageId的校验
    @Operation(summary = "下载文件")
    @PostMapping("/download")
    public Result downloadFile(
            @Parameter(description = "用户id", name = "userId", required = true) @RequestParam("userId") String userId,
            @Parameter(description = "消息id", name = "messageId", required = true) @RequestParam("messageId") String messageId,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token,
            HttpServletResponse response
    ) {
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        File file = chatMessageService.getFileByMessageId(Integer.valueOf(messageId));
        if (file == null) {
            return Result.fail(702, "File not found", null);
        }
        String fileName = file.getName();
        int length = (int) file.length();
        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setContentLength(length);
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        try (FileInputStream fis = new FileInputStream(file)) {
            IOUtils.copy(fis, response.getOutputStream());
        } catch (IOException e) {
            log.error("Failed to download file", e);
            return Result.fail(703, "Failed to download file", null);
        }
        return null;
    }

    @Operation(summary = "获取文件大小")
    @PostMapping("/fileInfo")
    public Result getFileInfo(
            @Parameter(description = "用户id", name = "userId", required = true) @RequestParam("userId") String userId,
            @Parameter(description = "消息id", name = "messageId", required = true) @RequestParam("messageId") String messageId,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) {
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        File file = chatMessageService.getFileByMessageId(Integer.valueOf(messageId));
        if (file == null) {
            return Result.fail(702, "File not found", null);
        }
        Map<String, String> fileInfo = Map.of("size", String.valueOf(file.length())
        , "name", file.getName());
        return Result.ok(fileInfo, "success");
    }
}
