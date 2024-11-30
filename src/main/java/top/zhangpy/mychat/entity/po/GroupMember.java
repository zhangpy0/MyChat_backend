package top.zhangpy.mychat.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("group_members")
public class GroupMember {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("group_id")
    private Integer groupId;

    @TableField("user_id")
    private Integer userId;

    @TableField("joined_at")
    private Timestamp joinedAt;

    @TableField("role")
    private String role;
}