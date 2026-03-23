package com.example.mobileproject.data.mapper

import com.example.mobileproject.data.model.ProductDto
import com.example.mobileproject.domain.entity.Product

fun ProductDto.toDomain(): Product = Product(
    id = id,
    name = name,
)
