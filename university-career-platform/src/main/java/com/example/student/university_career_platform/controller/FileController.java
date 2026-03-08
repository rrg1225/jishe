package com.example.student.university_career_platform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
@CrossOrigin
public class FileController {

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file")MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("文件不能为空");
        }

        try {
            // 1.获取文件的原始后缀名（比如 .png , .mp4）
            String originalFilename = file.getOriginalFilename();
            String extension = null;
            if (originalFilename != null) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 2.为了防止文件名重复覆盖，生成一个全球唯一的 UUID 做为新文件名

            String newFileName = UUID.randomUUID() + extension;

            // 3, 确定保存的路径（项目根目录下的 uploads 文件夹）
            String projectPath = System.getProperty("user.dir");
            File uploadDir = new File(projectPath + File.separator + "uploads");

            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 4.把文件保存在硬盘上
            File dest = new File(uploadDir.getAbsolutePath() + File.separator + newFileName);
            file.transferTo(dest);

            // 5.拼接处可以访问该文件的 URL 并返回给前端
            // 注意，如果云服务器上，这里的 localhost 应该换成你的公网 IP
            String fileUrl = "http://114.55.2.140:8080/uploads/" + newFileName;

            return ResponseEntity.ok(fileUrl);

            }catch (Exception e) {
            return ResponseEntity.internalServerError().body("文件上传失败" + e.getMessage());

        }
        }
}

