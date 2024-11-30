package top.zhangpy.mychat.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_profiles")
public class UserProfile {
    @TableId(value = "profile_id", type = IdType.AUTO)
    private Integer profileId;

    @TableField("user_id")
    private Integer userId;

    @TableField("avatar_path")
    private String avatarPath;

    @TableField("nickname")
    private String nickname;

    @TableField("gender")
    private String gender;

    @TableField("region")
    private String region;

    public void nullToEmpty() {
        if (avatarPath == null) {
            avatarPath = "";
        }
        if (nickname == null) {
            nickname = "";
        }
        if (gender == null) {
            gender = "";
        }
        if (region == null) {
            region = "";
        }
    }
}
