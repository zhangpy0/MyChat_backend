package top.zhangpy.mychat.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

@Data
public class ServerMessage implements Serializable {
    private Integer senderId;
    private Integer receiverId;
    private Integer groupId; // 0: user, other: group
    private String content;
    private Long time;
    private String messageType; // 0: text, 1: image, 2: file

    public ServerMessage(Integer senderId, Integer receiverId, Integer groupId, String content, String messageType) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.groupId = groupId;
        this.content = content;
        this.time = Instant.now().getEpochSecond();
        this.messageType = messageType;
    }
}
