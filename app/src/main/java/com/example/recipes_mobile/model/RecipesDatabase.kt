package com.example.recipes_mobile.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MyRecipe::class], version = 1, exportSchema = false)
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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
