package top.zhangpy.mychat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.zhangpy.mychat.entity.po.Group;
import top.zhangpy.mychat.entity.po.GroupInfo;
import top.zhangpy.mychat.entity.po.GroupMember;
import top.zhangpy.mychat.mapper.GroupInfoMapper;
import top.zhangpy.mychat.mapper.GroupMapper;
import top.zhangpy.mychat.mapper.GroupMemberMapper;
import top.zhangpy.mychat.service.GroupService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {

    @Value("${project.folder}")
    String fileRootPath;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private GroupInfoMapper groupInfoMapper;

    @Autowired
    private GroupMemberMapper groupMemberMapper;

    @Override
    public Group getGroupByGroupId(Integer groupId) {
        return groupMapper.selectById(groupId);
    }

    @Override
    public GroupInfo getGroupInfoByGroupId(Integer groupId) {
        return groupInfoMapper.selectById(groupId);
    }

    @Override
    @Transactional
    public boolean createGroup(Integer creatorId) {
        Group group = new Group();
        group.setCreatorId(creatorId);
        if (groupMapper.insert(group) == 1) {
            QueryWrapper<Group> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .eq(Group::getCreatorId, creatorId)
                    .orderByDesc(Group::getCreatedAt); // 按 createdAt 降序排列
            group = groupMapper.selectOne(queryWrapper.last("LIMIT 1"));

            GroupInfo groupInfo = new GroupInfo();
            groupInfo.setGroupId(group.getGroupId());
            groupInfo.setGroupName("群聊_"+group.getGroupId());
            groupInfo.setAnnouncement("");
            groupInfo.setAvatarPath("");
            GroupMember groupMember = new GroupMember();
            groupMember.setGroupId(group.getGroupId());
            groupMember.setUserId(creatorId);
            groupMember.setRole("owner");
            return groupInfoMapper.insert(groupInfo) == 1 && groupMemberMapper.insert(groupMember) == 1;
        } else {
            return false;
        }
    }

    @Override
    public boolean updateGroupInfo(GroupInfo groupInfo) {
        return groupInfoMapper.updateById(groupInfo) == 1;
    }

    @Override
    public boolean updateGroupAvatar(Integer groupId, MultipartFile file) throws IOException {
        Path groupFolderPath = Path.of(fileRootPath + File.separator + "file" + File.separator + "group" + File.separator + String.valueOf(groupId));
        Path newAvatarPath = Path.of(groupFolderPath + File.separator + file.getOriginalFilename());
        File newAvatarFile = new File(newAvatarPath.toUri());
        if (!groupFolderPath.equals(newAvatarFile.getParentFile().toPath())) {
            return false;
        }
        if (!newAvatarFile.getParentFile().exists()) {
            boolean mkdirs = newAvatarFile.getParentFile().mkdirs();
        }
        file.transferTo(newAvatarFile);
        GroupInfo groupInfo = getGroupInfoByGroupId(groupId);
        groupInfo.setAvatarPath(String.valueOf(newAvatarPath));
        return updateGroupInfo(groupInfo);
    }

    @Override
    public File getGroupAvatar(Integer groupId) {
        GroupInfo groupInfo = getGroupInfoByGroupId(groupId);
        if (groupInfo == null) {
            return null;
        }
        return new File(groupInfo.getAvatarPath());
    }

    @Override
    public List<Group> getGroupsByCreatorId(Integer creatorId) {
        QueryWrapper<Group> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Group::getCreatorId, creatorId);
        return groupMapper.selectList(queryWrapper);
    }

    @Transactional
    @Override
    public boolean deleteGroup(Integer groupId) {
        QueryWrapper<GroupMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(GroupMember::getGroupId, groupId);
        groupMemberMapper.delete(queryWrapper);
        return groupMapper.deleteById(groupId) == 1;
    }
}
