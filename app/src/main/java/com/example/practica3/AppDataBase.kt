package com.example.practica3

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavoriteFile::class, RecentFile::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Métodos abstractos para obtener los DAOs
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentFileDao(): RecentFileDao

    // Singleton para asegurar una única instancia de la base de datos
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "file_manager_database" // Nombre del archivo de la base de datos
                )
                    // .fallbackToDestructiveMigration() // Usar si cambias la estructura y no quieres migraciones
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}