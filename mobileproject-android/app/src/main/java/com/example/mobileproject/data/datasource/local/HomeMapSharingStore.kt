package com.example.mobileproject.data.datasource.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.homeMapSharingDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "home_map_sharing_store",
)

@Singleton
class HomeMapSharingStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.homeMapSharingDataStore

    val shareLocationEnabled: Flow<Boolean> = dataStore.data
        .map { it[KEY_SHARE_LOCATION] ?: false }
        .catch { emit(false) }

    suspend fun setShareLocationEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_SHARE_LOCATION] = enabled }
    }

    private companion object {
        val KEY_SHARE_LOCATION = booleanPreferencesKey("share_location_enabled")
    }
}
