package com.example.student.university_career_platform.service;

import com.example.student.university_career_platform.model.JobOfferDoc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EsJobSearchService {

    // 使用较新的 ElasticsearchOperations 替代废弃的 ElasticsearchTemplate
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    /**
     * 多条件+高亮全文检索
     */
    public List<JobOfferDoc> searchJobsWithHighlight(String keyword, String city) {

        // 1. 构建查询条件 (检索 Title 或 Description 包含 keyword 的文档)
        Criteria criteria = new Criteria("title").matches(keyword)
                .or(new Criteria("description").matches(keyword));

        // 如果传了城市参数，则增加精确匹配条件
        if (city != null && !city.isEmpty()) {
            criteria.and(new Criteria("city").is(city));
        }

        // 2. 配置高亮参数 (前缀和后缀 HTML 标签)
        HighlightParameters parameters = HighlightParameters.builder()
                .withPreTags("<em style='color:red;'>")
                .withPostTags("</em>")
                .build();

        // 指定哪些字段需要高亮
        Highlight highlight = new Highlight(parameters, List.of(
                new HighlightField("title"),
                new HighlightField("description")
        ));

        HighlightQuery highlightQuery = new HighlightQuery(highlight, JobOfferDoc.class);

        // 3. 组装查询对象
        CriteriaQuery query = new CriteriaQuery(criteria);
        query.setHighlightQuery(highlightQuery);

        // 4. 执行搜索
        SearchHits<JobOfferDoc> searchHits = elasticsearchOperations.search(query, JobOfferDoc.class);

        // 5. 组装并替换高亮结果
        List<JobOfferDoc> result = new ArrayList<>();
        for (SearchHit<JobOfferDoc> hit : searchHits) {
            JobOfferDoc doc = hit.getContent();

            // 如果 ES 返回了 title 的高亮切片，就用它覆盖原本的普通 title
            List<String> highlightTitle = hit.getHighlightField("title");
            if (!highlightTitle.isEmpty()) {
                doc.setTitle(highlightTitle.get(0));
            }

            List<String> highlightDesc = hit.getHighlightField("description");
            if (!highlightDesc.isEmpty()) {
                doc.setDescription(highlightDesc.get(0));
            }

            result.add(doc);
        }

        return result;
    }
}
