package top.zhangpy.mychat.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("`groups`")
public class Group {

    @TableId(value = "group_id", type = IdType.AUTO)
    private Integer groupId;

    @TableField("creator_id")
    private Integer creatorId;

    @TableField("created_at")
    private Timestamp createdAt;

}
