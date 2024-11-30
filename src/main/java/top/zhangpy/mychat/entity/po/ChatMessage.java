package top.zhangpy.mychat.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Map;

@Data
@TableName("chat_messages")
public class ChatMessage {

    @TableId(value = "message_id", type = IdType.AUTO)
    private Integer messageId;

    @TableField("sender_id")
    private Integer senderId;

    @TableField("receiver_id")
    private Integer receiverId;

    @TableField("group_id")
    private Integer groupId;

    @TableField("receiver_type")
    private String receiverType;

    @TableField("send_time")
    private Timestamp sendTime;

    @TableField("content")
    private String content;

    @TableField("message_type")
    private String messageType;

    @TableField("file_path")
    private String filePath;

    public void nullToEmpty() {
        if (this.content == null) {
            this.content = "";
        }
        if (this.filePath == null) {
            this.filePath = "";
        }
        if (this.groupId == null) {
            this.groupId = 0;
        }
        if (this.receiverId == null) {
            this.receiverId = 0;
        }
    }

    public Map<String, String> toMap() {
        return Map.of(
                "messageId", String.valueOf(this.messageId),
                "senderId", String.valueOf(this.senderId),
                "receiverId", String.valueOf(this.receiverId),
                "groupId", String.valueOf(this.groupId),
                "receiverType", this.receiverType,
                "sendTime", String.valueOf(sendTime.getTime()),
                "content", this.content,
                "messageType", this.messageType,
                "filePath", this.filePath
        );
    }
}
