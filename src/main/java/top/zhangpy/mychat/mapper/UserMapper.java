package top.zhangpy.mychat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.zhangpy.mychat.entity.po.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
