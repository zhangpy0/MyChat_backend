package top.zhangpy.mychat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.zhangpy.mychat.entity.po.Group;
import top.zhangpy.mychat.entity.po.GroupInfo;

@Mapper
public interface GroupMapper extends BaseMapper<Group> {
}
