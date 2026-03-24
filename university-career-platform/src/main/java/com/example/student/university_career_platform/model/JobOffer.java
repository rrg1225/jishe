package com.example.student.university_career_platform.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "job_offers")
public class JobOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tech_stack")
    private String techStack;

    @Column(name = "job_name")
    private String jobName;

    @Column(name = "salary")
    private String salary;

    @Column(name = "requirements")
    private String requirements;

    @Column(name = "company_name")
    private String companyName;


    @Column(name = "industry")
    private String industry;

    @Column(name = "financing_stage")
    private String financingStage;

    @Column(name = "area")
    private String area;

    @Column(name = "job_description")
    private String jobDescription;

    @Column(name = "gmt_created", updatable = false)
    private LocalDateTime gmtCreated;

    @Column(name = "gmt_modified")
    private LocalDateTime gmtModified;

    @PrePersist
    protected void onCreate() {
        this.gmtCreated = LocalDateTime.now();
        this.gmtModified = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        this.gmtModified = LocalDateTime.now();
    }
}
