package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.common.Result;
import com.example.student.university_career_platform.model.ResumeTemplate;
import com.example.student.university_career_platform.repository.ResumeTemplateRepository;
import com.example.student.university_career_platform.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/templates")
public class ResumeTemplateController {

    @Autowired
    private ResumeTemplateRepository repository;

    @Autowired
    private OssService ossService;

    @GetMapping("/list")
    public Result<List<ResumeTemplate>> getTemplates(@RequestParam Integer type) {
        List<ResumeTemplate> list = repository.findByType(type);
        return Result.success(list);
    }

    // 🌟 已净化：直接抛出异常，剥离业务外的代码
    @PostMapping("/upload")
    public Result<String> uploadTemplate(
            @RequestParam("name") String name,
            @RequestParam("type") Integer type,
            @RequestParam("file") MultipartFile file,
            @RequestParam("coverImg") MultipartFile coverImg
    ) throws Exception {

        String fileObjectName = "resumes/files/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        String imgObjectName = "resumes/covers/" + UUID.randomUUID() + "_" + coverImg.getOriginalFilename();

        String fileUrl = ossService.uploadVideo(fileObjectName, file.getInputStream());
        String previewUrl = ossService.uploadVideo(imgObjectName, coverImg.getInputStream());

        ResumeTemplate template = new ResumeTemplate();
        template.setName(name);
        template.setType(type);
        template.setDocUrl(fileUrl);
        template.setPreviewUrl(previewUrl);

        repository.save(template);
        return Result.success("简历及封面上传成功！");
    }

    @GetMapping("/{id}")
    public Result<ResumeTemplate> getDetail(@PathVariable Long id) {
        return repository.findById(id).map(Result::success).orElse(Result.error(400, "未找到该模板"));
    }
}