package com.mobileproject.mobileprojectbackend.analytics;

import com.mobileproject.mobileprojectbackend.analytics.dto.CategoryBreakdownItem;
import com.mobileproject.mobileprojectbackend.analytics.dto.MonthlyTrendItem;
import com.mobileproject.mobileprojectbackend.analytics.dto.SpendingTrendItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/category-breakdown")
    public ResponseEntity<List<CategoryBreakdownItem>> getCategoryBreakdown(
            @RequestParam(required = false) String coupleId,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate) {
        List<CategoryBreakdownItem> result = analyticsService.getCategoryBreakdown(coupleId, startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/spending-trend")
    public ResponseEntity<List<SpendingTrendItem>> getSpendingTrend(
            @RequestParam(required = false) String coupleId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        List<SpendingTrendItem> result = analyticsService.getSpendingTrend(coupleId, year, month);
        return ResponseEntity.ok(result);
    }

    // New endpoint: expense by category for a specific month/year
    @GetMapping("/expense-by-category")
    public ResponseEntity<List<CategoryBreakdownItem>> expenseByCategory(
            @RequestParam String coupleId,
            @RequestParam int month,
            @RequestParam int year) {
        List<CategoryBreakdownItem> result = analyticsService.getExpenseByCategory(coupleId, month, year);
        return ResponseEntity.ok(result);
    }

    // New endpoint: monthly trend (income/expense) for a given year
    @GetMapping("/monthly-trend")
    public ResponseEntity<List<MonthlyTrendItem>> monthlyTrend(
            @RequestParam String coupleId,
            @RequestParam int year) {
        List<MonthlyTrendItem> result = analyticsService.getMonthlyTrend(coupleId, year);
        return ResponseEntity.ok(result);
    }
}