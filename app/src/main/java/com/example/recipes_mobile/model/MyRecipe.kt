package com.example.recipes_mobile.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_recipes")
data class MyRecipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val ingredients: String,
    val steps: String,
    val imageUrl: String
)
