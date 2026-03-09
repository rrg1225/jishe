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
        if (currentPhone == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("登录状态已失效，请重新登录");
        }

        // 去数据库查一下，这人是不是已经存过简历了？
        Optional<Resume> existingResumeOpt = resumeRepository.findByUserPhone(currentPhone);

        Resume resumeToSave;

        if (existingResumeOpt.isPresent()) {
            // 【老用户修改】：不能直接覆盖，需要把前端传来的新内容，一个个赋给数据库里的老对象
            resumeToSave = existingResumeOpt.get();
            resumeToSave.setTemplateName(requestResume.getTemplateName());
            resumeToSave.setFullName(requestResume.getFullName());
            resumeToSave.setContactPhone(requestResume.getContactPhone());
            resumeToSave.setEmail(requestResume.getEmail());
            resumeToSave.setTargetCity(requestResume.getTargetCity());
            resumeToSave.setEducationInfo(requestResume.getEducationInfo());
            resumeToSave.setProjectExperience(requestResume.getProjectExperience());
            resumeToSave.setProfessionalSkills(requestResume.getProfessionalSkills());
        } else {
            // 【新用户第一次保存】：直接使用前端传来的对象，并死死绑定他的手机号！
            resumeToSave = requestResume;
            resumeToSave.setUserPhone(currentPhone);
        }

        // 统一执行保存（JPA 会自动识别是 INSERT 还是 UPDATE）
        resumeRepository.save(resumeToSave);
        return ResponseEntity.ok("简历保存成功！");
    }
}