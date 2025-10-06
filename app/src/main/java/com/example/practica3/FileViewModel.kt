import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.io.File

class FileViewModel : ViewModel() {

    private val repository = FileRepository()

    // LiveData para la lista de archivos. Es privado y mutable para el ViewModel.
    private val _files = MutableLiveData<List<File>>()
    // LiveData público e inmutable para que la Activity lo observe.
    val files: LiveData<List<File>> = _files

    // LiveData para la ruta actual
    private val _currentPath = MutableLiveData<String>()
    val currentPath: LiveData<String> = _currentPath

    private val initialPath = Environment.getExternalStorageDirectory().absolutePath

    init {
        loadFiles(initialPath)
    }

    /**
     * Carga los archivos usando el repositorio dentro de una corrutina.
     */
    fun loadFiles(path: String) {
        viewModelScope.launch {
            val fileList = repository.getFiles(path)
            _files.postValue(fileList) // postValue es seguro para hilos de fondo
            _currentPath.postValue(path)
        }
    }

    /**
     * Maneja la lógica del botón 'atrás' para navegar hacia el directorio padre.
     * @return true si se manejó el evento, false si se debe llamar al comportamiento por defecto.
     */
    fun navigateBack(): Boolean {
        val current = _currentPath.value ?: initialPath
        if (current != initialPath) {
            val parentDirectory = File(current).parentFile
            if (parentDirectory != null) {
                loadFiles(parentDirectory.absolutePath)
                return true // Se manejó el 'back press'
            }
        }
        return false // No se manejó, dejar que la Activity lo haga
    }
}