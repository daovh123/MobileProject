package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.model.moment.MomentDto
import com.example.mobileproject.data.model.moment.MomentRequestDto
import com.example.mobileproject.domain.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoriesViewModel @Inject constructor(
    private val apiService: ApiService,
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    private val _moments = MutableStateFlow<List<MomentDto>>(emptyList())
    val moments: StateFlow<List<MomentDto>> = _moments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadMoments(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val coupleStatus = onboardingRepository.getCoupleStatus(token)
                val coupleId = coupleStatus.coupleId
                if (!coupleId.isNullOrBlank()) {
                    val authHeader = "Bearer $token"
                    val response = apiService.getMoments(coupleId, authHeader)
                    if (response.isSuccessful) {
                        _moments.value = response.body() ?: emptyList()
                    } else {
                        _error.value = "Failed to load moments"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveMoment(token: String, title: String, base64Image: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val coupleStatus = onboardingRepository.getCoupleStatus(token)
                val coupleId = coupleStatus.coupleId
                if (!coupleId.isNullOrBlank()) {
                    val authHeader = "Bearer $token"
                    val request = MomentRequestDto(coupleId, title, base64Image)
                    val response = apiService.createMoment(request, authHeader)
                    if (response.isSuccessful && response.body()?.success == true) {
                        loadMoments(token) // Reload
                    } else {
                        _error.value = response.body()?.message ?: "Failed to save moment"
                    }
                } else {
                    _error.value = "You are not paired yet"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
