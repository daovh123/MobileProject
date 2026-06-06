package com.mobileproject.mobileprojectbackend.place;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;

/**
 * Entity đại diện cho một địa điểm (quán ăn, quán nước, ...) trong hệ thống.
 * Ánh xạ tới collection {@code places} trên MongoDB.
 *
 * <p>Hỗ trợ tìm kiếm theo tỉnh/thành phố, quận/huyện, loại (food/drink),
 * đánh giá, và khoảng cách địa lý (GeoJSON).</p>
 *
 * <p>Trường {@code searchString} được xây dựng tự động khi import để tối ưu
 * tốc độ tìm kiếm全文.</p>
 */
@Getter
@Setter
@Document(collection = "places")
public class Place implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** ID duy nhất của địa điểm (MongoDB ObjectId). */
    @Id
    private String id;

    /** Tên địa điểm. */
    private String name;

    /** Địa chỉ chi tiết. */
    private String address;

    /** Quận/huyện. */
    private String district;

    /** Phân loại (nhà hàng, cafe, ...). */
    private String category;

    /** Loại bữa ăn (sáng, trưa, tối, ...). */
    private String mealType;

    /** Điểm đánh giá trung bình (thang 0-5). */
    private Double rating;

    /** Tổng số lượt đánh giá. */
    private Integer reviewCount;

    /** Giờ mở cửa. */
    private String openHours;

    /** Khoảng giá (ví dụ: "30.000-80.000đ"). */
    private String priceRange;

    /** URL hình ảnh đại diện của địa điểm. */
    private String imageUrl;

    /** Địa điểm có được ghím (pin) hay không. */
    private Boolean isPinned;

    /** Liên kết Google Maps. */
    private String googleMapsUrl;

    /** Vĩ độ (latitude) cho tính khoảng cách. */
    private Double lat;

    /** Kinh độ (longitude) cho tính khoảng cách. */
    private Double lng;

    /** Tỉnh/thành phố. */
    private String province;

    /** Quận/huyện đã chuẩn hóa (không dấu, lowercase). */
    private String normalizedDistrict;

    /** Tag hiệu quả dùng cho phân loại và tìm kiếm. */
    private String effectiveTag;

    /**
     * Chuỗi tìm kiếm tổng hợp (全文搜索), được xây dựng từ tên, địa chỉ,
     * quận, tỉnh, tag, ... dưới dạng không dấu, lowercase.
     */
    private String searchString;

    /** Đánh dấu địa điểm là quán ăn. */
    private Boolean isFood;

    /** Đánh dấu địa điểm là quán nước. */
    private Boolean isDrink;
}