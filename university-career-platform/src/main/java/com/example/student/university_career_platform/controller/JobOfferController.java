package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.common.Result;
import com.example.student.university_career_platform.model.JobOffer;
import com.example.student.university_career_platform.service.JobOfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobOfferController {

    @Autowired // 👈 同样必须用 Autowired 注入，绝对不能写 = new JobOfferService()
    private JobOfferService jobOfferService;

    /**
     * 获取产教融合双选岗位列表
     * 接口地址: GET /api/jobs/list
     */

    @GetMapping("/list")
    public Result<List<JobOffer>> getJobList() {
        try {
            List<JobOffer> jobs = jobOfferService.getLatestJobOffers();
            return Result.success(jobs);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500,"获取岗位列表失败：" + e.getMessage());
        }
    }
}