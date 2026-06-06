package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.Product
import com.example.mobileproject.domain.usecase.GetProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel phục vụ màn hình danh sách sản phẩm.
 *
 * Xử lý business logic:
 * - Tải danh sách sản phẩm từ [GetProductUseCase]
 * - Quản lý trạng thái loading/success/error thông qua Resource pattern
 *
 * Tự động tải sản phẩm trong init block khi ViewModel được tạo.
 * Sử dụng runCatching để bắt lỗi và chuyển sang Resource.Error.
 */
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val getProductUseCase: GetProductUseCase,
) : ViewModel() {

    // StateFlow pattern: trực tiếp dùng Resource sealed class làm giá trị StateFlow
    // Resource.Loading / Resource.Success / Resource.Error bao bọc kết quả
    private val _products = MutableStateFlow<Resource<List<Product>>>(Resource.Loading)
    val products: StateFlow<Resource<List<Product>>> = _products.asStateFlow()

    init {
        loadProducts()
    }

    /**
     * Tải danh sách sản phẩm từ repository.
     *
     * Đặt trạng thái Loading trước khi gọi use case.
     * Cập nhật Resource.Success nếu thành công, Resource.Error nếu thất bại.
     * Sử dụng runCatching để bắt exception.
     */
    fun loadProducts() {
        viewModelScope.launch {
            _products.value = Resource.Loading
            runCatching { getProductUseCase() }
                .onSuccess { _products.value = Resource.Success(it) }
                .onFailure { _products.value = Resource.Error(it) }
        }
    }
}
