package com.example.practica3

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_files")
data class RecentFile(
    @PrimaryKey val path: String, // La ruta del archivo
    val name: String, // Nombre del archivo
    val timestamp: Long = System.currentTimeMillis() // Fecha en que se abrió (se actualizará)
)