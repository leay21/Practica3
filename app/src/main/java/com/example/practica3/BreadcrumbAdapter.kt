package com.example.practica3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.practica3.databinding.ItemBreadcrumbBinding
import java.io.File

class BreadcrumbAdapter(
    private val onBreadcrumbClicked: (File) -> Unit
) : RecyclerView.Adapter<BreadcrumbAdapter.BreadcrumbViewHolder>() {

    private val pathParts = mutableListOf<File>()

    fun setPath(newPath: List<File>) {
        pathParts.clear()
        pathParts.addAll(newPath)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BreadcrumbViewHolder {
        val binding = ItemBreadcrumbBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BreadcrumbViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BreadcrumbViewHolder, position: Int) {
        holder.bind(pathParts[position], position == pathParts.lastIndex)
    }

    override fun getItemCount(): Int = pathParts.size

    inner class BreadcrumbViewHolder(private val binding: ItemBreadcrumbBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            // Manejador de clics para cada ítem
            binding.breadcrumbName.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onBreadcrumbClicked(pathParts[adapterPosition])
                }
            }
        }

        fun bind(file: File, isLast: Boolean) {
            // Si es la raíz, mostramos "Almacenamiento" en lugar de "0"
            if (file.parentFile == null || file.parentFile.path == "/storage/emulated") {
                binding.breadcrumbName.text = "Almacenamiento"
            } else {
                binding.breadcrumbName.text = file.name
            }

            // Ocultamos el separador ">" si es el último ítem de la lista
            binding.breadcrumbSeparator.visibility = if (isLast) View.GONE else View.VISIBLE
        }
    }
}