package com.mobileproject.mobileprojectbackend.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Entity lưu thông tin cặp đôi đã ghép, ánh xạ tới collection {@code couple_info} trên MongoDB.
 *
 * <p>Được tạo khi hai người dùng chấp nhận ghép đôi. Mỗi cặp đôi có một CoupleInfo duy nhất
 * với ID canonical: {@code couple:{min(userId1,userId2)}:{max(userId1,userId2)}}.</p>
 *
 * <p>Lưu ý: Entity này hỗ trợ cả 2 tên field trên MongoDB (snake_case mới và camelCase cũ)
 * để tương thích ngược với dữ liệu legacy.</p>
 */
@Getter
@Setter
@Document(collection = "couple_info")
public class CoupleInfo {

    /** ID duy nhất của cặp đôi (canonical format). */
    @Id
    private String id;

    /** ID cặp đôi (snake_case field trên MongoDB). */
    @Field("id_couple")
    private String idCouple;

    /** ID cặp đôi (camelCase legacy field trên MongoDB). */
    @Field("idCouple")
    private String legacyIdCouple;

    /** ID người dùng thứ nhất (snake_case). */
    @Field("id_user1")
    private String idUser1;

    /** ID người dùng thứ nhất (camelCase legacy). */
    @Field("idUser1")
    private String legacyIdUser1;

    /** ID người dùng thứ hai (snake_case). */
    @Field("id_user2")
    private String idUser2;

    /** ID người dùng thứ hai (camelCase legacy). */
    @Field("idUser2")
    private String legacyIdUser2;

    /** Thời điểm bắt đầu mối quan hệ (ISO-8601 instant, snake_case). */
    @Field("start_at")
    private String startAt;

    /** Thời điểm bắt đầu mối quan hệ (camelCase legacy). */
    @Field("startAt")
    private String legacyStartAt;

    /** Tổng số dư ví chung của cặp đôi (đơn vị: VND). */
    @Field("total_balance")
    private Long totalBalance;

    public CoupleInfo() {
        this.totalBalance = 0L;
    }

    public String getStartAt() {
        return startAt != null ? startAt : legacyStartAt;
    }

    public String getIdCouple() {
        return idCouple != null ? idCouple : (legacyIdCouple != null ? legacyIdCouple : id);
    }

    public String getIdUser1() {
        return idUser1 != null ? idUser1 : legacyIdUser1;
    }

    public String getIdUser2() {
        return idUser2 != null ? idUser2 : legacyIdUser2;
    }
}
