package com.mobileproject.mobileprojectbackend.place;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Document(collection = "places")
public class Place implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    private String name;
    private String address;
    private String district;
    private String category;
    private String mealType;
    private Double rating;
    private Integer reviewCount;
    private String openHours;
    private String priceRange;
    private String imageUrl;
    private Boolean isPinned;
    private String googleMapsUrl;
    private Double lat;
    private Double lng;
    private String province;
    private String normalizedDistrict;
    private String effectiveTag;
    private String searchString;
    private Boolean isFood;
    private Boolean isDrink;
}