package com.example.student.university_career_platform.service;

import com.example.student.university_career_platform.utils.CosineSimilarityUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class LocalVectorDb {

    // 直接存储文本段落作为本地知识库
    private final List<String> knowledgeBase = new ArrayList<>();

    // 系统启动时加载内部政策
    @PostConstruct
    public void initKnowledgeBase() {
        knowledgeBase.add("【关于三方协议】：2025届毕业生三方协议违约金上限不得超过5000元，必须在每年5月30日前完成学信网网签。");
        knowledgeBase.add("【关于档案去向】：如果毕业后未落实就业单位，档案将默认打回原籍（生源地）的人才交流中心，保留时间为2年。");
        knowledgeBase.add("【关于实习证明】：毕业实习需满3个月，实习证明必须加盖企业公章，并在第四学年下学期提交至教务处。");
    }

    /**
     * 核心检索逻辑：直接利用你的 CosineSimilarityUtil 对比文本
     */
    public List<String> searchSimilar(String userQuery, int topK) {
        List<Map.Entry<String, Double>> similarities = new ArrayList<>();

        for (String doc : knowledgeBase) {
            // 🚀 完美适配你项目中真实存在的方法，不会再报找不到方法的错误
            double similarity = CosineSimilarityUtil.calculateSimilarity(userQuery, doc);
            similarities.add(new AbstractMap.SimpleEntry<>(doc, similarity));
        }

        // 按相似度降序排序，并截取前 Top K 个文档返回
        return similarities.stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}