package com.example.recipes_mobile.model

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MyRecipesDao {

    @Query("SELECT * FROM recipes")
    fun getAll(): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipes")
    suspend fun getAllSync(): List<Recipe>

    @Query("SELECT * FROM recipes")
    fun getAllFlow(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    fun getById(id: String): LiveData<Recipe>

    @Query("SELECT * FROM recipes WHERE userId = :userId")
    fun getByOwner(userId: String): LiveData<List<Recipe>>

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: Recipe)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<Recipe>)

    @Query("DELETE FROM recipes WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)
}
