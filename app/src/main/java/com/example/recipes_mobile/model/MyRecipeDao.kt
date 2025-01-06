package com.example.recipes_mobile.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MyRecipesDao {
    @Insert
    suspend fun insertRecipe(recipe: MyRecipe)

    @Query("SELECT * FROM my_recipes")
    fun getAllRecipes(): List<MyRecipe> // Temporarily remove Flow
}
