package com.example.student.university_career_platform.controller;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.student.university_career_platform.service.OssService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/file")
@CrossOrigin
public class FileController {

    @Autowired
    private OssService ossService;

    @PostMapping("/upload")
    public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 先上传原图 (比如 10MB 的高清大图)
            String originalUrl = ossService.uploadFile(file);
            String thumbnailUrl = originalUrl; // 默认缩略图等于原图（针对非图片文件如 PDF/Word）

            // 2. 🌟 核心优化：如果是图片，在内存中瞬间压缩出一张 50KB 的缩略图并上传
            String contentType = file.getContentType();
            if (contentType != null && contentType.startsWith("image/")) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                // 宽高缩小一半，质量压缩到 30%，极限削峰！
                Thumbnails.of(file.getInputStream())
                        .scale(0.5f)
                        .outputQuality(0.3f)
                        .toOutputStream(os);

                // 将压缩后的内存流上传到 OSS（文件名加个 thumb_ 前缀）
                ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
                String originalFileName = file.getOriginalFilename();
                String thumbFileName = "thumb_" + originalFileName;
                thumbnailUrl = ossService.uploadFileStream(is, thumbFileName, contentType);
            }

            // 返回冷热两张图的 URL
            Map<String, Object> data = new HashMap<>();
            data.put("url", originalUrl);            // 10MB 高清原图 (点击放大看)
            data.put("thumbnailUrl", thumbnailUrl);  // 50KB 极速缩略图 (聊天列表渲染)
            data.put("fileName", file.getOriginalFilename());
            data.put("fileSize", file.getSize());

            result.put("code", 200);
            result.put("data", data);
            result.put("msg", "上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "上传失败: " + e.getMessage());
        }
        return result;
    }
}