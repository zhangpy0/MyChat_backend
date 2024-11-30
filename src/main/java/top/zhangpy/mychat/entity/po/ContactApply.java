package top.zhangpy.mychat.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.sql.Timestamp;

@Data
@TableName("contact_apply")
public class ContactApply {

    @TableId(value = "apply_id", type = IdType.AUTO)
    private Integer applyId;

    @TableField("applicant_id")
    private Integer applicantId;

    @TableField("contact_type")
    private String contactType;

    @TableField("receiver_id")
    private Integer receiverId;

    @TableField("group_id")
    private Integer groupId;

    @TableField("apply_time")
    private Timestamp applyTime;

    @TableField("status")
    private String status;

    @TableField("message")
    private String message;

    public enum ContactType {
        FRIEND, GROUP
    }

    public enum ApplyStatus {
        PENDING, APPROVED, REJECTED
    }

    public void nullToEmpty() {
        if (this.message == null) {
            this.message = "";
        }
        if (this.groupId == null) {
            this.groupId = 0;
        }
    }
}
