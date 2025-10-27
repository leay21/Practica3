package com.example.practica3

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteFile(
    @PrimaryKey val path: String, // La ruta del archivo será la clave única
    val name: String, // Guardamos el nombre para mostrarlo fácilmente
    val timestamp: Long = System.currentTimeMillis() // Fecha en que se añadió
)