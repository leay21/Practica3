package com.example.practica3

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.example.practica3.databinding.ItemFileBinding
import com.example.practica3.databinding.ItemFileGridBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FileAdapter(
    private val onFileClicked: (File) -> Unit,
    private val onFileLongClicked: (File) -> Unit
) : ListAdapter<File, FileAdapter.BaseViewHolder>(FileDiffCallback()) {

    // Variable para saber qué vista mostrar
    private var currentViewMode = SettingsManager.VIEW_MODE_LIST

    // Constantes para los tipos de vista
    companion object {
        private const val VIEW_TYPE_LIST = 1
        private const val VIEW_TYPE_GRID = 2
    }

    fun setViewMode(viewMode: String) {
        currentViewMode = viewMode
    }

    override fun getItemViewType(position: Int): Int {
        return if (currentViewMode == SettingsManager.VIEW_MODE_LIST) {
            VIEW_TYPE_LIST
        } else {
            VIEW_TYPE_GRID
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return if (viewType == VIEW_TYPE_LIST) {
            // Inflar el layout de lista
            val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ListViewHolder(binding)
        } else {
            // Inflar el layout de cuadrícula
            val binding = ItemFileGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            GridViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val file = getItem(position)
        holder.bind(file)
        holder.itemView.setOnClickListener { onFileClicked(file) }
        holder.itemView.setOnLongClickListener {
            onFileLongClicked(file)
            true
        }
    }

    // --- Definimos los ViewHolders ---

    // ViewHolder base abstracto
    abstract class BaseViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(file: File)

        // Función de ayuda para formatear tamaño (la movemos aquí)
        fun formatFileSize(size: Long): String {
            if (size <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
            return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
        }
    }

    // ViewHolder para la VISTA DE LISTA (tu código original)
    inner class ListViewHolder(private val binding: ItemFileBinding) : BaseViewHolder(binding) {
        override fun bind(file: File) {
            binding.textFileName.text = file.name
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val lastModified = sdf.format(Date(file.lastModified()))

            if (file.isDirectory) {
                val numItems = file.listFiles()?.size ?: 0
                binding.textFileDetails.text = "$numItems elementos | $lastModified"
                binding.iconFile.setImageResource(R.drawable.baseline_archive_24)
            } else {
                binding.textFileDetails.text = "${formatFileSize(file.length())} | $lastModified"
                bindIcon(file)
            }
        }

        private fun bindIcon(file: File) {
            when (file.extension.lowercase()) {
                "jpg", "jpeg", "png", "gif" -> {
                    Glide.with(itemView.context).load(file)
                        .placeholder(R.drawable.outline_animated_images_24)
                        .into(binding.iconFile)
                }
                else -> {
                    binding.iconFile.setImageResource(R.drawable.outline_article_24)
                }
            }
        }
    }

    // ViewHolder para la VISTA DE CUADRÍCULA (nuevo)
    inner class GridViewHolder(private val binding: ItemFileGridBinding) : BaseViewHolder(binding) {
        override fun bind(file: File) {
            binding.textFileName.text = file.name

            if (file.isDirectory) {
                binding.iconFile.setImageResource(R.drawable.baseline_archive_24)
            } else {
                bindIcon(file)
            }
        }

        private fun bindIcon(file: File) {
            when (file.extension.lowercase()) {
                "jpg", "jpeg", "png", "gif" -> {
                    Glide.with(itemView.context).load(file)
                        .placeholder(R.drawable.outline_animated_images_24)
                        .into(binding.iconFile)
                }
                else -> {
                    binding.iconFile.setImageResource(R.drawable.outline_article_24)
                }
            }
        }
    }
}

// DiffCallback se queda igual
class FileDiffCallback : DiffUtil.ItemCallback<File>() {
    override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
        return oldItem.path == newItem.path
    }
    override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
        return oldItem.lastModified() == newItem.lastModified() && oldItem.length() == newItem.length()
    }
}