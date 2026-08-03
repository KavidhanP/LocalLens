package com.prog7314.locallens.data.biometric

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "biometric_prefs")

class BiometricPreferencesRepository(private val context: Context) {

    private val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")

    val biometricEnabled: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[BIOMETRIC_ENABLED_KEY]
        }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BIOMETRIC_ENABLED_KEY] = enabled
        }
    }
}
