package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.model.FriendRequest;
import com.example.student.university_career_platform.model.UserFriend;
import com.example.student.university_career_platform.repository.ChatMessageRepository;
import com.example.student.university_career_platform.repository.FriendRequestRepository;
import com.example.student.university_career_platform.repository.UserFriendRepository;
import com.example.student.university_career_platform.service.WebSocketServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friend")
public class FriendController {

    @Autowired
    private UserFriendRepository userFriendRepository;
    @Autowired
    private FriendRequestRepository friendRequestRepository;
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 1. 获取好友列表
     * 结合 Redis 查询实时的在线状态
     */
    @GetMapping("/list")
    public Map<String, Object> getFriendList(@RequestParam String userId) {
        Map<String, Object> result = new HashMap<>();
        List<UserFriend> records = userFriendRepository.findByUserId(userId);
        List<Map<String, Object>> friendList = new ArrayList<>();

        for (UserFriend record : records) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getFriendId());
            // 优先展示备注名，没有则展示默认名
            map.put("name", record.getFriendAlias() != null ? record.getFriendAlias() : "用户_" + record.getFriendId());
            map.put("avatar", "https://api.dicebear.com/7.x/avataaars/svg?seed=" + record.getFriendId());
            map.put("lastMessage", "点击开始聊天");
            map.put("time", "");
            map.put("unread", 0);

            // 🌟 实时状态检测：去 Redis 匹配 IM_ONLINE 存活标签
            Boolean isOnline = redisTemplate.hasKey("IM_ONLINE:" + record.getFriendId());
            map.put("isOnline", isOnline != null && isOnline);

            friendList.add(map);
        }

        result.put("code", 200);
        result.put("data", friendList);
        return result;
    }

    /**
     * 2. 发送好友请求
     * 保存到数据库并实时推送信令给接收方
     */
    @PostMapping("/request")
    public Map<String, Object> sendRequest(@RequestParam String senderId,
                                           @RequestParam String receiverId,
                                           @RequestParam(required = false, defaultValue = "你好，我是...") String message) {
        FriendRequest request = new FriendRequest();
        request.setSenderId(senderId);
        request.setReceiverId(receiverId);
        request.setStatus("PENDING");
        request.setRequestMessage(message);
        friendRequestRepository.save(request);

        // 🚀 实时推送：告诉对方“有人想加你”
        sendSystemNotice(receiverId, "FRIEND_REQUEST", senderId, message);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "好友请求已发送");
        return result;
    }

    /**
     * 3. 待处理请求列表
     */
    @GetMapping("/pending-requests")
    public Map<String, Object> getPendingRequests(@RequestParam String userId) {
        Map<String, Object> result = new HashMap<>();
        List<FriendRequest> requests = friendRequestRepository.findByReceiverIdAndStatus(userId, "PENDING");
        result.put("code", 200);
        result.put("data", requests);
        return result;
    }

    /**
     * 4. 处理好友申请
     * 同意时建立双向好友关系，并发送反馈通知
     */
    @PostMapping("/handle")
    @Transactional
    public Map<String, Object> handleRequest(@RequestParam Long requestId,
                                             @RequestParam String action,
                                             @RequestParam(required = false) String alias) {
        Map<String, Object> result = new HashMap<>();
        FriendRequest request = friendRequestRepository.findById(requestId).orElse(null);

        if (request == null || !"PENDING".equals(request.getStatus())) {
            result.put("code", 400);
            result.put("msg", "请求不存在或已处理");
            return result;
        }

        String finalAction = action.toUpperCase();
        request.setStatus(finalAction);
        friendRequestRepository.save(request);

        if ("ACCEPT".equals(finalAction)) {
            // 建立双向关系：A -> B
            UserFriend f1 = new UserFriend();
            f1.setUserId(request.getReceiverId());
            f1.setFriendId(request.getSenderId());
            f1.setFriendAlias(alias);
            userFriendRepository.save(f1);

            // 建立双向关系：B -> A
            UserFriend f2 = new UserFriend();
            f2.setUserId(request.getSenderId());
            f2.setFriendId(request.getReceiverId());
            userFriendRepository.save(f2);

            // 🚀 反向通知：告诉申请人“对方已同意”
            sendSystemNotice(request.getSenderId(), "FRIEND_ACCEPT", request.getReceiverId(), "已通过您的好友验证");
            result.put("msg", "已通过好友验证");
        } else if ("REJECT".equals(finalAction)) {
            sendSystemNotice(request.getSenderId(), "FRIEND_REJECT", request.getReceiverId(), "对方拒绝了您的请求");
            result.put("msg", "已拒绝请求");
        }

        result.put("code", 200);
        return result;
    }

    /**
     * 5. 修改备注
     */
    @PostMapping("/alias")
    public Map<String, Object> setAlias(@RequestParam String userId, @RequestParam String friendId, @RequestParam String alias) {
        Map<String, Object> result = new HashMap<>();
        UserFriend record = userFriendRepository.findByUserIdAndFriendId(userId, friendId);
        if (record != null) {
            record.setFriendAlias(alias);
            userFriendRepository.save(record);
            result.put("code", 200);
            result.put("msg", "备注修改成功");
        } else {
            result.put("code", 400);
            result.put("msg", "记录不存在");
        }
        return result;
    }

    /**
     * 6. 删除好友
     * 物理删除关系并清理聊天记录
     */
    @PostMapping("/delete")
    @Transactional
    public Map<String, Object> deleteFriend(@RequestParam String userId, @RequestParam String friendId) {
        userFriendRepository.deleteByUserIdAndFriendId(userId, friendId);
        userFriendRepository.deleteByUserIdAndFriendId(friendId, userId);
        chatMessageRepository.deleteChatHistoryBetween(userId, friendId);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "删除成功");
        return result;
    }

    /**
     * --- 封装 WebSocket 系统通知 ---
     * 🌟 核心：通过静态方法调用 WebSocketServer 接口
     */
    private void sendSystemNotice(String toUserId, String subType, String fromUserId, String content) {
        try {
            ObjectNode notice = objectMapper.createObjectNode();
            notice.put("type", "SYSTEM_NOTICE");
            notice.put("subType", subType);
            notice.put("fromUserId", fromUserId);
            notice.put("content", content);

            // 这里的调用要求 WebSocketServer 中的 sendMessageToUser 方法必须为 public static
            WebSocketServer.sendMessageToUser(toUserId, notice.toString());
        } catch (Exception e) {
            System.err.println("发送系统信令失败: " + e.getMessage());
        }
    }
}