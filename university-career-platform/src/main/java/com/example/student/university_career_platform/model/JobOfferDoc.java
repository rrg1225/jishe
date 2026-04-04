package com.example.student.university_career_platform.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "job_offer_index")
public class JobOfferDoc {

    @Id
    private Long id;

    // 职位标题，支持全文高亮检索
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;

    // 职位描述
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String description;

    // 城市，用于精确匹配
    @Field(type = FieldType.Keyword)
    private String city;

    // getter 和 setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}