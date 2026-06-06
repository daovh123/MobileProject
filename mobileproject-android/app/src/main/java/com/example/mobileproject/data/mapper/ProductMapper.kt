package com.example.mobileproject.data.mapper

import com.example.mobileproject.data.model.ProductDto
import com.example.mobileproject.domain.entity.Product

/**
 * Ánh xạ [ProductDto] sang [Product] domain entity.
 *
 * Mapping 1:1, không có transformation hay default value.
 * Cả hai trường [id] và [name] đều required từ API nên không cần xử lý null.
 */
fun ProductDto.toDomain(): Product = Product(
    id = id,
    name = name,
)
