package com.example.recipes_mobile.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.recipes_mobile.model.MyRecipesDao
import com.example.recipes_mobile.model.Recipe

@Database(entities = [Recipe::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)

abstract class RecipesDatabase : RoomDatabase() {
    abstract fun myRecipesDao(): MyRecipesDao

    companion object {
        @Volatile
        private var INSTANCE: RecipesDatabase? = null

        fun getInstance(context: Context): RecipesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecipesDatabase::class.java,
                    "recipes_database"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
