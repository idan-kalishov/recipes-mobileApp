package com.example.recipes_mobile.services

import android.util.Log
import com.example.recipes_mobile.Classes.User
import com.example.recipes_mobile.model.Recipe
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirestoreService(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    // Listen for updates in Firestore and update recipes in real time
    fun listenForUpdates(
        lastUpdateTime: Long,
        onRecipeAddedOrUpdated: (Recipe) -> Unit,
        onRecipeDeleted: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            db.collection(COLLECTION_RECIPES)
                .whereGreaterThan(FIELD_LAST_UPDATED, lastUpdateTime)
                .addSnapshotListener { snapshot, exception ->
                    Log.d("test", "ok this attached idkL: $snapshot")
                    if (exception != null) {
                        Log.e("FirestoreService", "Error listening for updates", exception)
                        onFailure(exception)
                        return@addSnapshotListener
                    }

                    snapshot?.documentChanges?.forEach { change ->
                        val recipeId = change.document.id
                        when (change.type) {
                            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                val recipe = change.document.toObject(Recipe::class.java)
                                CoroutineScope(Dispatchers.IO).launch {
                                    recipe.user = fetchUserById(recipe.userId) ?: User()
                                    onRecipeAddedOrUpdated(recipe)
                                }
                            }
                            DocumentChange.Type.REMOVED -> {
                                onRecipeDeleted(recipeId)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error setting up real-time listener", e)
            onFailure(e)
        }
    }

    // Fetch all recipes since the last updated time
    fun fetchAllRecipes(
        lastUpdateTime: Long,
        coroutineScope: CoroutineScope,
        onSuccess: (List<Recipe>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        coroutineScope.launch {
            try {
                val querySnapshot = db.collection(COLLECTION_RECIPES)
                    .whereGreaterThan(FIELD_LAST_UPDATED, lastUpdateTime)
                    .get()
                    .await()

                Log.d("test", "we got this: $querySnapshot")

                val recipes = querySnapshot.documents.map { document ->
                    val recipe = document.toObject(Recipe::class.java)!!
                    recipe.user = fetchUserById(recipe.userId) ?: User()
                    recipe
                }

                onSuccess(recipes)
            } catch (e: Exception) {
                Log.e("FirestoreService", "Error fetching recipes", e)
                onFailure(e)
            }
        }
    }

    // Fetch user by ID
    private suspend fun fetchUserById(userId: String): User? {
        if (userId.isBlank()) {
            Log.e("FirestoreService", "Error fetching user: userId is blank or invalid")
            return null
        }

        return try {
            val documentSnapshot = db.collection(COLLECTION_USERS).document(userId).get().await()
            documentSnapshot.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error fetching user $userId", e)
            null
        }
    }

    // Check for deleted recipes
    fun fetchDeletedRecipes(
        recipeIds: List<String>,
        onSuccess: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val fetchTasks = recipeIds.map { recipeId ->
                db.collection(COLLECTION_RECIPES).document(recipeId).get()
            }
            Tasks.whenAllSuccess<DocumentSnapshot>(fetchTasks)
                .addOnSuccessListener { snapshots ->
                    val deletedRecipeIds = snapshots.mapIndexedNotNull { index, snapshot ->
                        if (!snapshot.exists()) recipeIds[index] else null
                    }
                    onSuccess(deletedRecipeIds)
                }
                .addOnFailureListener { exception ->
                    Log.e("FirestoreService", "Error checking for deleted recipes", exception)
                    onFailure(exception)
                }
        } catch (exception: Exception) {
            Log.e("FirestoreService", "Unexpected error during deletion check", exception)
            onFailure(exception)
        }
    }

    companion object {
        const val COLLECTION_RECIPES = "recipes"
        const val COLLECTION_USERS = "users"
        const val FIELD_LAST_UPDATED = "lastUpdated"
    }
}
