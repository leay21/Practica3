package com.example.practica3

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels // Importante
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.practica3.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fileAdapter: FileAdapter
    private val fileViewModel: FileViewModel by viewModels() // Inicializa el ViewModel

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        if (checkStoragePermissions()) {
            observeViewModel()
        } else {
            requestStoragePermissions()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewFiles.layoutManager = LinearLayoutManager(this)
        // Inicializamos el adapter con una lista vacía. El ViewModel la llenará.
        fileAdapter = FileAdapter(this, emptyList()) { file ->
            onFileClicked(file)
        }
        binding.recyclerViewFiles.adapter = fileAdapter
    }

    // El ViewModel se encarga de la lógica, la Activity solo observa los resultados
    private fun observeViewModel() {
        fileViewModel.files.observe(this) { files ->
            // Creamos un nuevo adapter con la nueva lista y lo asignamos
            fileAdapter = FileAdapter(this, files) { file -> onFileClicked(file) }
            binding.recyclerViewFiles.adapter = fileAdapter
        }

        fileViewModel.currentPath.observe(this) { path ->
            binding.tvCurrentPath.text = path
        }
    }

    private fun onFileClicked(file: File) {
        if (file.isDirectory) {
            fileViewModel.loadFiles(file.absolutePath)
        } else {
            Toast.makeText(this, "Abriendo ${file.name}", Toast.LENGTH_SHORT).show()
            // Lógica de Intents
        }
    }

    override fun onBackPressed() {
        // Le delegamos la lógica al ViewModel
        if (!fileViewModel.navigateBack()) {
            super.onBackPressed() // Si el ViewModel no lo manejó, hacemos lo normal
        }
    }

    // --- La gestión de permisos se queda en la Activity, porque es parte de la UI y el contexto ---
    private fun checkStoragePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            observeViewModel()
            fileViewModel.loadFiles(Environment.getExternalStorageDirectory().absolutePath) // Carga inicial
        } else {
            Toast.makeText(this, "Permiso de almacenamiento denegado.", Toast.LENGTH_LONG).show()
        }
    }
}