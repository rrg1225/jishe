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

    // 1. 获取列表
    @GetMapping("/list")
    public Result<List<ResumeTemplate>> getTemplates(@RequestParam Integer type) {
        List<ResumeTemplate> list = repository.findByType(type);
        return Result.success(list);
    }

    // 2. 真实上传接口：严格匹配你的 OssService.uploadVideo(String, InputStream)
    @PostMapping("/upload")
    public Result<String> uploadTemplate(
            @RequestParam("name") String name,
            @RequestParam("type") Integer type,
            @RequestParam("file") MultipartFile file,        // Word原件
            @RequestParam("coverImg") MultipartFile coverImg // 截图封面
    ) {
        try {
            // 生成 OSS 上的唯一完整路径
            String fileObjectName = "resumes/files/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            String imgObjectName = "resumes/covers/" + UUID.randomUUID() + "_" + coverImg.getOriginalFilename();

            // 调用你的服务。注意：虽然方法名叫 uploadVideo，但它内部处理 InputStream 的逻辑对所有文件通用
            String fileUrl = ossService.uploadVideo(fileObjectName, file.getInputStream());
            String previewUrl = ossService.uploadVideo(imgObjectName, coverImg.getInputStream());

            // 存入数据库
            ResumeTemplate template = new ResumeTemplate();
            template.setName(name);
            template.setType(type);
            template.setDocUrl(fileUrl);
            template.setPreviewUrl(previewUrl);

            repository.save(template);

            return Result.success("简历及封面上传成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "上传失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<ResumeTemplate> getDetail(@PathVariable Long id) {
        return repository.findById(id).map(Result::success).orElse(Result.error(400, "未找到该模板"));
    }
}