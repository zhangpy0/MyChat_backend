package top.zhangpy.mychat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.zhangpy.mychat.entity.po.UserProfile;
import top.zhangpy.mychat.entity.vo.Result;
import top.zhangpy.mychat.service.UserProfileService;
import top.zhangpy.mychat.util.auth.JWTUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/profile")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JWTUtils jwtUtils;

    @Operation(summary = "获取用户信息")
    @PostMapping("/getUserProfile")
    public Result getUserProfile(
            @Parameter(description = "用户id", name = "userId", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token) {

        String userId = map.get("userId");
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        UserProfile userProfile = userProfileService.getUserProfileByUserId(Integer.valueOf(userId));
        if (userProfile == null) {
            return Result.fail(412, "User profile not found", null);
        }
        userProfile.nullToEmpty();
        Map<String, String> res = Map.of(
                "userId", String.valueOf(userProfile.getUserId()),
                "nickname", userProfile.getNickname(),
                "gender", userProfile.getGender(),
                "region", userProfile.getRegion()
        );

        return Result.ok(res, "success");
    }

    @Operation(summary = "修改用户信息(昵称)")
    @PostMapping("/updateUserNickname")
    public Result updateUserNickname(
            @Parameter(description = "用户id,新昵称", name = "userId,nickname", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token) {

        String userId = map.get("userId");
        String nickname = map.get("nickname");
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        if (nickname == null) {
            return Result.fail(408, "nickname is null", null);
        }

        UserProfile userProfile = userProfileService.getUserProfileByUserId(Integer.valueOf(userId));
        if (userProfile == null) {
            return Result.fail(412, "User profile not found", null);
        }

        userProfile.setNickname(nickname);
        boolean isUpdated = userProfileService.updateUserProfile(userProfile);
        if (isUpdated) {
            return Result.ok(null, "success");
        } else {
            return Result.fail(413, "Failed to update user profile", null);
        }
    }

    @Operation(summary = "修改用户信息(性别)")
    @PostMapping("/updateUserGender")
    public Result updateUserGender(
            @Parameter(description = "用户id,新性别", name = "userId, gender", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token) {

        String userId = map.get("userId");
        String gender = map.get("gender");
        if (gender == null) {
            return Result.fail(408, "nickname is null", null);
        }
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }

        UserProfile userProfile = userProfileService.getUserProfileByUserId(Integer.valueOf(userId));
        if (userProfile == null) {
            return Result.fail(412, "User profile not found", null);
        }

        List<String> genderList = List.of("male","female", "");
        if (!genderList.contains(gender)) {
            return Result.fail(414, "Invalid gender", null);
        }
        userProfile.setGender(gender);
        boolean isUpdated = userProfileService.updateUserProfile(userProfile);
        if (isUpdated) {
            return Result.ok(null, "success");
        } else {
            return Result.fail(413, "Failed to update user profile", null);
        }
    }

    @Operation(summary = "修改用户信息(地区)")
    @PostMapping("/updateUserRegion")
    public Result updateUserRegion(
            @Parameter(description = "用户id,新地区", name = "userId, region", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token) {

        String userId = map.get("userId");
        String region = map.get("region");
        if (region == null) {
            return Result.fail(408, "region is null", null);
        }
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }

        UserProfile userProfile = userProfileService.getUserProfileByUserId(Integer.valueOf(userId));
        if (userProfile == null) {
            return Result.fail(412, "User profile not found", null);
        }

        userProfile.setRegion(region);
        boolean isUpdated = userProfileService.updateUserProfile(userProfile);
        if (isUpdated) {
            return Result.ok(null, "success");
        } else {
            return Result.fail(413, "Failed to update user profile", null);
        }
    }

    @Operation(summary = "获取用户信息(头像)")
    @PostMapping("/getUserAvatar")
    public Result getUserAvatar(
            @Parameter(description = "用户id", name = "userId", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token) throws IOException {

        String userId = map.get("userId");
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }

        UserProfile userProfile = userProfileService.getUserProfileByUserId(Integer.valueOf(userId));
        if (userProfile == null) {
            return Result.fail(412, "User profile not found", null);
        }

        File avatarFile = userProfileService.getAvatarFileByUserId(Integer.valueOf(userId));
        FileInputStream fileInputStream = new FileInputStream(avatarFile);
        final byte[] avatarBytes = IOUtils.toByteArray(fileInputStream);
        fileInputStream.close();
        String avatarBase64 = Base64.getEncoder().encodeToString(avatarBytes);
        Map<String, String> res = Map.of(
                "userId", String.valueOf(userProfile.getUserId()),
                "avatar", avatarBase64
        );
        return Result.ok(res, "success");
    }

    @Operation(summary = "修改用户信息(头像)")
    @PostMapping(value = "/updateUserAvatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result updateUserAvatar(
            @Parameter(description = "用户id", name = "userId", required = true) @RequestParam("userId") String userId,
            @Parameter(description = "file", name = "avatar", required = true) @RequestParam("avatar") MultipartFile file,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token) throws IOException {

        if (file == null) {
            return Result.fail(408, "file is null", null);
        }
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }

        UserProfile userProfile = userProfileService.getUserProfileByUserId(Integer.valueOf(userId));
        if (userProfile == null) {
            return Result.fail(412, "User profile not found", null);
        }

        boolean isUpdated = userProfileService.updateUserProfileAvatar(Integer.valueOf(userId), file);
        if (isUpdated) {
            return Result.ok(null, "success");
        } else {
            return Result.fail(413, "Failed to update user profile", null);
        }
    }
}
