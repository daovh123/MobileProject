package com.mobileproject.mobileprojectbackend.analytics;

import com.mobileproject.mobileprojectbackend.analytics.dto.CategoryBreakdownItem;
import com.mobileproject.mobileprojectbackend.analytics.dto.SpendingTrendItem;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final MongoTemplate mongoTemplate;

    public AnalyticsService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<CategoryBreakdownItem> getCategoryBreakdown(String coupleId, Instant startDate, Instant endDate) {
        System.out.println("=== DEBUG getCategoryBreakdown ===");
        System.out.println("coupleId: " + coupleId);
        System.out.println("startDate: " + startDate);
        System.out.println("endDate: " + endDate);

        Criteria criteria = new Criteria();
        
        criteria = criteria.and("type").is("EXPENSE");
        
        if (coupleId != null && !coupleId.isBlank()) {
            criteria = criteria.and("coupleId").is(coupleId);
        }
        
        if (startDate != null && endDate != null) {
            criteria = criteria.and("created_at").gte(startDate).lte(endDate);
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("category")
                        .sum("amount").as("totalAmount"),
                Aggregation.project()
                        .and("_id").as("category")
                        .and("totalAmount").as("totalAmount")
        );

        AggregationResults<CategoryBreakdownItem> results = mongoTemplate.aggregate(
                aggregation,
                "transactions",
                CategoryBreakdownItem.class
        );

        System.out.println("Results count: " + results.getMappedResults().size());
        return results.getMappedResults();
    }

    public List<SpendingTrendItem> getSpendingTrend(String coupleId, Integer year, Integer month) {
        ZonedDateTime startOfMonth = null;
        ZonedDateTime endOfMonth = null;
        
        if (year != null && month != null) {
            startOfMonth = ZonedDateTime.of(year, month, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
            endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);
        }

        Instant startInstant = startOfMonth != null ? startOfMonth.toInstant() : null;
        Instant endInstant = endOfMonth != null ? endOfMonth.toInstant() : null;

        System.out.println("=== DEBUG getSpendingTrend ===");
        System.out.println("coupleId: " + coupleId);
        System.out.println("year: " + year + ", month: " + month);
        System.out.println("startInstant: " + startInstant);
        System.out.println("endInstant: " + endInstant);

        Criteria criteria = new Criteria();
        
        criteria = criteria.and("type").is("EXPENSE");
        
        if (coupleId != null && !coupleId.isBlank()) {
            criteria = criteria.and("coupleId").is(coupleId);
        }
        
        if (startInstant != null && endInstant != null) {
            criteria = criteria.and("created_at").gte(startInstant).lte(endInstant);
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group()
                        .addToSet("createdAt").as("dates")
                        .sum("amount").as("totalAmount"),
                Aggregation.unwind("dates"),
                Aggregation.project()
                        .and("dates").as("date")
                        .and("totalAmount").as("totalAmount")
        );

        AggregationResults<Map> rawResults = mongoTemplate.aggregate(
                aggregation,
                "transactions",
                Map.class
        );

        System.out.println("Raw results count: " + rawResults.getMappedResults().size());

        Map<Integer, Long> dailySpending = new HashMap<>();
        for (Map result : rawResults.getMappedResults()) {
            Object dateObj = result.get("date");
            if (dateObj instanceof Number) {
                int day = ((Number) dateObj).intValue();
                Number totalAmt = (Number) result.get("totalAmount");
                dailySpending.put(day, totalAmt.longValue());
            } else if (dateObj instanceof String) {
                Instant instant = Instant.parse((String) dateObj);
                ZonedDateTime zdt = instant.atZone(ZoneId.of("UTC"));
                int day = zdt.getDayOfMonth();
                Number totalAmt = (Number) result.get("totalAmount");
                dailySpending.put(day, totalAmt.longValue());
            } else if (dateObj instanceof Date) {
                ZonedDateTime zdt = ((Date) dateObj).toInstant().atZone(ZoneId.of("UTC"));
                int day = zdt.getDayOfMonth();
                Number totalAmt = (Number) result.get("totalAmount");
                dailySpending.put(day, totalAmt.longValue());
            }
        }

        int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();
        List<SpendingTrendItem> trend = new ArrayList<>();
        for (int day = 1; day <= daysInMonth; day++) {
            Long amount = dailySpending.getOrDefault(day, 0L);
            trend.add(new SpendingTrendItem(day, amount));
        }

        return trend;
    }
}