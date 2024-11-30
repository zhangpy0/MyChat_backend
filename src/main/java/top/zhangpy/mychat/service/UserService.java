package top.zhangpy.mychat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.zhangpy.mychat.entity.po.User;

public interface UserService extends IService<User> {
    User getUserByUserIdAndPassWord(Integer userId, String passwordHash);

    User getUserByEmailAndPassWord(String email, String passwordHash);

    User getUserByEmail(String email);

    User getUserByUserId(Integer userId);

    boolean changePassword(String email, String password);

    boolean addUser(User user);
}
