package com.example.practica3

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        // Claves de Tema (existentes)
        val THEME_KEY = stringPreferencesKey("theme_preference")
        const val THEME_GUINDA = "guinda"
        const val THEME_AZUL = "azul"

        // Claves de Modo de Vista
        val VIEW_MODE_KEY = stringPreferencesKey("view_mode_preference")
        const val VIEW_MODE_LIST = "list"
        const val VIEW_MODE_GRID = "grid"
    }

    // Flujo de Tema (existente)
    val themeFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: THEME_GUINDA
    }

    // Función de Tema (existente)
    suspend fun setTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    // Flujo para leer el modo de vista
    val viewModeFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[VIEW_MODE_KEY] ?: VIEW_MODE_LIST // Lista será el default
    }

    // Función para guardar el modo de vista
    suspend fun setViewMode(viewMode: String) {
        dataStore.edit { preferences ->
            preferences[VIEW_MODE_KEY] = viewMode
        }
    }
}