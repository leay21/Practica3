package com.example.practica3

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import android.os.Environment
import android.app.Application // <-- AÑADE ESTE
import androidx.lifecycle.AndroidViewModel // <-- CAMBIA ViewModel por AndroidViewModel
import kotlinx.coroutines.flow.Flow // <-- AÑADE SI NO ESTÁ

enum class ClipboardOperation {
    COPY, MOVE
}

data class ClipboardAction(
    val file: File,
    val operation: ClipboardOperation
)
class FileViewModel(application: Application) : AndroidViewModel(application) {
    private val _breadcrumbParts = MutableLiveData<List<File>>()
    val breadcrumbParts: LiveData<List<File>> = _breadcrumbParts
    private val _files = MutableLiveData<List<File>>()
    val files: LiveData<List<File>> = _files

    private val _currentPath = MutableLiveData<String>()
    val currentPath: LiveData<String> = _currentPath

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    private val _clipboard = MutableLiveData<ClipboardAction?>(null)
    val clipboard: LiveData<ClipboardAction?> = _clipboard
    private val favoriteDao = AppDatabase.getDatabase(application).favoriteDao()
    private val recentFileDao = AppDatabase.getDatabase(application).recentFileDao()

    val allFavorites: Flow<List<FavoriteFile>> = favoriteDao.getAllFavorites()
    val recentFiles: Flow<List<RecentFile>> = recentFileDao.getRecentFiles()
    private val _searchResults = MutableLiveData<List<File>>()
    val searchResults: LiveData<List<File>> = _searchResults // Resultados de la búsqueda

    private val _isSearchActive = MutableLiveData<Boolean>(false)
    val isSearchActive: LiveData<Boolean> = _isSearchActive // Indica si estamos buscando

    fun setSearchActive(isActive: Boolean) {
        _isSearchActive.value = isActive
        if (!isActive) {
            // Si desactivamos la búsqueda, limpiamos los resultados
            _searchResults.value = emptyList()
        }
    }

    // Busca archivos/carpetas en el directorio actual (no recursivo por ahora)
    fun searchFiles(query: String) {
        if (!_isSearchActive.value!!) return // No buscar si no está activo

        val currentFiles = _files.value ?: emptyList() // Busca en la lista actual

        viewModelScope.launch(Dispatchers.Default) { // Usamos Default para filtrar
            val results = if (query.isBlank()) {
                emptyList() // Si la búsqueda está vacía, no mostramos nada
            } else {
                currentFiles.filter { file ->
                    file.name.contains(query, ignoreCase = true)
                    // Podrías añadir filtros por tipo o fecha aquí
                }
            }
            _searchResults.postValue(results) // Actualiza los resultados
        }
    }

    // Añade un archivo al historial
    fun addRecentFile(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            val recent = RecentFile(path = file.absolutePath, name = file.name)
            recentFileDao.insert(recent)
        }
    }

    // Añade o quita un archivo de favoritos
    fun toggleFavorite(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            val path = file.absolutePath
            if (favoriteDao.isFavorite(path)) {
                // Si ya es favorito, lo eliminamos
                favoriteDao.delete(FavoriteFile(path = path, name = file.name))
                // (Podrías enviar un evento a la UI para mostrar un Toast "Eliminado de favoritos")
            } else {
                // Si no es favorito, lo añadimos
                favoriteDao.insert(FavoriteFile(path = path, name = file.name))
                // (Podrías enviar un evento a la UI para mostrar un Toast "Añadido a favoritos")
            }
        }
    }

    // Función para saber si un archivo es favorito (útil para la UI)
    // No es 'suspend' porque queremos observarlo como LiveData o StateFlow en el futuro
    // Por ahora, lo dejamos así para usarlo directamente si es necesario.
    // suspend fun isFavorite(path: String): Boolean {
    //     return favoriteDao.isFavorite(path)
    // }
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

                    // Genera la lista de breadcrumbs
                    val parts = mutableListOf<File>()
                    var currentFile = File(path)
                    val rootPath = Environment.getExternalStorageDirectory().absolutePath

                    // Sube por el árbol de directorios hasta llegar a la raíz
                    while (currentFile.absolutePath != rootPath && currentFile.parentFile != null) {
                        parts.add(currentFile)
                        currentFile = currentFile.parentFile
                    }
                    parts.add(File(rootPath)) // Añade la raíz ("Almacenamiento")
                    _breadcrumbParts.value = parts.reversed() // Le damos la vuelta para el orden correcto
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
    fun setClipboard(file: File, operation: ClipboardOperation) {
        _clipboard.value = ClipboardAction(file, operation)
    }

    fun clearClipboard() {
        _clipboard.value = null
    }

    fun pasteClipboard() {
        val action = _clipboard.value ?: return
        val destinationDir = File(currentPath.value ?: return)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                when (action.operation) {
                    ClipboardOperation.COPY -> {
                        copyFileOrDirectory(action.file, destinationDir)
                    }
                    ClipboardOperation.MOVE -> {
                        moveFileOrDirectory(action.file, destinationDir)
                    }
                }
                // Limpiamos el portapapeles y recargamos
                _clipboard.postValue(null)
                loadFiles(destinationDir.absolutePath)
            } catch (e: Exception) {
                _error.postValue("Error al pegar: ${e.message}")
            }
        }
    }
    private fun copyFileOrDirectory(source: File, destinationDir: File) {
        val newFile = File(destinationDir, source.name)
        if (source.isDirectory) {
            source.copyRecursively(newFile, overwrite = true)
        } else {
            source.copyTo(newFile, overwrite = true)
        }
    }

    private fun moveFileOrDirectory(source: File, destinationDir: File) {
        val newFile = File(destinationDir, source.name)
        // renameTo es la forma atómica de "mover"
        if (!source.renameTo(newFile)) {
            // Si renameTo falla (ej. entre diferentes discos), recurrimos a copiar y borrar
            copyFileOrDirectory(source, destinationDir)
            if (source.isDirectory) source.deleteRecursively() else source.delete()
        }
    }
}