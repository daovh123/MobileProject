package com.example.mobileproject.presentation.viewmodel

import com.example.mobileproject.domain.entity.ExplorePlanItem
import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.utils.formatSimpleAmount
import java.text.Normalizer
import java.util.Locale

/**
 * Dữ liệu đã parse từ tin nhắn chat chứa thông tin quán ăn/uống trong kế hoạch khám phá.
 * Được dùng để render card UI trong chat thay vì hiển thị text thuần.
 *
 * @property stopOrder Số thứ tự điểm dừng trong kế hoạch (null nếu chỉ chia sẻ lẻ)
 * @property title Tên quán
 * @property imageUrl URL hình ảnh quán (nếu có)
 * @property experienceType Loại trải nghiệm: "An chinh" / "Cafe / do uong"
 * @property estimatedCostLabel Nhãn chi phí dự kiến đã format (vd: "50,000d")
 * @property reason Lý do gợi ý quán này
 * @property address Địa chỉ đầy đủ (đã ghép quận, tỉnh)
 * @property googleMapsUrl Link Google Maps (từ API hoặc tự tạo từ tọa độ)
 */
data class ExplorePlanChatCardData(
    val stopOrder: Int?,
    val title: String,
    val imageUrl: String?,
    val experienceType: String?,
    val estimatedCostLabel: String?,
    val reason: String?,
    val address: String?,
    val googleMapsUrl: String?,
)

/**
 * Formatter chuyển đổi thông tin quán ăn/uống thành tin nhắn text có cấu trúc
 * để gửi qua chat giữa cặp đôi.
 *
 * Cấu trúc tin nhắn:
 * ```
 * Goi y hen ho
 * Diem dung: 1
 * Quan: Tên quán
 * Anh: URL
 * Loai trai nghiem: An chinh
 * Chi phi du kien: 50,000d
 * Ly do: ...
 * Dia chi: ...
 * Google Maps: ...
 * ```
 *
 * Tin nhắn có thể được parse ngược lại bởi [ExplorePlanChatCardParser].
 */
object ExplorePlanChatMessageFormatter {

    private const val header = "Goi y hen ho"
    private const val stopLabel = "Diem dung"
    private const val titleLabel = "Quan"
    private const val imageLabel = "Anh"
    private const val typeLabel = "Loai trai nghiem"
    private const val costLabel = "Chi phi du kien"
    private const val reasonLabel = "Ly do"
    private const val addressLabel = "Dia chi"
    private const val mapsLabel = "Google Maps"

    /**
     * Format một [ExplorePlanItem] thành tin nhắn text có cấu trúc.
     *
     * @param item Điểm dừng trong kế hoạch khám phá
     * @return Chuỗi text đã format, mỗi dòng một trường thông tin
     */
    fun build(item: ExplorePlanItem): String {
        return build(
            name = item.place.name,
            address = item.place.address,
            district = item.place.district,
            province = item.place.province,
            googleMapsUrl = item.place.googleMapsUrl,
            lat = item.place.lat,
            lng = item.place.lng,
            imageUrl = item.place.imageUrl,
            stopOrder = item.stopOrder,
            estimatedCost = item.estimatedCost,
            experienceType = item.experienceType,
            reason = item.reason,
        )
    }

    /**
     * Format một [Place] thành tin nhắn text (không có stopOrder, cost, reason).
     *
     * @param place Địa điểm
     * @return Chuỗi text đã format
     */
    fun build(place: Place): String {
        return build(
            name = place.name,
            address = place.address,
            district = place.district,
            province = place.province,
            googleMapsUrl = place.googleMapsUrl,
            lat = place.lat,
            lng = place.lng,
            imageUrl = place.imageUrl,
        )
    }

    /**
     * Build tin nhắn từ các tham số rời rạc.
     * Các tham số null sẽ bị bỏ qua (không thêm dòng).
     *
     * @param name Tên quán (bắt buộc, mặc định "Dia diem" nếu null)
     * @param address Địa chỉ
     * @param district Quận/huyện
     * @param province Tỉnh/thành phố
     * @param googleMapsUrl Link Google Maps trực tiếp
     * @param lat Vĩ độ (dùng tạo link Maps nếu không có googleMapsUrl)
     * @param lng Kinh độ
     * @param imageUrl URL hình ảnh
     * @param stopOrder Số thứ tự điểm dừng
     * @param estimatedCost Chi phí dự kiến (VND)
     * @param experienceType Loại trải nghiệm ("food" -> "An chinh", "drink" -> "Cafe / do uong")
     * @param reason Lý do gợi ý
     * @return Chuỗi text nhiều dòng
     */
    fun build(
        name: String?,
        address: String?,
        district: String?,
        province: String?,
        googleMapsUrl: String?,
        lat: Double?,
        lng: Double?,
        imageUrl: String? = null,
        stopOrder: Int? = null,
        estimatedCost: Long? = null,
        experienceType: String? = null,
        reason: String? = null,
    ): String {
        val resolvedName = normalizedValue(name) ?: "Dia diem"
        val resolvedAddress = buildAddress(address, district, province)
        val resolvedGoogleMapsUrl = resolveGoogleMapsUrl(googleMapsUrl, lat, lng)

        return buildList {
            add(header)
            stopOrder?.let { add("$stopLabel: $it") }
            add("$titleLabel: $resolvedName")
            normalizedValue(imageUrl)?.let { add("$imageLabel: $it") }
            humanizeExperienceType(experienceType)?.let { add("$typeLabel: $it") }
                estimatedCost?.takeIf { it > 0 }?.let { add("$costLabel: ${formatSimpleAmount(it)}d") }
            normalizedValue(reason)?.let { add("$reasonLabel: $it") }
            resolvedAddress?.let { add("$addressLabel: $it") }
            resolvedGoogleMapsUrl?.let { add("$mapsLabel: $it") }
        }.joinToString("\n")
    }

    /**
     * Trả về label đã format cho một key cụ thể, dùng cho unit test.
     *
     * @param key Tên trường cần lấy label (vd: "header", "stop", "title", "image", "type", "cost", "reason", "address", "maps")
     * @return Chuỗi label đã format, hoặc rỗng nếu key không hợp lệ
     */
    internal fun labelForTesting(key: String): String {
        return when (key) {
            "header" -> header
            "stop" -> stopLabel
            "title" -> titleLabel
            "image" -> imageLabel
            "type" -> typeLabel
            "cost" -> costLabel
            "reason" -> reasonLabel
            "address" -> addressLabel
            "maps" -> mapsLabel
            else -> ""
        }
    }

    private fun humanizeExperienceType(experienceType: String?): String? {
        val normalized = normalizedValue(experienceType) ?: return null
        return when (normalized.lowercase(Locale.getDefault())) {
            "food" -> "An chinh"
            "drink" -> "Cafe / do uong"
            else -> normalized.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }
    }

    private fun resolveGoogleMapsUrl(googleMapsUrl: String?, lat: Double?, lng: Double?): String? {
        normalizedValue(googleMapsUrl)?.let { return it }

        if (lat != null && lng != null) {
            return "https://www.google.com/maps/search/?api=1&query=$lat,$lng"
        }

        return null
    }

    private fun buildAddress(address: String?, district: String?, province: String?): String? {
        return listOf(
            normalizedValue(address),
            normalizedValue(district),
            normalizedValue(province),
        ).filterNotNull().takeIf { it.isNotEmpty() }?.joinToString(", ")
    }

    private fun normalizedValue(value: String?): String? {
        return value?.trim()?.takeIf { it.isNotBlank() }
    }
}

/**
 * Parser chuyển đổi tin nhắn text có cấu trúc (từ [ExplorePlanChatMessageFormatter])
 * ngược lại thành [ExplorePlanChatCardData] để render card UI trong chat.
 *
 * Sử dụng normalize Unicode (NFD + loại bỏ dấu) để so sánh label không phân biệt dấu tiếng Việt.
 */
object ExplorePlanChatCardParser {

    /**
     * Parse chuỗi tin nhắn text thành [ExplorePlanChatCardData].
     *
     * @param messageText Nội dung tin nhắn chat
     * @return [ExplorePlanChatCardData] nếu parse thành công, null nếu không đúng định dạng
     *   (dòng đầu phải là header "Goi y hen ho")
     */
    fun parse(messageText: String): ExplorePlanChatCardData? {
        val lines = messageText.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (!matchesLabel(lines.firstOrNull(), "header")) {
            return null
        }

        var stopOrder: Int? = null
        var title: String? = null
        var imageUrl: String? = null
        var experienceType: String? = null
        var estimatedCostLabel: String? = null
        var reason: String? = null
        var address: String? = null
        var googleMapsUrl: String? = null

        lines.drop(1).forEach { line ->
            val separatorIndex = line.indexOf(':')
            if (separatorIndex <= 0) {
                return@forEach
            }

            val key = line.substring(0, separatorIndex).trim()
            val value = line.substring(separatorIndex + 1).trim().takeIf { it.isNotBlank() } ?: return@forEach

            when {
                matchesLabel(key, "stop") -> stopOrder = value.toIntOrNull()
                matchesLabel(key, "title") -> title = value
                matchesLabel(key, "image") -> imageUrl = value
                matchesLabel(key, "type") -> experienceType = value
                matchesLabel(key, "cost") -> estimatedCostLabel = value
                matchesLabel(key, "reason") -> reason = value
                matchesLabel(key, "address") -> address = value
                matchesLabel(key, "maps") -> googleMapsUrl = value
            }
        }

        val resolvedTitle = title ?: return null
        return ExplorePlanChatCardData(
            stopOrder = stopOrder,
            title = resolvedTitle,
            imageUrl = imageUrl,
            experienceType = experienceType,
            estimatedCostLabel = estimatedCostLabel,
            reason = reason,
            address = address,
            googleMapsUrl = googleMapsUrl,
        )
    }

    private fun matchesLabel(value: String?, key: String): Boolean {
        return normalizeLabel(value) == normalizeLabel(ExplorePlanChatMessageFormatter.labelForTesting(key))
    }

    private fun normalizeLabel(value: String?): String {
        return value
            ?.let { Normalizer.normalize(it, Normalizer.Form.NFD) }
            ?.replace(Regex("\\p{Mn}+"), "")
            ?.lowercase(Locale.getDefault())
            .orEmpty()
    }
}
