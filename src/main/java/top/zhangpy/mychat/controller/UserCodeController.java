package top.zhangpy.mychat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.zhangpy.mychat.entity.vo.Result;
import top.zhangpy.mychat.service.UserService;
import top.zhangpy.mychat.util.auth.PassToken;

import java.util.Map;

@ApiResponse(description = "用户验证码")
@RestController
public class UserCodeController {

    @Autowired
    private JavaMailSenderImpl mailSender;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserService userService;

    @PassToken
    @Operation(summary = "注册时验证码")
    @PostMapping("/sendEmailForRegister")
    public Result sendEmailForRegister(
            @Parameter(description = "邮箱", name = "json", required = true)
            @RequestBody Map<String, String> map
            ) {
        String email = map.get("email");
        if (userService.getUserByEmail(email) != null) {
            return Result.fail(400, "邮箱已被注册", null);
        }
        int authCode = ((int) (Math.random() * 1000000) - 1);
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setSubject("[MyChat]注册验证码");
        mailMessage.setText("欢迎注册MyChat,您的验证码是：" + authCode);
        mailMessage.setFrom("1055803945@qq.com");
        mailMessage.setTo(email);
        mailSender.send(mailMessage);
        redisTemplate.opsForValue().set(email, String.valueOf(authCode), 300, java.util.concurrent.TimeUnit.SECONDS);
        return Result.ok(null, "验证码已发送");
    }

    @PassToken
    @Operation(summary = "修改密码时验证码")
    @PostMapping("/sendEmailForChangePassword")
    public Result sendEmailForChangePassword(
            @Parameter(description = "邮箱", name = "json", required = true)
            @RequestBody Map<String, String> map
    ) {
        String email = map.get("email");
        if (userService.getUserByEmail(email) == null) {
            return Result.fail(400, "邮箱未注册", null);
        }
        int authCode = ((int) (Math.random() * 1000000) - 1);
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setSubject("[MyChat]修改密码验证码");
        mailMessage.setText("您的验证码是：" + authCode);
        mailMessage.setFrom("1055803945@qq.com");
        mailMessage.setTo(email);
        mailSender.send(mailMessage);
        redisTemplate.opsForValue().set(email, String.valueOf(authCode), 300, java.util.concurrent.TimeUnit.SECONDS);
        return Result.ok(null, "验证码已发送");
    }
}
