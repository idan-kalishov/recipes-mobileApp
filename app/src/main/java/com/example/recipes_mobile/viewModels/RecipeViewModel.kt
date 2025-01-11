package com.example.recipes_mobile.viewModels

import android.util.Log
import androidx.lifecycle.*
import com.example.recipes_mobile.model.Recipe
import com.example.recipes_mobile.repositories.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    // LiveData for the list of recipes
    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> get() = _recipes

    // Initialize the ViewModel
    fun init() {
        syncRecipes() // Start real-time syncing
        fetchAllRecipes() // Load local data into LiveData
        checkForDeletedRecipes()
    }

    // Fetch all recipes from the repository
    private fun fetchAllRecipes() {
        viewModelScope.launch {
            repository.getAllRecipes()
                .flowOn(Dispatchers.IO)
                .catch { exception ->
                    // Handle exceptions (e.g., log or show error messages)
                    exception.printStackTrace()
                }
                .collect { recipes ->
                    _recipes.postValue(recipes) // Post updated list to LiveData
                }
        }
    }

    // Synchronize recipes with Firestore and local database
    private fun syncRecipes() {
        viewModelScope.launch {
            repository.syncRecipes(viewModelScope)
        }
    }

    // Filter recipes based on a search query
    fun filterRecipes(query: String) {
        viewModelScope.launch {
            repository.getAllRecipes()
                .map { recipes ->
                    recipes.filter { recipe ->
                        recipe.title.contains(query, ignoreCase = true)
                    }
                }
                .catch { exception ->
                    // Handle exceptions (e.g., log or show error messages)
                    exception.printStackTrace()
                }
                .collect { filteredRecipes ->
                    _recipes.postValue(filteredRecipes) // Post filtered recipes to LiveData
                }
        }
    }

    // Manually trigger a check for deleted recipes
    fun checkForDeletedRecipes() {
        viewModelScope.launch {
            repository.checkAndRemoveDeletedRecipes(viewModelScope)
        }
    }
    fun test1(){
        repository.test1();
    }

}

class RecipeViewModelFactory(private val repository: RecipeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
