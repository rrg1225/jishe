package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.model.FriendRequest;
import com.example.student.university_career_platform.model.UserFriend;
import com.example.student.university_career_platform.repository.FriendRequestRepository;
import com.example.student.university_career_platform.repository.UserFriendRepository;
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

    // 1. 获取我的好友列表 (保持不变)
    @GetMapping("/list")
    public Map<String, Object> getFriendList(@RequestParam String userId) {
        Map<String, Object> result = new HashMap<>();
        List<UserFriend> records = userFriendRepository.findByUserId(userId);
        List<Map<String, Object>> friendList = new ArrayList<>();
        for (UserFriend record : records) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getFriendId());
            map.put("name", "用户_" + record.getFriendId());
            map.put("isOnline", true);
            friendList.add(map);
        }
        result.put("code", 200);
        result.put("data", friendList);
        return result;
    }

    // 🌟 2. 发送好友请求 (替代原来的直接添加)
    @PostMapping("/request")
    public Map<String, Object> sendRequest(@RequestParam String senderId, @RequestParam String receiverId) {
        Map<String, Object> result = new HashMap<>();
        try {
            FriendRequest request = new FriendRequest();
            request.setSenderId(senderId);
            request.setReceiverId(receiverId);
            request.setStatus("PENDING");
            friendRequestRepository.save(request);

            result.put("code", 200);
            result.put("msg", "好友请求已发送，等待对方通过");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "请勿重复发送请求！");
        }
        return result;
    }

    // 🌟 3. 获取别人发给我的“待处理”请求
    @GetMapping("/pending-requests")
    public Map<String, Object> getPendingRequests(@RequestParam String userId) {
        Map<String, Object> result = new HashMap<>();
        // 查出我是 receiver，且状态是 PENDING 的所有记录
        List<FriendRequest> requests = friendRequestRepository.findByReceiverIdAndStatus(userId, "PENDING");
        result.put("code", 200);
        result.put("data", requests);
        return result;
    }

    // 🌟 4. 同意好友请求
    @PostMapping("/accept")
    public Map<String, Object> acceptRequest(@RequestParam Long requestId) {
        Map<String, Object> result = new HashMap<>();
        // 找到这条请求
        FriendRequest request = friendRequestRepository.findById(requestId).orElse(null);
        if (request != null && "PENDING".equals(request.getStatus())) {
            // 将状态改为已同意
            request.setStatus("ACCEPTED");
            friendRequestRepository.save(request);

            // 正式写入好友表 (双向添加)
            UserFriend f1 = new UserFriend();
            f1.setUserId(request.getSenderId());
            f1.setFriendId(request.getReceiverId());
            userFriendRepository.save(f1);

            UserFriend f2 = new UserFriend();
            f2.setUserId(request.getReceiverId());
            f2.setFriendId(request.getSenderId());
            userFriendRepository.save(f2);

            result.put("code", 200);
            result.put("msg", "已通过好友验证！");
        } else {
            result.put("code", 400);
            result.put("msg", "请求不存在或已处理");
        }
        return result;
    }
}