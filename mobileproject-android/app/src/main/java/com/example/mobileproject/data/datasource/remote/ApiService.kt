package com.example.mobileproject.data.datasource.remote

import com.example.mobileproject.data.model.auth.AuthResponseDto
import com.example.mobileproject.data.model.auth.LoginRequestDto
import com.example.mobileproject.data.model.auth.RegisterRequestDto
import com.example.mobileproject.data.model.ProductDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @GET("products")
    suspend fun getProducts(): List<ProductDto>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>
}
