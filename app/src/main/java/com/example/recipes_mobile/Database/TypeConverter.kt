package com.example.recipes_mobile.Database

import androidx.room.TypeConverter
import com.example.recipes_mobile.Classes.User
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun fromUser(user: User): String {
        return Gson().toJson(user)
    }

    @TypeConverter
    fun toUser(userJson: String): User {
        return Gson().fromJson(userJson, User::class.java)
    }
}
