package com.mobileproject.mobileprojectbackend.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@Document(collection = "couple_info")
public class CoupleInfo {

    @Id
    private String id;

    @Field("id_couple")
    private String idCouple;

    @Field("idCouple")
    private String legacyIdCouple;

    @Field("id_user1")
    private String idUser1;

    @Field("idUser1")
    private String legacyIdUser1;

    @Field("id_user2")
    private String idUser2;

    @Field("idUser2")
    private String legacyIdUser2;

    @Field("start_at")
    private String startAt;

    @Field("startAt")
    private String legacyStartAt;

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
