package com.example.ezblue.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.scanDataStore by preferencesDataStore(name = "scan_prefs")

object ScanPreferences {
    private val SCANNING_ENABLED_KEY = booleanPreferencesKey("scanning_enabled")

    fun isScanningEnabled(context: Context): Flow<Boolean> {
        return context.scanDataStore.data
            .map { prefs -> prefs[SCANNING_ENABLED_KEY] ?: false }
    }

    suspend fun setScanningEnabled(context: Context, enabled: Boolean) {
        context.scanDataStore.edit { prefs ->
            prefs[SCANNING_ENABLED_KEY] = enabled
        }
    }
}
