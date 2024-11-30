package top.zhangpy.mychat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import top.zhangpy.mychat.entity.po.User;
import top.zhangpy.mychat.entity.po.UserProfile;
import top.zhangpy.mychat.entity.vo.Result;
import top.zhangpy.mychat.service.UserProfileService;
import top.zhangpy.mychat.service.UserService;
import top.zhangpy.mychat.util.StringJudge;
import top.zhangpy.mychat.util.auth.JWTUtils;
import top.zhangpy.mychat.util.auth.PassToken;

import java.util.Map;

@RestController
public class UserAccountController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PassToken
    @Operation(summary = "注册")
    @PostMapping("/register")
    public Result doRegister(
            @Parameter(name = "json", description = "邮箱email,验证码authCode,用户id userId,密码哈希passwordHash", required = true)
            @RequestBody Map<String, String> map
            ) {
        String email = map.get("email");
        String authCode = map.get("authCode");
        Integer userId = Integer.valueOf(map.get("userId"));
        String passwordHash = map.get("passwordHash");
        User userFromId = userService.getUserByUserId(userId);
        if (userFromId != null) {
            return Result.fail(401, "userId已存在", null);
        }
        User userFromEmail = userService.getUserByEmail(email);
        if (userFromEmail != null) {
            return Result.fail(402, "邮箱已被注册", null);
        }
        String redisAuthCode = redisTemplate.opsForValue().get(email);
        if (redisAuthCode == null || !redisAuthCode.equals(authCode)) {
            return Result.fail(403, "验证码错误或超时", null);
        }
        User user = new User();
        user.setUserId(userId);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        boolean isRegister = userService.addUser(user);
        if (isRegister) {
            UserProfile userProfile = new UserProfile();
            userProfile.setUserId(userId);
            userProfile.setNickname("user_" + userId);
            if (userProfileService.addUserProfile(userProfile)) {
                return Result.ok(null, "注册成功");
            } else {
                return Result.fail(405, "注册失败", null);
            }
        } else {
            return Result.fail(405, "注册失败", null);
        }
    }

    @PassToken
    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result doLogin(
            @Parameter(name = "json", description = "userId/email,密码哈希passwordHash", required = true)
            @RequestBody Map<String, String> map
    ) {
        String userIdOrEmail = map.get("userId");
        User user = null;
        if (StringJudge.isEmail(userIdOrEmail)) {
            user = userService.getUserByEmail(userIdOrEmail);
            if (user == null) {
                return Result.fail(406, "邮箱未注册", null);
            }
            if (!user.getPasswordHash().equals(map.get("passwordHash"))) {
                return Result.fail(407, "密码错误", null);
            }
        } else {
            user = userService.getUserByUserId(Integer.valueOf(userIdOrEmail));
            if (user == null) {
                return Result.fail(408, "userId不存在", null);
            }
            if (!user.getPasswordHash().equals(map.get("passwordHash"))) {
                return Result.fail(407, "密码错误", null);
            }
        }
        // 生成token 保存到redis 7天
        String token = JWTUtils.getJWTToken(String.valueOf(user.getUserId()));
        redisTemplate.opsForValue().set(String.valueOf(user.getUserId()) , token, 7 * 24 * 60 * 60, java.util.concurrent.TimeUnit.SECONDS);

        Map<String, String> userAccount =  Map.of("userId", String.valueOf(user.getUserId()),
                "email", user.getEmail(),
                "passwordHash", user.getPasswordHash(),
                "token", token
        );
        return Result.ok(userAccount, "登录成功");
    }

    @PassToken
    @Operation(summary = "修改密码")
    @PostMapping("/changePassword")
    public Result doChangePassword(
            @Parameter(name = "json", description = "email,验证码authCode,新密码哈希newPasswordHash", required = true)
            @RequestBody Map<String, String> map
    ) {
        String email = map.get("email");
        String authCode = map.get("authCode");
        String newPasswordHash = map.get("newPasswordHash");
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return Result.fail(406, "邮箱未注册", null);
        }
        String redisAuthCode = redisTemplate.opsForValue().get(email);
        if (redisAuthCode == null || !redisAuthCode.equals(authCode)) {
            return Result.fail(403, "验证码错误或超时", null);
        }
        user.setPasswordHash(newPasswordHash);
        boolean isChange = userService.updateById(user);
        if (isChange) {
            return Result.ok(null, "修改密码成功");
        } else {
            return Result.fail(410, "修改密码失败", null);
        }
    }

    @PassToken
    @Operation(summary = "效验token")
    @PostMapping("/check")
    public Result checkToken(
            @Parameter(name = "json", description = "userId", required = true)
            @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) {
        String userId = map.get("userId");
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        return Result.ok(null, "token有效");
    }
}
