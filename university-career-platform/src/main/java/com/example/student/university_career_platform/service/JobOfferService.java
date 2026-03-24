package com.example.student.university_career_platform.service;

import com.example.student.university_career_platform.model.JobOffer;
import com.example.student.university_career_platform.repository.JobOfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobOfferService {

    @Autowired // 自动注入持久层
    private JobOfferRepository jobOfferRepository;

    public List<JobOffer> getLatestJobOffers() {
        return jobOfferRepository.findAllByOrderByGmtCreatedDesc();
    }
}