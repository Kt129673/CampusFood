package com.example.campusfood.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * App configuration manager using DataStore.
 * Stores user preferences and app settings.
 */
private val Context.configDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_config")

class AppConfig(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val SHOW_PRODUCT_IMAGES = booleanPreferencesKey("show_product_images")
        val DATA_SAVER_MODE = booleanPreferencesKey("data_saver_mode")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        val SEARCH_HISTORY = stringSetPreferencesKey("search_history")
    }

    // Theme mode (light, dark, system)
    val themeMode: Flow<String> = context.configDataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE] ?: "system"
    }

    suspend fun setThemeMode(mode: String) {
        context.configDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode
        }
    }

    // Notifications
    val notificationsEnabled: Flow<Boolean> = context.configDataStore.data.map { prefs ->
        prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.configDataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    // Sound
    val soundEnabled: Flow<Boolean> = context.configDataStore.data.map { prefs ->
        prefs[Keys.SOUND_ENABLED] ?: true
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.configDataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = enabled
        }
    }

    // Vibration
    val vibrationEnabled: Flow<Boolean> = context.configDataStore.data.map { prefs ->
        prefs[Keys.VIBRATION_ENABLED] ?: true
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.configDataStore.edit { prefs ->
            prefs[Keys.VIBRATION_ENABLED] = enabled
        }
    }

    // Show product images
    val showProductImages: Flow<Boolean> = context.configDataStore.data.map { prefs ->
        prefs[Keys.SHOW_PRODUCT_IMAGES] ?: true
    }

    suspend fun setShowProductImages(show: Boolean) {
        context.configDataStore.edit { prefs ->
            prefs[Keys.SHOW_PRODUCT_IMAGES] = show
        }
    }

    // Data saver mode
    val dataSaverMode: Flow<Boolean> = context.configDataStore.data.map { prefs ->
        prefs[Keys.DATA_SAVER_MODE] ?: false
    }

    suspend fun setDataSaverMode(enabled: Boolean) {
        context.configDataStore.edit { prefs ->
            prefs[Keys.DATA_SAVER_MODE] = enabled
        }
    }

    // Last sync time
    val lastSyncTime: Flow<Long> = context.configDataStore.data.map { prefs ->
        prefs[Keys.LAST_SYNC_TIME] ?: 0L
    }

    suspend fun updateLastSyncTime(time: Long = System.currentTimeMillis()) {
        context.configDataStore.edit { prefs ->
            prefs[Keys.LAST_SYNC_TIME] = time
        }
    }

    // Search history
    val searchHistory: Flow<Set<String>> = context.configDataStore.data.map { prefs ->
        prefs[Keys.SEARCH_HISTORY] ?: emptySet()
    }

    suspend fun addSearchQuery(query: String) {
        context.configDataStore.edit { prefs ->
            val current = prefs[Keys.SEARCH_HISTORY] ?: emptySet()
            val updated = (current + query).takeLast(10).toSet() // Keep last 10 searches
            prefs[Keys.SEARCH_HISTORY] = updated
        }
    }

    suspend fun clearSearchHistory() {
        context.configDataStore.edit { prefs ->
            prefs.remove(Keys.SEARCH_HISTORY)
        }
    }

    // Clear all preferences
    suspend fun clearAll() {
        context.configDataStore.edit { it.clear() }
    }
}
