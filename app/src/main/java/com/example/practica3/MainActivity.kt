package com.example.practica3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.practica3.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    // Esta función maneja el clic en la flecha "atrás"
    override fun onSupportNavigateUp(): Boolean {
        // Si el NavController puede navegar hacia arriba, lo hace.
        // Si no, activamos la lógica del OnBackPressedDispatcher (nuestro callback).
        return navController.navigateUp() || run {
            onBackPressedDispatcher.onBackPressed()
            true
        }
    }
}