package com.mobileproject.mobileprojectbackend.place;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PlaceRepository extends MongoRepository<Place, String> {

	List<Place> findByImageUrlContainingIgnoreCase(String imageUrlPart);

	List<Place> findByImageUrlIn(List<String> imageUrls);
}