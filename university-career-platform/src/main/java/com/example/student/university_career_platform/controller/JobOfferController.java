package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.common.Result;
import com.example.student.university_career_platform.model.JobOffer;
import com.example.student.university_career_platform.service.JobOfferService;
import com.example.student.university_career_platform.utils.CosineSimilarityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobOfferController {

    @Autowired
    private JobOfferService jobOfferService;

    /**
     * 获取产教融合双选岗位列表 (已接入全局 AOP 异常处理，无需 try-catch)
     */
    @GetMapping("/list")
    public Result<List<JobOffer>> getJobList() {
        List<JobOffer> jobs = jobOfferService.getLatestJobOffers();
        return Result.success(jobs);
    }

    /**
     * 🌟 基于余弦相似度的 AI 智能岗位推荐
     */
    @PostMapping("/recommend")
    public Result<List<JobOffer>> getRecommendedJobs(@RequestBody Map<String, String> requestData) {

        String userSkills = requestData.getOrDefault("userSkills", "");
        List<JobOffer> allJobs = jobOfferService.getLatestJobOffers();

        if (userSkills.trim().isEmpty()) {
            return Result.success(allJobs.subList(0, Math.min(10, allJobs.size())));
        }

        List<Map<String, Object>> scoredJobs = new ArrayList<>();
        for (JobOffer job : allJobs) {
            String jobText = (job.getJobName() == null ? "" : job.getJobName()) + " " +
                    (job.getRequirements() == null ? "" : job.getRequirements()) + " " +
                    (job.getJobDescription() == null ? "" : job.getJobDescription()) + " " +
                    (job.getTechStack() == null ? "" : job.getTechStack());

            double score = CosineSimilarityUtil.calculateSimilarity(userSkills, jobText);

            Map<String, Object> jobMap = new HashMap<>();
            jobMap.put("job", job);
            jobMap.put("matchScore", score);
            scoredJobs.add(jobMap);
        }

        // 按匹配度得分降序排序
        scoredJobs.sort((a, b) -> Double.compare((Double) b.get("matchScore"), (Double) a.get("matchScore")));

        List<JobOffer> recommendedJobs = new ArrayList<>();
        for (int i = 0; i < Math.min(10, scoredJobs.size()); i++) {
            JobOffer bestJob = (JobOffer) scoredJobs.get(i).get("job");
            recommendedJobs.add(bestJob);
        }

        return Result.success(recommendedJobs);
    }
}