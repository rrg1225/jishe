package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.model.FriendRequest;
import com.example.student.university_career_platform.model.UserFriend;
import com.example.student.university_career_platform.repository.ChatMessageRepository;
import com.example.student.university_career_platform.repository.FriendRequestRepository;
import com.example.student.university_career_platform.repository.UserFriendRepository;
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

    // 🌟 已净化：去掉 try-catch
    @PostMapping("/request")
    public Map<String, Object> sendRequest(@RequestParam String senderId, @RequestParam String receiverId) {
        FriendRequest request = new FriendRequest();
        request.setSenderId(senderId);
        request.setReceiverId(receiverId);
        request.setStatus("PENDING");
        friendRequestRepository.save(request);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "好友请求已发送，等待对方通过");
        return result;
    }

    @GetMapping("/pending-requests")
    public Map<String, Object> getPendingRequests(@RequestParam String userId) {
        Map<String, Object> result = new HashMap<>();
        List<FriendRequest> requests = friendRequestRepository.findByReceiverIdAndStatus(userId, "PENDING");
        result.put("code", 200);
        result.put("data", requests);
        return result;
    }

    @PostMapping("/accept")
    public Map<String, Object> acceptRequest(@RequestParam Long requestId) {
        Map<String, Object> result = new HashMap<>();
        FriendRequest request = friendRequestRepository.findById(requestId).orElse(null);
        if (request != null && "PENDING".equals(request.getStatus())) {
            request.setStatus("ACCEPTED");
            friendRequestRepository.save(request);

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

    // 🌟 已净化：去掉 try-catch
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
}