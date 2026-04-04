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

    // 注入 Redis
    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 1. 获取好友列表（支持展示备注）
    @GetMapping("/list")
    public Map<String, Object> getFriendList(@RequestParam String userId) {
        Map<String, Object> result = new HashMap<>();
        List<UserFriend> records = userFriendRepository.findByUserId(userId);
        List<Map<String, Object>> friendList = new ArrayList<>();

        for (UserFriend record : records) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getFriendId());
            map.put("name", record.getFriendAlias() != null ? record.getFriendAlias() : "用户_" + record.getFriendId());
            map.put("avatar", "https://api.dicebear.com/7.x/avataaars/svg?seed=" + record.getFriendId());
            map.put("lastMessage", "点击开始聊天");
            map.put("time", "");
            map.put("unread", 0);

            // 🌟 核心：去 Redis 里查这个朋友有没有存活的心跳记录
            Boolean isOnline = redisTemplate.hasKey("IM_ONLINE:" + record.getFriendId());
            map.put("isOnline", isOnline);

            friendList.add(map);
        }

        result.put("code", 200);
        result.put("data", friendList);
        return result;
    }

    // 2. 发送好友请求（携带验证信息，并推送给对方）
    @PostMapping("/request")
    public Map<String, Object> sendRequest(@RequestParam String senderId,
                                           @RequestParam String receiverId,
                                           @RequestParam(required = false, defaultValue = "你好，我是...") String message) {
        FriendRequest request = new FriendRequest();
        request.setSenderId(senderId);
        request.setReceiverId(receiverId);
        request.setStatus("PENDING");
        request.setRequestMessage(message); // 保存验证信息
        friendRequestRepository.save(request);

        // 🚀 实时推送信令给目标用户
        sendSystemNotice(receiverId, "FRIEND_REQUEST", senderId, message);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "好友请求已发送，等待对方通过");
        return result;
    }

    // 3. 待处理列表
    @GetMapping("/pending-requests")
    public Map<String, Object> getPendingRequests(@RequestParam String userId) {
        Map<String, Object> result = new HashMap<>();
        List<FriendRequest> requests = friendRequestRepository.findByReceiverIdAndStatus(userId, "PENDING");
        result.put("code", 200);
        result.put("data", requests);
        return result;
    }

    // 4. 处理好友申请（包含同意、拒绝、忽略，并在同意时支持设置备注）
    @PostMapping("/handle")
    @Transactional
    public Map<String, Object> handleRequest(@RequestParam Long requestId,
                                             @RequestParam String action, // "ACCEPT", "REJECT", "IGNORE"
                                             @RequestParam(required = false) String alias) { // 仅在ACCEPT时可用
        Map<String, Object> result = new HashMap<>();
        FriendRequest request = friendRequestRepository.findById(requestId).orElse(null);

        if (request == null || !"PENDING".equals(request.getStatus())) {
            result.put("code", 400);
            result.put("msg", "请求不存在或已处理");
            return result;
        }

        request.setStatus(action.toUpperCase());
        friendRequestRepository.save(request);

        if ("ACCEPT".equalsIgnoreCase(action)) {
            // 我加对方
            UserFriend f1 = new UserFriend();
            f1.setUserId(request.getReceiverId()); // 我(接收者)
            f1.setFriendId(request.getSenderId()); // 对方(发送者)
            f1.setFriendAlias(alias); // 我给对方设置备注
            userFriendRepository.save(f1);

            // 对方加我
            UserFriend f2 = new UserFriend();
            f2.setUserId(request.getSenderId());
            f2.setFriendId(request.getReceiverId());
            userFriendRepository.save(f2);

            // 🚀 反向通知申请人：“对方已同意”
            sendSystemNotice(request.getSenderId(), "FRIEND_ACCEPT", request.getReceiverId(), "已通过您的好友验证，可以开始聊天了");
            result.put("msg", "已通过好友验证！");
        } else if ("REJECT".equalsIgnoreCase(action)) {
            sendSystemNotice(request.getSenderId(), "FRIEND_REJECT", request.getReceiverId(), "对方婉拒了您的好友请求");
            result.put("msg", "已拒绝该请求");
        } else {
            result.put("msg", "已忽略该请求");
        }

        result.put("code", 200);
        return result;
    }

    // 5. 修改好友备注 (随时可以修改)
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
            result.put("msg", "好友记录不存在");
        }
        return result;
    }

    // 6. 删除好友 (保持不变)
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

    // --- 封装WebSocket系统通知方法 ---
    private void sendSystemNotice(String toUserId, String subType, String fromUserId, String content) {
        try {
            ObjectNode notice = objectMapper.createObjectNode();
            notice.put("type", "SYSTEM_NOTICE");
            notice.put("subType", subType);
            notice.put("fromUserId", fromUserId);
            notice.put("content", content);

            // 🌟 核心修改：直接调用静态方法
            WebSocketServer.sendMessageToUser(toUserId, notice.toString());
        } catch (Exception e) {
            System.err.println("发送系统信令失败: " + e.getMessage());
        }
    }


}