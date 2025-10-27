package com.example.practica3

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.practica3.databinding.ItemFileBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FileAdapter(
    private val onFileClicked: (File) -> Unit,
    private val onFileLongClicked: (File) -> Unit) :
    ListAdapter<File, FileAdapter.FileViewHolder>(FileDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }
    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = getItem(position)
        holder.bind(file)
        // Clic normal
        holder.itemView.setOnClickListener { onFileClicked(file) }
        // Clic largo
        holder.itemView.setOnLongClickListener {
            onFileLongClicked(file)
            true // Devuelve true para indicar que el evento fue consumido
        }
    }

    class FileViewHolder(private val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(file: File) {
            binding.textFileName.text = file.name

            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val lastModified = sdf.format(Date(file.lastModified()))

            if (file.isDirectory) {
                val numItems = file.listFiles()?.size ?: 0
                binding.textFileDetails.text = "$numItems elementos | $lastModified"
                binding.iconFile.setImageResource(R.drawable.baseline_archive_24) // Reemplaza con tu icono
            } else {
                binding.textFileDetails.text = "${formatFileSize(file.length())} | $lastModified"
                // Lógica de iconos y miniaturas
                when (file.extension.lowercase()) {
                    "jpg", "jpeg", "png", "gif" -> {
                        Glide.with(itemView.context)
                            .load(file)
                            .placeholder(R.drawable.outline_animated_images_24) // Reemplaza con tu icono
                            .into(binding.iconFile)
                    }
                    else -> {
                        binding.iconFile.setImageResource(R.drawable.outline_article_24) // Reemplaza con tu icono
                    }
                }
            }
        }

        private fun formatFileSize(size: Long): String {
            if (size <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
            return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
        }
    }
}

class FileDiffCallback : DiffUtil.ItemCallback<File>() {
    override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
        return oldItem.path == newItem.path
    }

    override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
        return oldItem.lastModified() == newItem.lastModified() && oldItem.length() == newItem.length()
    }
}