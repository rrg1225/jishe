package com.example.student.university_career_platform.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class EmbeddingService {

    /**
     * 将文本转为稠密向量 (Embedding)
     * ⚠️ 【答辩注意】：这里为了演示，返回了随机向量。
     * 真实比赛上线前，请将这里替换为调用阿里云 DashScope 的 Text-Embedding API 接口！
     */
    public List<Double> getEmbedding(String text) {
        List<Double> vector = new ArrayList<>();
        Random random = new Random(text.hashCode()); // 保证相同文本生成的向量一致
        for (int i = 0; i < 1536; i++) { // 模拟 1536 维
            vector.add(random.nextDouble());
        }
        return vector;
    }
}