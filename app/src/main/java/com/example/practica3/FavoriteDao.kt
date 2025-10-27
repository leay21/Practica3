package com.example.practica3

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    // Inserta un favorito. Si ya existe, lo reemplaza.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favoriteFile: FavoriteFile)

    // Elimina un favorito.
    @Delete
    suspend fun delete(favoriteFile: FavoriteFile)

    // Obtiene todos los favoritos ordenados por nombre. Devuelve un Flow para observar cambios.
    @Query("SELECT * FROM favorites ORDER BY name ASC")
    fun getAllFavorites(): Flow<List<FavoriteFile>>

    // Comprueba si un archivo específico ya es favorito.
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE path = :path LIMIT 1)")
    suspend fun isFavorite(path: String): Boolean
}