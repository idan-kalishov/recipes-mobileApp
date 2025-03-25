package com.example.recipes_mobile.network

import com.example.recipes_mobile.model.RecipeRequest
import com.example.recipes_mobile.model.RecipeResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Replace with your Google AI API Key
const val API_KEY = "AIzaSyCFw74NkowBPD4H84RCMWVIAHxAmsvgWq8"

interface RecipeApiService {
    @POST("v1beta/models/gemini-2.0-flash:generateContent?key=$API_KEY")
    suspend fun getRecipe(@Body request: RecipeRequest): RecipeResponse
}

// Retrofit instance
object RecipeApi {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    val instance: RecipeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecipeApiService::class.java)
    }
}
