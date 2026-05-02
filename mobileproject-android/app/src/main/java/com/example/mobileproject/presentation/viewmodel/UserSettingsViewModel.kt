package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.data.datasource.local.UserSettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserSettingsViewModel @Inject constructor(
    private val store: UserSettingsStore,
) : ViewModel() {

    val pushNotifications: StateFlow<Boolean> = store.pushNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val emailNotifications: StateFlow<Boolean> = store.emailNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val showActivityStatus: StateFlow<Boolean> = store.showActivityStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val searchableByEmail: StateFlow<Boolean> = store.searchableByEmail
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setPushNotifications(v: Boolean) = viewModelScope.launch { store.setPushNotifications(v) }
    fun setEmailNotifications(v: Boolean) = viewModelScope.launch { store.setEmailNotifications(v) }
    fun setShowActivityStatus(v: Boolean) = viewModelScope.launch { store.setShowActivityStatus(v) }
    fun setSearchableByEmail(v: Boolean) = viewModelScope.launch { store.setSearchableByEmail(v) }
}
