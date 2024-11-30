package top.zhangpy.mychat.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("users")
public class User {

    @TableId
    @TableField("user_id")
    private Integer userId;

    @TableField("password_hash")
    private String passwordHash;

    @TableField("email")
    private String email;
}
