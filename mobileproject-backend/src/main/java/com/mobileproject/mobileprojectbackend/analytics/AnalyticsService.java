package com.mobileproject.mobileprojectbackend.analytics;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.stereotype.Service;

import com.mobileproject.mobileprojectbackend.analytics.dto.CategoryBreakdownItem;
import com.mobileproject.mobileprojectbackend.analytics.dto.MonthlyTrendItem;
import com.mobileproject.mobileprojectbackend.analytics.dto.SpendingTrendItem;

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
            criteria = criteria.and("id_couple").is(coupleId);
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
                CategoryBreakdownItem.class);

        System.out.println("Results count: " + results.getMappedResults().size());
        return results.getMappedResults();
    }

    public List<SpendingTrendItem> getSpendingTrend(String coupleId, Integer year, Integer month) {
        if (year == null || month == null) {
            return List.of();
        }

        ZonedDateTime startOfMonth = ZonedDateTime.of(year, month, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        Instant startInstant = startOfMonth.toInstant();
        Instant endInstant = endOfMonth.toInstant();

        System.out.println("=== DEBUG getSpendingTrend ===");
        System.out.println("coupleId: " + coupleId);
        System.out.println("year: " + year + ", month: " + month);
        System.out.println("startInstant: " + startInstant);
        System.out.println("endInstant: " + endInstant);

        Criteria criteria = new Criteria();
        criteria = criteria.and("type").is("EXPENSE");

        if (coupleId != null && !coupleId.isBlank()) {
            criteria = criteria.and("id_couple").is(coupleId);
        }

        criteria = criteria.and("created_at").gte(startInstant).lte(endInstant);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group()
                        .addToSet("created_at").as("dates")
                        .sum("amount").as("totalAmount"),
                Aggregation.unwind("dates"),
                Aggregation.project()
                        .and("dates").as("date")
                        .and("totalAmount").as("totalAmount"));

        AggregationResults<?> rawResults = mongoTemplate.aggregate(
                aggregation,
                "transactions",
                Map.class);

        System.out.println("Raw results count: " + rawResults.getMappedResults().size());

        Map<Integer, Long> dailySpending = new HashMap<>();
        for (Object rawResult : rawResults.getMappedResults()) {
            if (!(rawResult instanceof Map<?, ?> result)) {
                continue;
            }
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

    public List<CategoryBreakdownItem> getExpenseByCategory(String coupleId, int month, int year) {
        System.out.println("=== DEBUG getExpenseByCategory ===");
        System.out.println("coupleId: " + coupleId);
        System.out.println("month: " + month);
        System.out.println("year: " + year);

        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        ZonedDateTime startZDT = startDate.atStartOfDay(zoneId);
        ZonedDateTime endZDT = endDate.atTime(23, 59, 59).atZone(zoneId);

        Instant startInstant = startZDT.toInstant();
        Instant endInstant = endZDT.toInstant();

        Criteria criteria = new Criteria()
                .andOperator(
                        Criteria.where("type").is("EXPENSE"),
                        Criteria.where("id_couple").is(coupleId),
                        Criteria.where("created_at").gte(startInstant).lte(endInstant)
                );

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

        List<CategoryBreakdownItem> list = results.getMappedResults();
        System.out.println("Results count: " + (list == null ? 0 : list.size()));
        return list != null ? list : new ArrayList<>();
    }

    public List<MonthlyTrendItem> getMonthlyTrend(String coupleId, int year) {
        System.out.println("=== DEBUG getMonthlyTrend ===");
        System.out.println("coupleId: " + coupleId);
        System.out.println("year: " + year);

        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");

        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);

        ZonedDateTime startZDT = startOfYear.atStartOfDay(zoneId);
        ZonedDateTime endZDT = endOfYear.atTime(23, 59, 59).atZone(zoneId);

        Instant startInstant = startZDT.toInstant();
        Instant endInstant = endZDT.toInstant();

        Criteria criteria = new Criteria()
                .andOperator(
                        Criteria.where("id_couple").is(coupleId),
                        Criteria.where("created_at").gte(startInstant).lte(endInstant)
                );

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.project()
                        .and(DateOperators.Month.monthOf("created_at")).as("month")
                        .and("type").as("type")
                        .and("amount").as("amount"),
                Aggregation.group("month", "type")
                        .sum("amount").as("total"),
                Aggregation.project()
                        .and("month").as("month")
                        .and("type").as("type")
                        .and("total").as("total"),
                Aggregation.sort(Sort.by(Sort.Direction.ASC, "month"))
        );

        AggregationResults<Map> rawResults = mongoTemplate.aggregate(
                aggregation,
                "transactions",
                Map.class
        );

        System.out.println("Raw results count: " + (rawResults.getMappedResults() == null ? 0 : rawResults.getMappedResults().size()));

        Map<Integer, Map<String, Long>> monthlyData = new HashMap<>();
        for (Map<String, Object> m : rawResults.getMappedResults()) {
            Integer monthObj = (Integer) m.get("month");
            String type = (String) m.get("type");
            Long total = ((Number) m.get("total")).longValue();
            if (monthObj == null || type == null) continue;
            monthlyData.computeIfAbsent(monthObj, k -> new HashMap<>()).put(type, total);
        }

        List<MonthlyTrendItem> trend = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            Map<String, Long> map = monthlyData.getOrDefault(m, new HashMap<>());
            Long income = map.getOrDefault("INCOME", 0L);
            Long expense = map.getOrDefault("EXPENSE", 0L);
            trend.add(new MonthlyTrendItem(m, income, expense));
        }

        return trend;
    }
}