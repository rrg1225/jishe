package com.example.student.university_career_platform.service;

import com.aliyun.oss.OSS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class OssService {

    // 直接注入刚才在 OssConfig 中初始化的 OSS 客户端
    @Autowired
    private OSS ossClient;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    /**
     * 上传视频到 OSS 并返回播放链接
     *
     * @param objectName  OSS中的完整路径和文件名（例如："videos/tech/java_tutorial.mp4"）
     * @param inputStream 视频文件的输入流（通常从前端 Controller 接收 MultipartFile 获取）
     * @return 视频的公网播放URL
     */
    @SuppressWarnings("unused")
    public String uploadVideo(String objectName, InputStream inputStream) {
        try {
            // 1. 执行上传操作
            ossClient.putObject(bucketName, objectName, inputStream);

            // 2. 拼接公网可访问的播放链接
            // ⚠️ 重点：如果配置文件里填的是内网 endpoint，要把它替换成外网格式供用户播放
            String publicEndpoint = endpoint.replace("-internal", "");
            publicEndpoint = publicEndpoint.replace("http://", ""); // 去掉 http 前缀方便拼接 HTTPS

            return "https://" + bucketName + "." + publicEndpoint + "/" + objectName;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("OSS视频上传失败：" + e.getMessage());
        }
    }
    /**
     * 支持内存流直接上传 OSS (用于上传生成的缩略图)
     */
    public String uploadFileStream(java.io.InputStream inputStream, String originalFilename, String contentType) throws Exception {
        // 1. 生成唯一文件名，防止重名覆盖
        String fileName = java.util.UUID.randomUUID().toString().replace("-", "") + "_" + originalFilename;

        // 2. 这里的 ossClient, bucketName 等变量请确保您类中已有定义并注入
        // 设置元数据，防止图片被浏览器直接下载而是预览
        com.aliyun.oss.model.ObjectMetadata meta = new com.aliyun.oss.model.ObjectMetadata();
        meta.setContentType(contentType);

        // 执行上传
        ossClient.putObject(bucketName, fileName, inputStream, meta);

        // 3. 核心修改：填入您图片中的真实域名
        // 拼接后的格式为：https://student-career.oss-cn-beijing.aliyuncs.com/文件名
        return "https://student-career.oss-cn-beijing.aliyuncs.com/" + fileName;
    }
}
