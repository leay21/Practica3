package com.example.practica3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.practica3.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking // <-- AÑADE ESTA IMPORTACIÓN

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Obtenemos el manager ANTES de super.onCreate
        settingsManager = SettingsManager(this)

        // --- ESTE ES EL CAMBIO CLAVE ---
        // Leemos el tema de forma SÍNCRONA (bloqueando el hilo principal)
        // Esto es necesario para que setTheme() se llame ANTES de super.onCreate()
        val theme = runBlocking {
            settingsManager.themeFlow.first()
        }
        applyTheme(theme)
        // --- FIN DEL CAMBIO ---

        // AHORA llamamos a super.onCreate() en el hilo principal
        super.onCreate(savedInstanceState)

        // Y continuamos con el resto de la inicialización de la UI
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Establecemos nuestra Toolbar del layout como la ActionBar principal
        setSupportActionBar(binding.toolbar)

        // 2. Encontramos el NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 3. Configuramos la ActionBar para que funcione con el NavController
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    // Función para aplicar el tema correcto (se queda igual)
    private fun applyTheme(theme: String) {
        if (theme == SettingsManager.THEME_AZUL) {
            setTheme(R.style.Theme_GestorDeArchivos_Azul)
        } else {
            setTheme(R.style.Theme_GestorDeArchivos_Guinda)
        }
    }

    // Esta función maneja el clic en la flecha "atrás" (se queda igual)
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || run {
            onBackPressedDispatcher.onBackPressed()
            true
        }
    }
}