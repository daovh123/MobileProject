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

    @Field("id_user1")
    private String idUser1;

    @Field("id_user2")
    private String idUser2;

    private String startAt;

    @Field("total_balance")
    private Long totalBalance;

    public CoupleInfo() {
        this.totalBalance = 0L;
    }
}
