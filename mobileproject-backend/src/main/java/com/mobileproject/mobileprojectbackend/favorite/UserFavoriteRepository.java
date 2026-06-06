package com.mobileproject.mobileprojectbackend.favorite;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository truy vấn collection {@code user_favorites}.
 */
public interface UserFavoriteRepository extends MongoRepository<UserFavorite, String> {

    /** Tìm bản ghi yêu thích theo userId và placeId. */
    Optional<UserFavorite> findByUserIdAndPlaceId(String userId, String placeId);

    /** Tìm tất cả yêu thích của user, mới nhất trước. */
    List<UserFavorite> findByUserIdOrderByCreatedAtDesc(String userId);

    /** Xóa toàn bộ yêu thích của user. */
    void deleteByUserId(String userId);
}
