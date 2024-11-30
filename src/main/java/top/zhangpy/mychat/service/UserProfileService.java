package top.zhangpy.mychat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import top.zhangpy.mychat.entity.po.UserProfile;

import java.io.File;
import java.io.IOException;

public interface UserProfileService extends IService<UserProfile> {

    UserProfile getUserProfileByUserId(Integer userId);

    boolean updateUserProfile(UserProfile userProfile);

    boolean addUserProfile(UserProfile userProfile);

    boolean updateUserProfileAvatar(Integer userId, MultipartFile file) throws IOException;

    File getAvatarFileByUserId(Integer userId);
}
