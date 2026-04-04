package com.mobileproject.mobileprojectbackend.favorite;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserFavoriteRepository extends MongoRepository<UserFavorite, String> {

    Optional<UserFavorite> findByUserIdAndPlaceId(String userId, String placeId);

    List<UserFavorite> findByUserIdOrderByCreatedAtDesc(String userId);

    void deleteByUserId(String userId);
}
