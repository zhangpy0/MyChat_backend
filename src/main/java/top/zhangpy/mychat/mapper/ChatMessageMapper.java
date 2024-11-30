package top.zhangpy.mychat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.zhangpy.mychat.entity.po.ChatMessage;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
