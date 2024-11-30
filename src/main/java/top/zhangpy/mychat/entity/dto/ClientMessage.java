package top.zhangpy.mychat.entity.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ClientMessage implements Serializable {
    private Integer senderId;
    private Integer receiverId;
    private String content;
    private int messageType; // 0: text, 1: image, 2: file
    private int receiverType; // 0: user, 1: group

}
