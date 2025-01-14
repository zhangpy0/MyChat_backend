package top.zhangpy.mychat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.zhangpy.mychat.entity.po.UserProfile;
import top.zhangpy.mychat.mapper.UserProfileMapper;
import top.zhangpy.mychat.service.UserProfileService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile> implements UserProfileService {

    @Value("${project.folder}")
    String fileRootPath;

    @Override
    public UserProfile getUserProfileByUserId(Integer userId) {
        QueryWrapper<UserProfile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return getOne(queryWrapper);
    }

    @Override
    public boolean updateUserProfile(UserProfile userProfile) {
        UserProfile oldUserProfile = getUserProfileByUserId(userProfile.getUserId());
        if (oldUserProfile == null) {
            return false;
        }
        userProfile.setProfileId(oldUserProfile.getProfileId());
        return updateById(userProfile);
    }

    @Override
    public boolean addUserProfile(UserProfile userProfile) {
        return save(userProfile);
    }

    @Override
    public boolean updateUserProfileAvatar(Integer userId, MultipartFile file) throws IOException {
        Path userFolderPath = Path.of(fileRootPath + File.separator + "file" + File.separator + "user" + File.separator + String.valueOf(userId));
        Path newAvatarPath = Path.of(userFolderPath + File.separator + file.getOriginalFilename());
        File newAvatarFile = new File(newAvatarPath.toUri());
        if (!Files.isSameFile(userFolderPath, newAvatarFile.getParentFile().toPath())) {
            return false;
        }
        if (!newAvatarFile.getParentFile().exists()) {
            boolean mkdirs = newAvatarFile.getParentFile().mkdirs();
        }
        try {
            file.transferTo(newAvatarFile);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        UserProfile userProfile = getUserProfileByUserId(userId);
        userProfile.setAvatarPath(String.valueOf(newAvatarPath));
        return updateUserProfile(userProfile);
    }

    @Override
    public File getAvatarFileByUserId(Integer userId) {
        UserProfile userProfile = getUserProfileByUserId(userId);
        if (userProfile == null || userProfile.getAvatarPath() == null) {
            return null;
        }
        return new File(userProfile.getAvatarPath());
    }
}
