package com.mobileproject.mobileprojectbackend.place;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlaceRepository extends MongoRepository<Place, String> {
}