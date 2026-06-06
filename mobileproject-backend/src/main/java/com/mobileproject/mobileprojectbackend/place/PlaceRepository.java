package com.mobileproject.mobileprojectbackend.place;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository truy vấn collection {@code places} trên MongoDB.
 *
 * <p>Cung cấp các phương thức tìm kiếm theo URL hình ảnh, phục vụ cho
 * quá trình backfill ảnh GPS-CS và làm sạch dữ liệu ảnh.</p>
 */
public interface PlaceRepository extends MongoRepository<Place, String> {

	/**
	 * Tìm tất cả địa điểm có imageUrl chứa chuỗi con (không phân biệt hoa thường).
	 * Dùng để xác định các ảnh GPS-CS cần thay thế.
	 *
	 * @param imageUrlPart chuỗi con cần khớp
	 * @return danh sách địa điểm phù hợp
	 */
	List<Place> findByImageUrlContainingIgnoreCase(String imageUrlPart);

	/**
	 * Tìm tất cả địa điểm có imageUrl nằm trong danh sách cho trước.
	 * Dùng để xác định các ảnh fallback Wikimedia hiện có trong DB.
	 *
	 * @param imageUrls danh sách URL cần khớp
	 * @return danh sách địa điểm phù hợp
	 */
	List<Place> findByImageUrlIn(List<String> imageUrls);
}