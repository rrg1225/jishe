package com.example.student.university_career_platform.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
@CrossOrigin
public class FileController {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @PostMapping("/upload")
    public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        if (file.isEmpty()) {
            result.put("code", 400);
            result.put("msg", "文件为空");
            return result;
        }
        try {
            // 1.获取上传文件流
            InputStream inputStream = file.getInputStream();
            String originalFilename = file.getOriginalFilename();
            String extension = null;
            if (originalFilename != null) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 2.构建在 OSS 中的企业及文件路径（按日期归档。防止单个文件过大）
            String datePath = java.time.LocalDate.now().toString().replace("-", "");
            String objectName = "chat/" + datePath + "/" + UUID.randomUUID().toString().replace("-", "") + extension;


            // 3. 上传到阿里云 OSS
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            ossClient.putObject(bucketName, objectName, inputStream);
            ossClient.shutdown();

            // 4. 拼接并返回 OSS 的全球加速访问 URL
            String url = "https://" + bucketName + "." + endpoint + "/" + objectName;

            // 🌟 结构化返回数据，方便前端组装消息
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("url", url);
            fileInfo.put("fileName", originalFilename);
            fileInfo.put("fileSize", file.getSize());

            result.put("code", 200);
            result.put("data", fileInfo);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "OSS上传失败");
        }
        return result;
    }
}

