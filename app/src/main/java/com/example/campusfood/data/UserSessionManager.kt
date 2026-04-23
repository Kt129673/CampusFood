package com.example.campusfood.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.campusfood.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

/**
 * Manages user session persistence using DataStore Preferences.
 * Stores user info after login so the user stays logged in across app restarts.
 */
class UserSessionManager(private val context: Context) {

    companion object {
        private val KEY_USER_ID = longPreferencesKey("user_id")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_MOBILE = stringPreferencesKey("user_mobile")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_ROLE = stringPreferencesKey("user_role")
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_LOGGED_IN] ?: false
    }

    val currentUser: Flow<User?> = context.dataStore.data.map { prefs ->
        val id = prefs[KEY_USER_ID]
        val name = prefs[KEY_USER_NAME]
        if (id != null && name != null) {
            User(
                id = id,
                name = name,
                mobile = prefs[KEY_USER_MOBILE],
                email = prefs[KEY_USER_EMAIL],
                role = prefs[KEY_USER_ROLE] ?: "CUSTOMER"
            )
        } else null
    }

    suspend fun saveSession(user: User) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = user.id
            prefs[KEY_USER_NAME] = user.name
            prefs[KEY_USER_MOBILE] = user.mobile ?: ""
            prefs[KEY_USER_EMAIL] = user.email ?: ""
            prefs[KEY_USER_ROLE] = user.role
            prefs[KEY_IS_LOGGED_IN] = true
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
