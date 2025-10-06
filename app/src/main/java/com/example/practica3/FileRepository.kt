import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileRepository {

    /**
     * Obtiene la lista de archivos y carpetas de una ruta específica.
     * Esta función es 'suspend' para poder ser llamada desde una corrutina
     * y ejecutarse en un hilo de fondo (Dispatchers.IO) sin bloquear la UI.
     */
    suspend fun getFiles(path: String): List<File> = withContext(Dispatchers.IO) {
        val directory = File(path)
        val filesList = directory.listFiles()?.toList() ?: emptyList()

        // Devuelve la lista ordenada: carpetas primero, luego archivos, ambos alfabéticamente
        filesList.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }
}