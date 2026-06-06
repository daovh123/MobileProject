package com.example.mobileproject.domain.model

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*

/**
 * Phân loại chi tiêu của cặp đôi, sử dụng sealed class để đảm bảo type-safety
 * và giới hạn tập hợp các danh mục hợp lệ tại compile-time.
 *
 * Mỗi danh mục thuộc một trong ba nhóm [CategoryType]:
 * - **ESSENTIALS**: chi tiêu thiết yếu hàng ngày (ăn ở, đi lại, học tập)
 * - **LIFESTYLE**: chi tiêu giải trí, hẹn hò, sở thích cá nhân
 * - **FINANCIALS**: các khoản tài chính như đầu tư, dự phòng khẩn cấp
 *
 * @property id Mã định danh duy nhất dùng để lưu trữ và đồng bộ với backend
 * @property displayName Tên hiển thị tiếng Việt trên UI
 * @property icon Icon đại diện hiển thị trên giao diện Compose
 * @property type Nhóm phân loại chi tiêu [CategoryType]
 */
sealed class ExpenseCategory(
    val id: String,
    val displayName: String,
    val icon: ImageVector,
    val type: CategoryType
) {
    /**
     * Nhóm phân loại chi tiêu, giúp phân tích và báo cáo theo từng nhóm.
     */
    enum class CategoryType {
        /** Chi tiêu thiết yếu: ăn uống, nhà ở, hóa đơn, giáo dục */
        ESSENTIALS,
        /** Chi tiêu lối sống: hẹn hò, du lịch, giải trí, quà tặng */
        LIFESTYLE,
        /** Chi tiêu tài chính: đầu tư, khẩn cấp, các khoản khác */
        FINANCIALS
    }

    // ── Essentials ──────────────────────────────────────────────────────

    /** Ăn uống: cơm nhà, quán ăn, cà phê, trà sữa, giao đồ ăn */
    object FoodDrink : ExpenseCategory("FOOD_DRINK", "Ăn uống", Icons.Default.Restaurant, CategoryType.ESSENTIALS)
    /** Di chuyển: xăng xe, grab, xe buýt, gửi xe, phí cầu đường */
    object Transport : ExpenseCategory("TRANSPORT", "Di chuyển", Icons.Default.DirectionsCar, CategoryType.ESSENTIALS)
    /** Gia dụng: dụng cụ nhà bếp, đồ vệ sinh, sửa chữa nhỏ trong nhà */
    object Household : ExpenseCategory("HOUSEHOLD", "Gia dụng", Icons.Default.Home, CategoryType.ESSENTIALS)
    /** Hóa đơn: điện, nước, internet, điện thoại, truyền hình cáp */
    object Bills : ExpenseCategory("BILLS", "Hóa đơn", Icons.Default.Receipt, CategoryType.ESSENTIALS)
    /** Tiền nhà: thuê phòng, thuê nhà, phí quản lý chung cư */
    object Rent : ExpenseCategory("RENT", "Tiền nhà", Icons.Default.Apartment, CategoryType.ESSENTIALS)
    /** Giáo dục: học phí, sách vở, khóa học online, chứng chỉ */
    object Education : ExpenseCategory("EDUCATION", "Giáo dục", Icons.Default.School, CategoryType.ESSENTIALS)

    // ── Couple & Lifestyle ──────────────────────────────────────────────

    /** Hẹn hò: ăn tối, xem phim, đi chơi riêng của cặp đôi */
    object Dating : ExpenseCategory("DATING", "Hẹn hò", Icons.Default.Favorite, CategoryType.LIFESTYLE)
    /** Quà tặng: sinh nhật, kỷ niệm, ngày lễ, quà bất ngờ cho đối phương */
    object Gifts : ExpenseCategory("GIFTS", "Quà tặng", Icons.Default.CardGiftcard, CategoryType.LIFESTYLE)
    /** Du lịch: vé máy bay, khách sạn, tour, ăn uống khi đi chơi xa */
    object Travel : ExpenseCategory("TRAVEL", "Du lịch", Icons.Default.Flight, CategoryType.LIFESTYLE)
    /** Thú cưng: thức ăn, khám bệnh, phụ kiện, spa cho thú nuôi */
    object Pet : ExpenseCategory("PET", "Thú cưng", Icons.Default.Pets, CategoryType.LIFESTYLE)
    /** Giải trí: xem phim, karaoke, game, sự kiện, concert */
    object Entertainment : ExpenseCategory("ENTERTAINMENT", "Giải trí", Icons.Default.Movie, CategoryType.LIFESTYLE)

    // ── Financials ──────────────────────────────────────────────────────

    /** Khẩn cấp: sửa xe hỏng, viện phí đột xuất, sửa chữa nhà */
    object Emergency : ExpenseCategory("EMERGENCY", "Khẩn cấp", Icons.Default.Error, CategoryType.FINANCIALS)
    /** Đầu tư: chứng khoán, crypto, gửi tiết kiệm, quỹ đầu tư */
    object Investment : ExpenseCategory("INVESTMENT", "Đầu tư", Icons.AutoMirrored.Filled.TrendingUp, CategoryType.FINANCIALS)
    /** Khác: các khoản chi không thuộc danh mục nào phía trên */
    object Others : ExpenseCategory("OTHERS", "Khác", Icons.Default.Category, CategoryType.FINANCIALS)

    companion object {
        /**
         * Trả về danh sách tất cả các danh mục chi tiêu.
         * Sử dụng function thay vì property để tránh lỗi khởi tạo
         * liên quan đến thứ tự companion object trong sealed class.
         */
        fun getAll(): List<ExpenseCategory> = listOf(
            FoodDrink, Transport, Household, Bills, Rent, Education,
            Dating, Gifts, Travel, Pet, Entertainment,
            Emergency, Investment, Others
        )
        
        /**
         * Tìm danh mục theo [id], trả về [Others] nếu không tìm thấy
         * để tránh crash khi dữ liệu cũ hoặc đồng bộ có category không hợp lệ.
         */
        fun fromId(id: String): ExpenseCategory = getAll().find { it.id == id } ?: Others
    }
}
