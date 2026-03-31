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

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val getProductUseCase: GetProductUseCase,
) : ViewModel() {

    private val _products = MutableStateFlow<Resource<List<Product>>>(Resource.Loading)
    val products: StateFlow<Resource<List<Product>>> = _products.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _products.value = Resource.Loading
            runCatching { getProductUseCase() }
                .onSuccess { _products.value = Resource.Success(it) }
                .onFailure { _products.value = Resource.Error(it) }
        }
    }
}
