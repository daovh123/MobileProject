package com.mobileproject.mobileprojectbackend.map;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Entity lưu vị trí hiện tại của user trong couple.
 * Ánh xạ tới collection {@code user_locations}.
 *
 * <p><strong>GeoSpatial index:</strong> Trường {@code location} được index kiểu
 * {@code 2dsphere} để hỗ trợ truy vấn khoảng cách địa lý (near, within).</p>
 */
@Getter
@Setter
@Document(collection = "user_locations")
public class UserLocation {

    /** ID (format: coupleId:userId). */
    @Id
    private String id;

    /** ID cặp đôi. */
    @Field("id_couple")
    private String coupleId;

    /** ID người dùng. */
    @Field("id_user")
    private String userId;

    /** Tọa độ GeoJSON (longitude, latitude). */
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint location;

    /** Thời điểm cập nhật vị trí (ISO-8601 string). */
    private String updatedAt;
}
