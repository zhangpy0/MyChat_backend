package top.zhangpy.mychat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.zhangpy.mychat.entity.po.Group;
import top.zhangpy.mychat.entity.po.GroupInfo;
import top.zhangpy.mychat.entity.vo.Result;
import top.zhangpy.mychat.service.GroupService;
import top.zhangpy.mychat.service.UserService;
import top.zhangpy.mychat.util.auth.JWTUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/group")
public class GroupController {

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @Operation(summary = "创建群聊")
    @PostMapping("/createGroup")
    public Result createGroup(
            @Parameter(description = "创建人id", name = "userId", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) {
        String userId = map.get("userId");
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        if (groupService.createGroup(Integer.valueOf(userId))) {
            return Result.ok(null, "success");
        } else {
            return Result.fail(501, "Failed to create group", null);
        }
    }

    @Operation(summary = "获取群聊信息")
    @PostMapping("/getGroupInfo")
    public Result getGroupInfo(
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
                if (file.exists()) {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    final byte[] avatarBytes = IOUtils.toByteArray(fileInputStream);
                    fileInputStream.close();
                    avatarBase64 = Base64.getEncoder().encodeToString(avatarBytes);
                } else {
                    avatarBase64 = "";
                }
            } catch (Exception e) {
                return Result.fail(503, "Failed to get group avatar", null);
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

    @Operation(summary = "修改群聊信息")
    @PostMapping("/updateGroupInfo")
    public Result updateGroupInfo(
            @Parameter(description = "用户id,群聊id,群聊名称,群公告", name = "userId, groupId, groupName, announcement", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) {
        String userId = map.get("userId");
        String groupId = map.get("groupId");
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        String groupName = map.get("groupName");
        String announcement = map.get("announcement");
        GroupInfo groupInfo = groupService.getGroupInfoByGroupId(Integer.valueOf(groupId));
        if (groupInfo == null) {
            return Result.fail(502, "Group info not found", null);
        }
        groupInfo.setGroupName(groupName);
        groupInfo.setAnnouncement(announcement);
        if (groupService.updateGroupInfo(groupInfo)) {
            return Result.ok(null, "success");
        } else {
            return Result.fail(504, "Failed to update group info", null);
        }
    }

    @Operation(summary = "修改群聊头像")
    @PostMapping(value = "/updateGroupAvatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result updateGroupAvatar(
            @Parameter(description = "用户id", name = "userId", required = true) @RequestParam("userId") String userId,
            @Parameter(description = "群聊id", name = "groupId", required = true) @RequestParam("groupId") String groupId,
            @Parameter(description = "file", name = "avatar", required = true) @RequestParam("avatar") MultipartFile file,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token) throws IOException {
        if (file == null) {
            return Result.fail(408, "file is null", null);
        }
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        Group group = groupService.getGroupByGroupId(Integer.valueOf(groupId));
        if (group == null) {
            return Result.fail(502, "Group not found", null);
        }
        if (!userId.equals(String.valueOf(group.getCreatorId()))) {
            return Result.fail(505, "Permission denied", null);
        }
        GroupInfo groupInfo = groupService.getGroupInfoByGroupId(Integer.valueOf(groupId));
        if (groupInfo == null) {
            return Result.fail(502, "Group info not found", null);
        }
        if (groupService.updateGroupAvatar(Integer.valueOf(groupId), file)) {
            return Result.ok(null, "success");
        } else {
            return Result.fail(506, "Failed to update group avatar", null);
        }
    }

    @Operation(summary = "解散群聊")
    @PostMapping("/deleteGroup")
    public Result deleteGroup(
            @Parameter(description = "用户id,群聊id", name = "userId, groupId", required = true) @RequestBody Map<String, String> map,
            @Parameter(description = "token", in = ParameterIn.HEADER, required = true) @RequestHeader("token") String token
    ) {
        String userId = map.get("userId");
        String groupId = map.get("groupId");
        Result result = JWTUtils.checkToken(userId, token);
        if (result != null) {
            return result;
        }
        Group group = groupService.getGroupByGroupId(Integer.valueOf(groupId));
        if (group == null) {
            return Result.fail(502, "Group not found", null);
        }
        if (!userId.equals(String.valueOf(group.getCreatorId()))) {
            return Result.fail(505, "Permission denied", null);
        }
        if (groupService.deleteGroup(Integer.valueOf(groupId))) {
            return Result.ok(null, "success");
        } else {
            return Result.fail(507, "Failed to delete group", null);
        }
    }
}
