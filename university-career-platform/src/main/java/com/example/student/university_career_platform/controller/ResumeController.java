package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.model.Resume;
import com.example.student.university_career_platform.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin
public class ResumeController {

    @Autowired
    private ResumeRepository resumeRepository;

    // 🌟 终极防御：安全获取当前登录用户的手机号
    private String getCurrentUserPhone() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return authentication.getName();
    }

    /**
     * 接口 1：获取我的简历（用于打开页面时，A4 纸的数据回显）
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyResume() {
        String currentPhone = getCurrentUserPhone();
        if (currentPhone == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("请先登录");
        }

        // 去数据库找这个手机号对应的简历
        Optional<Resume> resumeOpt = resumeRepository.findByUserPhone(currentPhone);

        if (resumeOpt.isPresent()) {
            return ResponseEntity.ok(resumeOpt.get()); // 找到了，原样返回给前端
        } else {
            // 如果没找到（新用户），返回一个空的对象，前端收到后显示空白模板即可
            return ResponseEntity.ok().body("{}");
        }
    }

    /**
     * 接口 2：保存或更新简历（前端点击“保存”时调用）
     */
    @PostMapping("/save")
    public ResponseEntity<?> saveResume(@RequestBody Resume requestResume) {
        String currentPhone = getCurrentUserPhone();
        if (currentPhone == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("请登录");

        Optional<Resume> existingResumeOpt = resumeRepository.findByUserPhone(currentPhone);
        Resume resumeToSave = existingResumeOpt.orElse(new Resume());

        // 覆盖所有字段
        resumeToSave.setUserPhone(currentPhone);
        resumeToSave.setTemplateName(requestResume.getTemplateName());
        resumeToSave.setFullName(requestResume.getFullName());
        resumeToSave.setContactPhone(requestResume.getContactPhone());
        resumeToSave.setEmail(requestResume.getEmail());
        resumeToSave.setTargetCity(requestResume.getTargetCity());
        resumeToSave.setEduStatus(requestResume.getEduStatus()); // 🌟 新增
        resumeToSave.setAvatarUrl(requestResume.getAvatarUrl()); // 🌟 新增
        resumeToSave.setEducationInfo(requestResume.getEducationInfo());
        resumeToSave.setProjectExperience(requestResume.getProjectExperience());
        resumeToSave.setProfessionalSkills(requestResume.getProfessionalSkills());

        resumeRepository.save(resumeToSave);
        return ResponseEntity.ok("简历已永久保存到云端！");
    }

}