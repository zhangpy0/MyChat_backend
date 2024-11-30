package top.zhangpy.mychat.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("group_info")
public class GroupInfo {

    @TableId
    @TableField("group_id")
    private Integer groupId;

    @TableField("group_name")
    private String groupName;

    @TableField("announcement")
    private String announcement;

    @TableField("avatar_path")
    private String avatarPath;
}
