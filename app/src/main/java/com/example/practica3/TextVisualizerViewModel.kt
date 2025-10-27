package com.example.practica3

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class TextVisualizerViewModel : ViewModel() {

    private val _fileContent = MutableLiveData<String>()
    val fileContent: LiveData<String> = _fileContent

    fun loadFileContent(filePath: String) {
        viewModelScope.launch {
            val content = withContext(Dispatchers.IO) {
                try {
                    val file = File(filePath)
                    if (file.canRead()) {
                        file.readText()
                    } else {
                        "Error: No se puede leer el archivo."
                    }
                } catch (e: Exception) {
                    "Error al abrir el archivo: ${e.message}"
                }
            }
            _fileContent.value = content
        }
    }
}