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

// 只保留 Spring Data 最基础的包，干掉所有冲突的 ES 原生导包
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EsJobSearchService {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    /**
     * 多条件+高亮全文检索
     */
    public List<JobOfferDoc> searchJobsWithHighlight(String keyword, String city) {
        Criteria criteria = new Criteria("title").matches(keyword)
                .or(new Criteria("description").matches(keyword));

        if (city != null && !city.isEmpty()) {
            criteria.and(new Criteria("city").is(city));
        }

        HighlightParameters parameters = HighlightParameters.builder()
                .withPreTags("<em style='color:red;'>")
                .withPostTags("</em>")
                .build();

        Highlight highlight = new Highlight(parameters, List.of(
                new HighlightField("title"),
                new HighlightField("description")
        ));

        HighlightQuery highlightQuery = new HighlightQuery(highlight, JobOfferDoc.class);

        CriteriaQuery query = new CriteriaQuery(criteria);
        query.setHighlightQuery(highlightQuery);

        SearchHits<JobOfferDoc> searchHits = elasticsearchOperations.search(query, JobOfferDoc.class);

        List<JobOfferDoc> result = new ArrayList<>();
        for (SearchHit<JobOfferDoc> hit : searchHits) {
            JobOfferDoc doc = hit.getContent();

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

    /**
     * 大数据可视化：按城市统计岗位数量 (用于前台 ECharts 大屏展示)
     */
    public Map<String, Long> getJobCityDistribution() {
        // 使用全限定名避免与 Spring 的 Aggregation 产生任何 Import 冲突
        NativeQuery query = NativeQuery.builder()
                .withMaxResults(0)
                .withAggregation("by_city", co.elastic.clients.elasticsearch._types.aggregations.Aggregation.of(a -> a
                        .terms(t -> t.field("city.keyword").size(10))
                ))
                .build();

        SearchHits<JobOfferDoc> searchHits = elasticsearchOperations.search(query, JobOfferDoc.class);

        Map<String, Long> cityData = new HashMap<>();

        if (searchHits.hasAggregations()) {
            ElasticsearchAggregations aggregationsContainer = (ElasticsearchAggregations) searchHits.getAggregations();

            // 1. 获取第一层包装类：ElasticsearchAggregation
            ElasticsearchAggregation springAggregation = aggregationsContainer.aggregationsAsMap().get("by_city");

            if (springAggregation != null) {
                // 🚀 核心终极修复：连续拆两层包！
                // 第一步：.aggregation() 剥开 ElasticsearchAggregation，拿到 elc.Aggregation
                // 第二步：.getAggregate() 剥开 elc.Aggregation，真正拿到原生的 ES Aggregate
                co.elastic.clients.elasticsearch._types.aggregations.Aggregate aggregate =
                        springAggregation.aggregation().getAggregate();

                // 确保这是一个 String 类型的 Terms 聚合
                if (aggregate != null && aggregate.isSterms()) {
                    co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate termsAggregate = aggregate.sterms();

                    // 遍历每一个城市的数据桶（Bucket）
                    for (co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket bucket : termsAggregate.buckets().array()) {
                        cityData.put(bucket.key().stringValue(), bucket.docCount());
                    }
                }
            }
        }

        return cityData;
    }
}