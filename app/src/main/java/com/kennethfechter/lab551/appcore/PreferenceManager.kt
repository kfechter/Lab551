package com.kennethfechter.lab551.appcore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lab551_settings")
val THEME_SETTING = intPreferencesKey("theme_setting")
val ANALYTICS_SETTING = intPreferencesKey("analytics_setting")
val APP_UID = stringPreferencesKey("app_uid")

suspend fun Context.setAppTheme(theme: Theme) {
    dataStore.edit { settings ->
        settings[THEME_SETTING] = when (theme) {
            Theme.Day -> 1
            Theme.Night -> 2
            Theme.PowerSave -> 3
            Theme.System -> 4
        }
    }
}

suspend fun Context.setUID(appUID: String) {
    dataStore.edit { preferences ->
        preferences[APP_UID] = appUID
    }
}

suspend fun Context.setAnalytics(analyticsEnabled: Boolean) {
    dataStore.edit { preferences ->
        preferences[ANALYTICS_SETTING] = when (analyticsEnabled) {
            false -> 0
            true -> 1
        }
    }
}

fun Context.readTheme(): Flow<Theme> {
    return dataStore.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preference ->
            when (preference[THEME_SETTING] ?: 1) {
                1 -> Theme.Day
                2 -> Theme.Night
                3 -> Theme.PowerSave
                4 -> Theme.System
                else -> Theme.Day
            }
        }
}

suspend fun Context.readUID(): String {
    return dataStore.data.first()[APP_UID] ?: ""
}

fun Context.readAnalytics(): Flow<Int> {
    return dataStore.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preference ->
            preference[ANALYTICS_SETTING] ?: -1
        }
}