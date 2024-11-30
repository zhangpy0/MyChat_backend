package top.zhangpy.mychat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.zhangpy.mychat.entity.po.User;
import top.zhangpy.mychat.entity.po.UserProfile;
import top.zhangpy.mychat.mapper.UserMapper;
import top.zhangpy.mychat.service.UserProfileService;
import top.zhangpy.mychat.service.UserService;

import java.io.File;

@SpringBootTest
class MyChatApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserProfileService userProfileService;

    @Test
    void contextLoads() {
        System.out.println(File.separator);
    }

}
