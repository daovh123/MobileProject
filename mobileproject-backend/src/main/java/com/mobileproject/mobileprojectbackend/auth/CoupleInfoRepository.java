package com.mobileproject.mobileprojectbackend.auth;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CoupleInfoRepository extends MongoRepository<CoupleInfo, String> {

    Optional<CoupleInfo> findFirstByIdUser1OrIdUser2(String idUser1, String idUser2);

    List<CoupleInfo> findByIdUser1AndIdUser2(String idUser1, String idUser2);
}
