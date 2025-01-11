package com.example.recipes_mobile.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.recipes_mobile.Classes.User

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val ingredients: String ="",
    val steps: String = "" ,
    val imageUrl: String = "",
    val userId: String = "",
    val lastUpdated: Long = 0,
    var user: User = User()
)



