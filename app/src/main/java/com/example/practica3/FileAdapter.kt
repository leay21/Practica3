import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FileAdapter(
    private val context: Context,
    private val files: List<File>,
    private val onFileClickListener: (File) -> Unit // Lambda para manejar clics
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    // Define las vistas de una fila
    class FileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileName: TextView = view.findViewById(R.id.tv_file_name)
        val fileDetails: TextView = view.findViewById(R.id.tv_file_details)
        val fileIcon: ImageView = view.findViewById(R.id.iv_icon)
    }

    // Crea una nueva vista (fila)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_file, parent, false)
        return FileViewHolder(view)
    }

    // Retorna la cantidad de elementos en la lista
    override fun getItemCount(): Int = files.size

    // Conecta los datos con la vista
    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]

        // Asigna el nombre del archivo
        holder.fileName.text = file.name

        // Asigna el ícono dependiendo del tipo de archivo
        if (file.isDirectory) {
            holder.fileIcon.setImageResource(R.drawable.ic_folder) // Necesitas este ícono
        } else {
            // Aquí puedes añadir más lógica para diferentes tipos de archivo (imagen, texto, etc.)
            holder.fileIcon.setImageResource(R.drawable.ic_file_generic)
        }

        // Formatea y muestra los detalles (tamaño y fecha)
        val date = Date(file.lastModified())
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val formattedDate = format.format(date)
        val size = if (file.isDirectory) "${file.listFiles()?.size ?: 0} elementos" else formatSize(file.length())
        holder.fileDetails.text = "$size - $formattedDate"

        // Configura el listener para el clic
        holder.itemView.setOnClickListener {
            onFileClickListener(file)
        }
    }

    // Función de ayuda para formatear el tamaño del archivo
    private fun formatSize(size: Long): String {
        if (size < 1024) return "$size B"
        val z = (63 - java.lang.Long.numberOfLeadingZeros(size)) / 10
        return String.format(Locale.getDefault(), "%.1f %sB", size.toDouble() / (1L shl z * 10), " KMGTPE"[z])
    }
}