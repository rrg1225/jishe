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

    // 🌟 已净化：使用 throws Exception 将异常外抛给全局处理器
    @PostMapping("/upload")
    public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
        Map<String, Object> result = new HashMap<>();
        if (file.isEmpty()) {
            result.put("code", 400);
            result.put("msg", "文件为空");
            return result;
        }

        InputStream inputStream = file.getInputStream();
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";

        String datePath = java.time.LocalDate.now().toString().replace("-", "");
        String objectName = "chat/" + datePath + "/" + UUID.randomUUID().toString().replace("-", "") + extension;

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ossClient.putObject(bucketName, objectName, inputStream);
        ossClient.shutdown();

        String url = "https://" + bucketName + "." + endpoint + "/" + objectName;

        Map<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("url", url);
        fileInfo.put("fileName", originalFilename);
        fileInfo.put("fileSize", file.getSize());

        result.put("code", 200);
        result.put("data", fileInfo);
        return result;
    }
}