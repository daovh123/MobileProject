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

/**
 * REST Controller cung cấp các API phân tích tài chính cho cặp đôi.
 *
 * <p>Base path: {@code /api/v1/analytics}</p>
 */
@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Phân tích chi tiêu theo danh mục với bộ lọc tùy chọn.
     *
     * <p><b>GET</b> {@code /api/v1/analytics/category-breakdown}</p>
     *
     * @param coupleId  ID cặp đôi (optional query param)
     * @param startDate thời điểm bắt đầu (optional, ISO-8601)
     * @param endDate   thời điểm kết thúc (optional, ISO-8601)
     * @return danh sách {@link CategoryBreakdownItem} với tổng tiền theo danh mục
     */
    @GetMapping("/category-breakdown")
    public ResponseEntity<List<CategoryBreakdownItem>> getCategoryBreakdown(
            @RequestParam(required = false) String coupleId,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate) {
        List<CategoryBreakdownItem> result = analyticsService.getCategoryBreakdown(coupleId, startDate, endDate);
        return ResponseEntity.ok(result);
    }

    /**
     * Lấy xu hướng chi tiêu theo ngày trong tháng.
     *
     * <p><b>GET</b> {@code /api/v1/analytics/spending-trend}</p>
     *
     * @param coupleId ID cặp đôi (optional query param)
     * @param year     năm (optional)
     * @param month    tháng 1–12 (optional)
     * @return danh sách {@link SpendingTrendItem} cho mỗi ngày trong tháng
     */
    @GetMapping("/spending-trend")
    public ResponseEntity<List<SpendingTrendItem>> getSpendingTrend(
            @RequestParam(required = false) String coupleId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        List<SpendingTrendItem> result = analyticsService.getSpendingTrend(coupleId, year, month);
        return ResponseEntity.ok(result);
    }

    // New endpoint: expense by category for a specific month/year
    /**
     * Lấy chi tiêu theo danh mục cho tháng/năm cụ thể.
     *
     * <p><b>GET</b> {@code /api/v1/analytics/expense-by-category}</p>
     * <p>Sử dụng timezone Asia/Ho_Chi_Minh.</p>
     *
     * @param coupleId ID cặp đôi (required)
     * @param month    tháng 1–12 (required)
     * @param year     năm (required)
     * @return danh sách {@link CategoryBreakdownItem}
     */
    @GetMapping("/expense-by-category")
    public ResponseEntity<List<CategoryBreakdownItem>> expenseByCategory(
            @RequestParam String coupleId,
            @RequestParam int month,
            @RequestParam int year) {
        List<CategoryBreakdownItem> result = analyticsService.getExpenseByCategory(coupleId, month, year);
        return ResponseEntity.ok(result);
    }

    // New endpoint: monthly trend (income/expense) for a given year
    /**
     * Lấy xu hướng thu/chi theo tháng trong năm.
     *
     * <p><b>GET</b> {@code /api/v1/analytics/monthly-trend}</p>
     * <p>Sử dụng timezone Asia/Ho_Chi_Minh.</p>
     *
     * @param coupleId ID cặp đôi (required)
     * @param year     năm (required)
     * @return danh sách {@link MonthlyTrendItem} cho 12 tháng
     */
    @GetMapping("/monthly-trend")
    public ResponseEntity<List<MonthlyTrendItem>> monthlyTrend(
            @RequestParam String coupleId,
            @RequestParam int year) {
        List<MonthlyTrendItem> result = analyticsService.getMonthlyTrend(coupleId, year);
        return ResponseEntity.ok(result);
    }
}