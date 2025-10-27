package com.example.practica3

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentFileDao {
    // Inserta un archivo reciente. Si ya existe, actualiza su timestamp.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recentFile: RecentFile)

    // Obtiene los archivos recientes, limitados a los últimos 50, ordenados por fecha descendente.
    @Query("SELECT * FROM recent_files ORDER BY timestamp DESC LIMIT 50")
    fun getRecentFiles(): Flow<List<RecentFile>>

    // (Opcional) Función para borrar el historial
    @Query("DELETE FROM recent_files")
    suspend fun clearHistory()
}