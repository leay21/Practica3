package com.example.practica3

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para crear la instancia de DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(context: Context) {

    private val dataStore = context.dataStore

    // Clave para guardar el nombre del tema
    companion object {
        val THEME_KEY = stringPreferencesKey("theme_preference")
        const val THEME_GUINDA = "guinda"
        const val THEME_AZUL = "azul"
    }

    // Flujo para leer el tema guardado
    val themeFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: THEME_GUINDA // Guinda será el default
    }

    // Función para guardar el tema
    suspend fun setTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }
}