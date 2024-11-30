package top.zhangpy.mychat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;
import top.zhangpy.mychat.entity.po.Group;
import top.zhangpy.mychat.entity.po.GroupInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface GroupService extends IService<Group> {
    Group getGroupByGroupId(Integer groupId);

    GroupInfo getGroupInfoByGroupId(Integer groupId);

    boolean createGroup(Integer creatorId);

    boolean updateGroupInfo(GroupInfo groupInfo);

    boolean updateGroupAvatar(Integer groupId, MultipartFile file) throws IOException;

    File getGroupAvatar(Integer groupId);

    List<Group> getGroupsByCreatorId(Integer creatorId);

    boolean deleteGroup(Integer groupId);

}
