package com.example.practica3

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FileViewModel : ViewModel() {

    private val _files = MutableLiveData<List<File>>()
    val files: LiveData<List<File>> = _files

    private val _currentPath = MutableLiveData<String>()
    val currentPath: LiveData<String> = _currentPath

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadFiles(path: String) {
        viewModelScope.launch {
            try {
                val fileList = withContext(Dispatchers.IO) {
                    val directory = File(path)
                    if (directory.exists() && directory.isDirectory) {
                        // Ordena: carpetas primero, luego archivos, ambos alfabéticamente
                        directory.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                    } else {
                        null
                    }
                }
                if (fileList != null) {
                    _files.value = fileList
                    _currentPath.value = path
                } else {
                    _error.value = "No se pudo acceder a la ruta."
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }

    fun createFolder(folderName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentDir = File(currentPath.value ?: return@launch)
                val newFolder = File(currentDir, folderName)
                if (!newFolder.exists()) {
                    newFolder.mkdir()
                    // Recargamos la lista para que aparezca la nueva carpeta
                    loadFiles(currentPath.value!!)
                }
            } catch (e: Exception) {
                _error.postValue("Error al crear la carpeta: ${e.message}")
            }
        }
    }

    fun renameFile(filePath: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(filePath)
                val newFile = File(file.parent, newName)
                if (file.exists()) {
                    file.renameTo(newFile)
                    loadFiles(currentPath.value!!)
                }
            } catch (e: Exception) {
                _error.postValue("Error al renombrar: ${e.message}")
            }
        }
    }

    fun deleteFile(filePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(filePath)
                if (file.exists()) {
                    // Si es una carpeta, la borramos recursivamente
                    if (file.isDirectory) {
                        file.deleteRecursively()
                    } else {
                        file.delete()
                    }
                    loadFiles(currentPath.value!!)
                }
            } catch (e: Exception) {
                _error.postValue("Error al eliminar: ${e.message}")
            }
        }
    }
}