package top.zhangpy.mychat.service;

import top.zhangpy.mychat.entity.po.ContactApply;
import top.zhangpy.mychat.entity.po.Friend;
import top.zhangpy.mychat.entity.po.Group;
import top.zhangpy.mychat.entity.po.User;

import java.util.List;

public interface ContactService {

    boolean addApplyToFriend(Integer userId, Integer friendId, String message);

    boolean addApplyToGroup(Integer userId, Integer groupId, Integer ownerId, String message);

    boolean processFriendApply(Integer userId, Integer friendId, int status);

    boolean processGroupApply(Integer userId, Integer groupId, Integer ownerId, int status);

    List<ContactApply> getContactApplyFromOthers(Integer userId);

    List<ContactApply> getContactApplyFromMe(Integer userId);

    boolean addFriend(Integer userId, Integer friendId);

    boolean updateFriendStatus(Integer userId, Integer friendId, Integer status);

    boolean deleteFriend(Integer userId, Integer friendId);

    boolean addToGroup(Integer userId, Integer groupId);

    boolean deleteFromGroup(Integer userId, Integer groupId);

    List<Friend> getFriends(Integer userId);

    List<Integer> getGroupMembers(Integer groupId);

    Integer getGroupOwner(Integer groupId);

    List<Group> getGroups(Integer userId);

    boolean addFriendToGroup(Integer friendId, Integer groupId);

    boolean deleteFriendFromGroup(Integer friendId, Integer groupId);
}
