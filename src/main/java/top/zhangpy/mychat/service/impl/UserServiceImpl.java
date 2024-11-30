package top.zhangpy.mychat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.zhangpy.mychat.entity.po.User;
import top.zhangpy.mychat.mapper.UserMapper;
import top.zhangpy.mychat.service.UserService;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User getUserByUserIdAndPassWord(Integer userId, String passwordHash) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("password_hash", passwordHash);
        return getOne(queryWrapper);
    }

    @Override
    public User getUserByEmailAndPassWord(String email, String passwordHash) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        queryWrapper.eq("password_hash", passwordHash);
        return getOne(queryWrapper);
    }

    @Override
    public User getUserByEmail(String email) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        return getOne(queryWrapper);
    }

    @Override
    public User getUserByUserId(Integer userId) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return getOne(queryWrapper);
    }

    @Override
    public boolean changePassword(String email, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        User user = getOne(queryWrapper);
        if (user == null) {
            return false;
        }
        user.setPasswordHash(password);
        return updateById(user);
    }

    @Override
    public boolean addUser(User user) {
        return save(user);
    }
}
