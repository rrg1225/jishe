package com.example.student.university_career_platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 🌟 核心修复：所有的逻辑操作，必须写在这个加了 @Override 的方法体里面！
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取当前项目的根目录
        String projectPath = System.getProperty("user.dir");
        String uploadPath = projectPath + File.separator + "uploads" + File.separator;

        // 这句代码的意思是：如果前端请求 http://localhost:8080/uploads/xxx.mp4
        // 就自动去项目的 uploads 文件夹下找这个文件！
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }

}