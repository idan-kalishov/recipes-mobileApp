    package com.example.recipes_mobile.repositories

    import android.content.Context
    import android.util.Log
    import com.example.recipes_mobile.data.local.LastUpdateManager
    import com.example.recipes_mobile.model.MyRecipesDao
    import com.example.recipes_mobile.model.Recipe
    import com.example.recipes_mobile.services.FirestoreService
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.flow.Flow
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.withContext

    class RecipeRepository(
        private val recipeDao: MyRecipesDao,
        private val firestoreService: FirestoreService,
        private val context: Context
    ) {

        // Get all recipes from Room as a Flow
        fun getAllRecipes(): Flow<List<Recipe>> = recipeDao.getAllFlow()

        // Sync recipes with Firestore
        fun syncRecipes(scope: CoroutineScope) {
            val lastUpdated = LastUpdateManager.getLastUpdated(context)

            // Fetch incremental updates from Firestore
            scope.launch(Dispatchers.IO) {
                firestoreService.fetchAllRecipes(
                    lastUpdateTime = lastUpdated,
                    coroutineScope = scope,
                    onSuccess = { updatedRecipes ->
                        if (updatedRecipes.isNotEmpty()) {
                            scope.launch(Dispatchers.IO) {
                                recipeDao.insertAll(updatedRecipes)
                                LastUpdateManager.setLastUpdated(
                                    context,
                                    updatedRecipes.maxOf { it.lastUpdated }
                                )
                            }
                        }
                    },
                    onFailure = { exception ->
                        Log.e("RecipeRepository", "Error fetching recipes", exception)
                    }
                )
            }

            // Listen for real-time changes
            firestoreService.listenForUpdates(
                lastUpdateTime = lastUpdated,
                onRecipeAddedOrUpdated = { recipe ->
                    scope.launch(Dispatchers.IO) {
                        recipeDao.insert(recipe)
                        Log.d("test insert", "this what's inside the room: ${recipeDao.getAllSync()}")
                        LastUpdateManager.setLastUpdated(context, recipe.lastUpdated)
                    }
                },
                onRecipeDeleted = { recipeId ->
                    scope.launch(Dispatchers.IO) {
                        Log.d("test","deleted recipe $recipeId!")
                        recipeDao.deleteById(recipeId)
                    }
                },
                onFailure = { exception ->
                    Log.e("RecipeRepository", "Error listening for real-time updates", exception)
                }
            )
        }

        // Check and remove deleted recipes
        fun checkAndRemoveDeletedRecipes(scope: CoroutineScope) {
            scope.launch(Dispatchers.IO) {
                val allLocalRecipes = withContext(Dispatchers.IO) { recipeDao.getAllSync() }
                firestoreService.fetchDeletedRecipes(
                    recipeIds = allLocalRecipes.map { it.id },
                    onSuccess = { deletedIds ->
                        scope.launch(Dispatchers.IO) {
                            recipeDao.deleteByIds(deletedIds)
                        }
                    },
                    onFailure = { exception ->
                        Log.e("RecipeRepository", "Error during deletion check", exception)
                    }
                )
            }
        }

        fun deleteRecipe(recipeId: String, scope: CoroutineScope) {
            scope.launch(Dispatchers.IO) {
                firestoreService.deleteRecipe(
                    recipeId,
                    onSuccess = {
                        // Launch a new coroutine inside the callback to call the suspend function
                        scope.launch {
                            recipeDao.deleteById(recipeId)
                        }
                    },
                    onFailure = { exception ->
                        Log.e("RecipeRepository", "Error deleting recipe", exception)
                    }
                )
            }
        }
    }
