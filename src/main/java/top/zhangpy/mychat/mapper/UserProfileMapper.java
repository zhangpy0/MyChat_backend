package top.zhangpy.mychat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.zhangpy.mychat.entity.po.UserProfile;

@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {

}
