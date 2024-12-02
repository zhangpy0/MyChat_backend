package top.zhangpy.mychat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.zhangpy.mychat.entity.po.*;
import top.zhangpy.mychat.entity.vo.Result;
import top.zhangpy.mychat.mapper.GroupMapper;
import top.zhangpy.mychat.mapper.GroupMemberMapper;
import top.zhangpy.mychat.service.ContactService;
import top.zhangpy.mychat.service.GroupService;
import top.zhangpy.mychat.service.UserProfileService;
import top.zhangpy.mychat.service.UserService;
import top.zhangpy.mychat.util.auth.JWTUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contact")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private UserProfileService userProfileService;
    @Autowired
    private GroupMemberMapper groupMemberMapper;

    @Operation(summary = "搜索用户")
    @PostMapping("/searchUser")
    public Result searchUser(
            @Parameter(description = "用户id,好友id", name = "userId, friendId", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) throws IOException {
        String userId = map.get("userId");
        String friendId = map.get("friendId");
        if (friendId == null || friendId.isEmpty()) {
            return Result.fail(416, "Friend id is empty", null);
        }
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        UserProfile friendProfile = userProfileService.getUserProfileByUserId(Integer.valueOf(friendId));
        if (friendProfile == null) {
            return Result.fail(412, "User not found", null);
        }
        friendProfile.nullToEmpty();
        File avatarFile = userProfileService.getAvatarFileByUserId(Integer.valueOf(friendId));
        String avatarBase64;
        if (avatarFile == null || !avatarFile.exists()) {
            avatarBase64 = "";
        } else {
            FileInputStream fileInputStream = new FileInputStream(avatarFile);
            final byte[] avatarBytes = IOUtils.toByteArray(fileInputStream);
            fileInputStream.close();
            avatarBase64 = Base64.getEncoder().encodeToString(avatarBytes);
        }
        Map<String, String> res = Map.of(
                "userId", String.valueOf(friendProfile.getUserId()),
                "nickname", friendProfile.getNickname(),
                "gender", friendProfile.getGender(),
                "region", friendProfile.getRegion(),
                "avatar", avatarBase64
        );
        return Result.ok(res, "success");
    }

    @Operation(summary = "搜索群聊")
    @PostMapping("/searchGroup")
    public Result searchGroup (
            @Parameter(description = "用户id,群聊id", name = "userId, groupId", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) {
        String userId = map.get("userId");
        String groupId = map.get("groupId");
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        GroupInfo groupInfo = groupService.getGroupInfoByGroupId(Integer.valueOf(groupId));
        if (groupInfo == null) {
            return Result.fail(502, "Group info not found", null);
        }
        Group group = groupService.getGroupByGroupId(Integer.valueOf(groupId));
        String avatarBase64 = "";
        File file = groupService.getGroupAvatar(Integer.valueOf(groupId));
        if (file != null) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                final byte[] avatarBytes = IOUtils.toByteArray(fileInputStream);
                fileInputStream.close();
                avatarBase64 = Base64.getEncoder().encodeToString(avatarBytes);
            } catch (Exception e) {
//                return Result.fail(503, "Failed to get group avatar", null);
            }
        }
        Map<String, String> res = Map.of(
                "groupId", String.valueOf(groupInfo.getGroupId()),
                "groupName", groupInfo.getGroupName(),
                "announcement", groupInfo.getAnnouncement(),
                "avatar", avatarBase64,
                "creatorId", String.valueOf(group.getCreatorId()),
                "createTime", String.valueOf(group.getCreatedAt().getTime())

        );
        return Result.ok(res, "success");
    }

    @Operation(summary = "发起好友申请")
    @PostMapping("/sendFriendRequest")
    public Result sendFriendRequest(
            @Parameter(description = "用户id,好友id,申请信息", name = "userId, friendId, message", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) {
        String userId = map.get("userId");
        String friendId = map.get("friendId");
        String message = map.get("message");
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        if (contactService.addApplyToFriend(Integer.valueOf(userId), Integer.valueOf(friendId), message)) {
            return Result.ok(null, "success");
        } else {
            return Result.fail(601, "Failed to send friend request", null);
        }
    }

    @Operation(summary = "申请加入群聊")
    @PostMapping("/sendGroupRequest")
    public Result sendGroupRequest(
            @Parameter(description = "用户id,群聊id,申请信息", name = "userId, groupId, message", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) {
        String userId = map.get("userId");
        String groupId = map.get("groupId");
        String message = map.get("message");
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            return Result.fail(502, "Group info not found", null);
        }
        if (contactService.addApplyToGroup(Integer.valueOf(userId), Integer.valueOf(groupId), group.getCreatorId(), message)) {
            return Result.ok(null, "success");
        } else {
            return Result.fail(601, "Failed to send group request", null);
        }
    }

    @Operation(summary = "处理好友申请")
    @PostMapping("/processFriendRequest")
    public Result processFriendRequest(
            @Parameter(description = "用户id,好友id,状态", name = "userId, friendId, status", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) {
        String userId = map.get("userId");
        String friendId = map.get("friendId");
        String status = map.get("status");
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        if (contactService.processFriendApply(Integer.valueOf(userId), Integer.valueOf(friendId), Integer.valueOf(status))) {
            return Result.ok(null, "success");
        } else {
            return Result.fail(602, "Failed to process friend request", null);
        }
    }

    @Operation(summary = "处理群聊申请")
    @PostMapping("/processGroupRequest")
    public Result processGroupRequest(
            @Parameter(description = "用户id,发起人id,群聊id,状态", name = "userId, otherId, groupId, status", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) {
        String userId = map.get("userId");
        String otherId = map.get("otherId");
        String groupId = map.get("groupId");
        String status = map.get("status");
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        if (contactService.processGroupApply(Integer.valueOf(otherId), Integer.valueOf(groupId), Integer.valueOf(userId), Integer.parseInt(status))) {
            return Result.ok(null, "success");
        } else {
            return Result.fail(602, "Failed to process group request", null);
        }
    }

    @Operation(summary = "获取好友申请(入群申请)")
    @PostMapping("/getContactApplyFromOthers")
    public Result getContactApplyFromOthers(
            @Parameter(description = "用户id", name = "userId", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) {
        String userId = map.get("userId");
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        List<ContactApply> contactApplies = contactService.getContactApplyFromOthers(Integer.valueOf(userId));
        List<Map<String, String>> res = new ArrayList<>(List.of());
        for (ContactApply contactApply : contactApplies) {
            contactApply.nullToEmpty();
            res.add(Map.of(
                    "applicantId", String.valueOf(contactApply.getApplicantId()),
                    "receiverId", String.valueOf(contactApply.getReceiverId()),
                    "groupId", String.valueOf(contactApply.getGroupId()),
                    "contactType", contactApply.getContactType(),
                    "message", contactApply.getMessage(),
                    "status", contactApply.getStatus(),
                    "applyTime", String.valueOf(contactApply.getApplyTime().getTime())
            ));
        }
        return Result.ok(res, "success");
    }

    @Operation(summary = "获取自己发出的好友申请(入群申请)")
    @PostMapping("/getContactApplyFromMe")
    public Result getContactApplyFromMe(
            @Parameter(description = "用户id", name = "userId", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) {
        String userId = map.get("userId");
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        List<ContactApply> contactApplies = contactService.getContactApplyFromMe(Integer.valueOf(userId));
        List<Map<String, String>> res = new ArrayList<>(List.of());
        for (ContactApply contactApply : contactApplies) {
            contactApply.nullToEmpty();
            res.add(Map.of(
                    "applicantId", String.valueOf(contactApply.getApplicantId()),
                    "receiverId", String.valueOf(contactApply.getReceiverId()),
                    "groupId", String.valueOf(contactApply.getGroupId()),
                    "contactType", contactApply.getContactType(),
                    "message", contactApply.getMessage(),
                    "status", contactApply.getStatus(),
                    "applyTime", String.valueOf(contactApply.getApplyTime().getTime())
            ));
        }
        return Result.ok(res, "success");
    }

    @Operation(summary = "添加好友入群")
    @PostMapping("/addFriendToGroup")
    public Result addFriendToGroup(
            @Parameter(description = "用户id,好友id,群聊id", name = "userId, friendId, groupId", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) {
        String userId = map.get("userId");
        String friendId = map.get("friendId");
        String groupId = map.get("groupId");
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        Group group = groupService.getGroupByGroupId(Integer.valueOf(groupId));
        if (group == null) {
            return Result.fail(502, "Group info not found", null);
        }
        if (contactService.addFriendToGroup(Integer.valueOf(friendId), Integer.valueOf(groupId))) {
            return Result.ok(null, "success");
        } else {
            return Result.fail(603, "Failed to add friend to group(may be friend in group)", null);
        }
    }

    @Operation(summary = "踢出群成员")
    @PostMapping("/deleteFriendFromGroup")
    public Result deleteFriendFromGroup(
            @Parameter(description = "用户id,好友id,群聊id", name = "userId, friendId, groupId", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) {
        String userId = map.get("userId");
        String friendId = map.get("friendId");
        String groupId = map.get("groupId");
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            return Result.fail(502, "Group info not found", null);
        }
        if (!userId.equals(String.valueOf(group.getCreatorId()))) {
            return Result.fail(505, "Permission denied", null);
        }
        try {
            if (contactService.deleteFriendFromGroup(Integer.valueOf(friendId), Integer.valueOf(groupId))) {
                return Result.ok(null, "success");
            } else {
                return Result.fail(604, "Failed to delete friend from group", null);
            }
        } catch (Exception e) {
            return Result.fail(605, e.getMessage(), null);
        }

    }

    @Operation(summary = "获取好友列表")
    @PostMapping("/getFriends")
    public Result getFriends(
            @Parameter(description = "用户id", name = "userId", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) {
        String userId = map.get("userId");
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        List<Map<String, String>> res = new ArrayList<>(List.of());
        List<Friend> friends = contactService.getFriends(Integer.valueOf(userId));
        for (Friend friend : friends) {
            res.add(Map.of(
                    "friendId", String.valueOf(friend.getFriendId()),
                    "status", String.valueOf(friend.getStatus())
            ));
        }
        return Result.ok(res, "success");
    }

    @Operation(summary = "获取群成员")
    @PostMapping("/getGroupMembers")
    public Result getGroupMembers(
            @Parameter(description = "用户id,群聊id", name = "userId, groupId", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) {
        String userId = map.get("userId");
        String groupId = map.get("groupId");
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        List<Integer> members = contactService.getGroupMembers(Integer.valueOf(groupId));
        List<Map<String, String>> res = new ArrayList<>(List.of());
        for (Integer member : members) {
            res.add(Map.of(
                    "userId", String.valueOf(member)
            ));
        }
        return Result.ok(res, "success");
    }

    @Operation(summary = "获取群主")
    @PostMapping("/getGroupOwner")
    public Result getGroupOwner(
            @Parameter(description = "用户id,群聊id", name = "userId, groupId", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) {
        String userId = map.get("userId");
        String groupId = map.get("groupId");
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            return Result.fail(502, "Group info not found", null);
        }
        Map<String, String> res = Map.of(
                "userId", String.valueOf(group.getCreatorId())
        );
        return Result.ok(res, "success");
    }

    @Operation(summary = "获取群列表")
    @PostMapping("/getGroups")
    public Result getGroups(
            @Parameter(description = "用户id", name = "userId", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) {
        String userId = map.get("userId");
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        List<Group> groups = contactService.getGroups(Integer.valueOf(userId));
        List<Map<String, String>> res = new ArrayList<>(List.of());
        for (Group group : groups) {
            res.add(Map.of(
                    "groupId", String.valueOf(group.getGroupId())
            ));
        }
        return Result.ok(res, "success");
    }

    // status: 0 拉黑 1 正常
    @Operation(summary = "更新好友状态")
    @PostMapping("/updateFriendStatus")
    public Result updateFriendStatus(
            @Parameter(description = "用户id,好友id,状态", name = "userId, friendId, status", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) {
        String userId = map.get("userId");
        String friendId = map.get("friendId");
        String status = map.get("status");
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        if (contactService.updateFriendStatus(Integer.valueOf(friendId), Integer.valueOf(userId), Integer.valueOf(status))) {
            return Result.ok(null, "success");
        } else {
            return Result.fail(605, "Failed to update friend status", null);
        }
    }

    @Operation(summary = "删除好友")
    @PostMapping("/deleteFriend")
    public Result deleteFriend(
            @Parameter(description = "用户id,好友id", name = "userId, friendId", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) {
        String userId = map.get("userId");
        String friendId = map.get("friendId");
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        if (contactService.deleteFriend(Integer.valueOf(userId), Integer.valueOf(friendId))) {
            return Result.ok(null, "success");
        } else {
            return Result.fail(606, "Failed to delete friend", null);
        }
    }
}
