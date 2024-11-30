package top.zhangpy.mychat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.zhangpy.mychat.entity.po.*;
import top.zhangpy.mychat.mapper.ContactApplyMapper;
import top.zhangpy.mychat.mapper.FriendMapper;
import top.zhangpy.mychat.mapper.GroupMapper;
import top.zhangpy.mychat.mapper.GroupMemberMapper;
import top.zhangpy.mychat.service.ContactService;
import top.zhangpy.mychat.service.PushService;

import java.util.List;

@Service
public class ContactServiceImpl implements ContactService {

    @Autowired
    private ContactApplyMapper contactApplyMapper;

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private GroupMemberMapper groupMemberMapper;

    @Autowired
    private PushService pushService;


    @Override
    public boolean addApplyToFriend(Integer userId, Integer friendId, String message) {
        ContactApply contactApply = new ContactApply();
        contactApply.setApplicantId(userId);
        contactApply.setReceiverId(friendId);
        contactApply.setMessage(message);
        contactApply.setContactType("friend");
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(0);
        chatMessage.setGroupId(0);
        chatMessage.setReceiverId(friendId);
        chatMessage.setReceiverType("user");
        chatMessage.setMessageType("text");
        chatMessage.setContent(userId + ":friend request get");
        chatMessage.nullToEmpty();
        return contactApplyMapper.insert(contactApply) == 1 && pushService.addToMessageQueue(chatMessage);
    }

    @Override
    public boolean addApplyToGroup(Integer userId, Integer groupId, Integer ownerId, String message) {
        ContactApply contactApply = new ContactApply();
        contactApply.setApplicantId(userId);
        contactApply.setReceiverId(ownerId);
        contactApply.setGroupId(groupId);
        contactApply.setMessage(message);
        contactApply.setContactType("group");
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(0);
        chatMessage.setGroupId(0);
        chatMessage.setReceiverId(ownerId);
        chatMessage.setReceiverType("user");
        chatMessage.setMessageType("text");
        chatMessage.setContent(userId + ":" + groupId + ":group request get");
        chatMessage.nullToEmpty();
        return contactApplyMapper.insert(contactApply) == 1 && pushService.addToMessageQueue(chatMessage);
    }

    // 处理friendId -> userId的好友申请
    @Transactional
    @Override
    public boolean processFriendApply(Integer userId, Integer friendId, int status) {
        QueryWrapper<ContactApply> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ContactApply::getApplicantId, friendId)
                .eq(ContactApply::getReceiverId, userId)
                .eq(ContactApply::getContactType, "friend")
                .orderByDesc(ContactApply::getApplyTime) // 按 ApplyTime 降序排列
                .last("LIMIT 1"); // 仅取最晚一条记录
        ContactApply contactApply = contactApplyMapper.selectOne(queryWrapper);
        if (contactApply == null) {
            throw new RuntimeException("No such apply");
        }
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(0);
        chatMessage.setGroupId(0);
        chatMessage.setReceiverId(friendId);
        chatMessage.setReceiverType("user");
        chatMessage.setMessageType("text");
        chatMessage.setContent(userId + ":friend request has been processed");
        chatMessage.nullToEmpty();
        if (status == 1) {
            contactApply.setStatus("approved");
            contactApplyMapper.updateById(contactApply);
            return addFriend(userId, friendId) && pushService.addToMessageQueue(chatMessage);
        }
        if (status == 0) {
            contactApply.setStatus("rejected");
            return contactApplyMapper.updateById(contactApply) == 1 && pushService.addToMessageQueue(chatMessage);
        }
        return false;
    }

    // ownerId处理userId加入groupId的群聊申请 0拒绝 1同意
    @Transactional
    @Override
    public boolean processGroupApply(Integer userId, Integer groupId, Integer ownerId, int status) {
        QueryWrapper<ContactApply> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ContactApply::getApplicantId, userId)
                .eq(ContactApply::getReceiverId, ownerId)
                .eq(ContactApply::getGroupId, groupId)
                .eq(ContactApply::getContactType, "group")
                .orderByDesc(ContactApply::getApplyTime) // 按 ApplyTime 降序排列
                .last("LIMIT 1"); // 仅取最晚一条记录

        ContactApply contactApply = contactApplyMapper.selectOne(queryWrapper);
        if (contactApply == null) {
            throw new RuntimeException("No such apply");
        }
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(0);
        chatMessage.setGroupId(0);
        chatMessage.setReceiverId(userId);
        chatMessage.setReceiverType("user");
        chatMessage.setMessageType("text");
        chatMessage.setContent(groupId + ":group request has been processed");
        chatMessage.nullToEmpty();
        if (status == 1) {
            contactApply.setStatus("approved");
            contactApplyMapper.updateById(contactApply);
            return addToGroup(userId, groupId) && pushService.addToMessageQueue(chatMessage);
        }
        if (status == 0) {
            contactApply.setStatus("rejected");
            return contactApplyMapper.updateById(contactApply) == 1 && pushService.addToMessageQueue(chatMessage);
        }
        return false;
    }


    @Override
    public List<ContactApply> getContactApplyFromOthers(Integer userId) {
        QueryWrapper<ContactApply> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ContactApply::getReceiverId, userId);
        return contactApplyMapper.selectList(queryWrapper);
    }

    @Override
    public List<ContactApply> getContactApplyFromMe(Integer userId) {
        QueryWrapper<ContactApply> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ContactApply::getApplicantId, userId);
        return contactApplyMapper.selectList(queryWrapper);
    }

    @Transactional
    @Override
    public boolean addFriend(Integer userId, Integer friendId) {
        Friend friend1 = new Friend();
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId);
        Friend friend1Exist = friendMapper.selectOne(queryWrapper);

        Friend friend2 = new Friend();
        queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId, friendId)
                .eq(Friend::getFriendId, userId);
        Friend friend2Exist = friendMapper.selectOne(queryWrapper);

        friend1.setUserId(userId);
        friend1.setFriendId(friendId);
        friend2.setUserId(friendId);
        friend2.setFriendId(userId);

        boolean result = false;

        if (friend1Exist != null) {
            friend1Exist.setStatus("1");
            result = friendMapper.updateById(friend1Exist) == 1;
        } else {
            friend1.setStatus("1");
            result = friendMapper.insert(friend1) == 1;
        }

        if (friend2Exist != null) {
            friend2Exist.setStatus("1");
            friendMapper.updateById(friend2Exist);
            result = result && friendMapper.updateById(friend2Exist) == 1;
        } else {
            friend2.setStatus("1");
            result = result && friendMapper.insert(friend2) == 1;
        }
        return result;
    }

    // user send to friend
    @Transactional
    @Override
    public boolean updateFriendStatus(Integer userId, Integer friendId, Integer status) {
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId);
        Friend friend = friendMapper.selectOne(queryWrapper);
        if (friend == null) {
            throw new RuntimeException("No such friend");
        }
        friend.setStatus(String.valueOf(status));
        return friendMapper.updateById(friend) == 1;
    }

    @Transactional
    @Override
    public boolean deleteFriend(Integer userId, Integer friendId) {
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId);
        Friend friend1 = friendMapper.selectOne(queryWrapper);
        queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId, friendId)
                .eq(Friend::getFriendId, userId);
        Friend friend2 = friendMapper.selectOne(queryWrapper);
        if (friend1 == null) {
            throw new RuntimeException("No such friend");
        }
        if (friend2 != null) {
            friend2.setStatus("0");
            friendMapper.updateById(friend2);
        }
        return friendMapper.deleteById(friend1.getId()) == 1;
    }

    @Transactional
    @Override
    public boolean addToGroup(Integer userId, Integer groupId) {
        QueryWrapper<GroupMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(GroupMember::getUserId, userId)
                .eq(GroupMember::getGroupId, groupId);
        GroupMember groupMember = groupMemberMapper.selectOne(queryWrapper);
        if (groupMember != null) {
            return false;
        }
        groupMember = new GroupMember();
        groupMember.setUserId(userId);
        groupMember.setGroupId(groupId);
        return groupMemberMapper.insert(groupMember) == 1;
    }

    @Transactional
    @Override
    public boolean deleteFromGroup(Integer userId, Integer groupId) {
        QueryWrapper<GroupMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(GroupMember::getUserId, userId)
                .eq(GroupMember::getGroupId, groupId);
        GroupMember groupMember = groupMemberMapper.selectOne(queryWrapper);
        if (groupMember == null) {
            throw new RuntimeException("No such member");
        }
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(0);
        chatMessage.setGroupId(0);
        chatMessage.setReceiverId(userId);
        chatMessage.setReceiverType("user");
        chatMessage.setMessageType("text");
        chatMessage.setContent(groupId + ":remove you from group");
        chatMessage.nullToEmpty();
        return groupMemberMapper.deleteById(groupMember) == 1 && pushService.addToMessageQueue(chatMessage);
    }

    @Override
    public List<Friend> getFriends(Integer userId) {
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId, userId);
        return friendMapper.selectList(queryWrapper);
    }

    @Override
    public List<Integer> getGroupMembers(Integer groupId) {
        QueryWrapper<GroupMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(GroupMember::getGroupId, groupId);
        List<GroupMember> groupMembers = groupMemberMapper.selectList(queryWrapper);
        return groupMembers.stream().map(GroupMember::getUserId).toList();
    }

    @Override
    public Integer getGroupOwner(Integer groupId) {
        QueryWrapper<Group> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Group::getGroupId, groupId);
        Group group = groupMapper.selectOne(queryWrapper);
        if (group == null) {
            throw new RuntimeException("No such group");
        }
        return group.getCreatorId();
    }

    @Transactional
    @Override
    public List<Group> getGroups(Integer userId) {
        QueryWrapper<GroupMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(GroupMember::getUserId, userId);
        List<GroupMember> groupMembers = groupMemberMapper.selectList(queryWrapper);
        return groupMembers.stream().map(groupMember -> {
            QueryWrapper<Group> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.lambda()
                    .eq(Group::getGroupId, groupMember.getGroupId());
            return groupMapper.selectOne(queryWrapper1);
        }).toList();
    }

    @Override
    public boolean addFriendToGroup(Integer friendId, Integer groupId) {
        QueryWrapper<GroupMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(GroupMember::getUserId, friendId)
                .eq(GroupMember::getGroupId, groupId);
        GroupMember groupMember = groupMemberMapper.selectOne(queryWrapper);
        if (groupMember != null) {
            return false;
        }
        groupMember = new GroupMember();
        groupMember.setUserId(friendId);
        groupMember.setGroupId(groupId);
        return groupMemberMapper.insert(groupMember) == 1;
    }

    @Override
    public boolean deleteFriendFromGroup(Integer friendId, Integer groupId) {
        QueryWrapper<GroupMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(GroupMember::getUserId, friendId)
                .eq(GroupMember::getGroupId, groupId);
        GroupMember groupMember = groupMemberMapper.selectOne(queryWrapper);
        if (groupMember == null) {
            throw new RuntimeException("No such member");
        }
        return groupMemberMapper.deleteById(groupMember) == 1;
    }
}
