package com.mobileproject.mobileprojectbackend.auth;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository MongoDB cho entity {@link CoupleInfo}.
 *
 * <p>Cung cấp các phương thức truy vấn thông tin cặp đôi theo ID người dùng.</p>
 */
public interface CoupleInfoRepository extends MongoRepository<CoupleInfo, String> {

    /**
     * Tìm cặp đôi có chứa một trong hai người dùng (dùng cho legacy lookup).
     *
     * @param idUser1 ID người dùng thứ nhất
     * @param idUser2 ID người dùng thứ hai
     * @return Optional chứa CoupleInfo đầu tiên tìm thấy
     */
    Optional<CoupleInfo> findFirstByIdUser1OrIdUser2(String idUser1, String idUser2);

    /**
     * Tìm tất cả cặp đôi theo cặp idUser1 và idUser2 (hỗ trợ cả 2 thứ tự).
     *
     * @param idUser1 ID người dùng thứ nhất
     * @param idUser2 ID người dùng thứ hai
     * @return danh sách CoupleInfo khớp
     */
    List<CoupleInfo> findByIdUser1AndIdUser2(String idUser1, String idUser2);
}
