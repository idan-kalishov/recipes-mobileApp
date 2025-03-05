package com.example.recipes_mobile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.recipes_mobile.databinding.FragmentAddRecipeBinding
import com.example.recipes_mobile.model.Recipe
import com.example.recipes_mobile.utils.ImagePickerUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class AddRecipeFragment : Fragment() {

    private lateinit var binding: FragmentAddRecipeBinding
    private var selectedImageBitmap: Bitmap? = null
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Flag to indicate if we are in edit mode and the existing recipe object
    private var isEditMode = false
    private var existingRecipe: Recipe? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if a Recipe argument was passed => edit mode
        existingRecipe = arguments?.getParcelable("recipe")
        isEditMode = existingRecipe != null

        if (isEditMode) {
            // Fill input fields with existing recipe data
            binding.etUploadTitle.setText(existingRecipe?.title)
            binding.etIngredients.setText(existingRecipe?.ingredients)
            binding.etSteps.setText(existingRecipe?.steps)
            val imageUrl = existingRecipe?.imageUrl ?: ""
            if (imageUrl.isNotEmpty()) {
                // Load the existing image using Picasso
                com.squareup.picasso.Picasso.get()
                    .load(imageUrl)
                    .error(R.drawable.ic_broken_image)
                    .into(binding.imageView)
            }
        }

        // Navigate back to Recipe Feed
        binding.fabHome.setOnClickListener {
            findNavController().navigate(R.id.recipeFeedFragment)
        }

        // Button to select an image from gallery or camera
        binding.btnRecipe.setOnClickListener {
            ImagePickerUtils.pickImageFromGalleryOrCamera(this)
        }

        // Button to save (or update) recipe
        binding.btnSave.setOnClickListener {
            saveRecipe()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ImagePickerUtils.handleImageResult(
            requestCode,
            resultCode,
            data,
            requireContext()
        ) { bitmap ->
            selectedImageBitmap = bitmap
            binding.imageView.setImageBitmap(bitmap)
        }
    }

    private fun saveRecipe() {
        val title = binding.etUploadTitle.text.toString().trim()
        val ingredients = binding.etIngredients.text.toString().trim()
        val steps = binding.etSteps.text.toString().trim()

        if (title.isEmpty() || ingredients.isEmpty() || steps.isEmpty() || (!isEditMode && selectedImageBitmap == null)) {
            Toast.makeText(requireContext(), "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show()
            return
        }

        val recipeId = if (isEditMode) existingRecipe!!.id else UUID.randomUUID().toString()

        if (selectedImageBitmap != null) {
            lifecycleScope.launch {
                try {
                    ImgurUploader.uploadImage(
                        selectedImageBitmap!!,
                        onSuccess = { imgurUrl ->
                            lifecycleScope.launch {
                                saveRecipeToFirestore(recipeId, userId, title, ingredients, steps, imgurUrl)
                            }
                        },
                        onFailure = { error ->
                            lifecycleScope.launch {
                                Toast.makeText(requireContext(), "Image upload failed: $error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error saving recipe: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            val imageUrl = existingRecipe?.imageUrl ?: ""
            saveRecipeToFirestore(recipeId, userId, title, ingredients, steps, imageUrl)
        }
    }

    private fun saveRecipeToFirestore(
        recipeId: String,
        userId: String,
        title: String,
        ingredients: String,
        steps: String,
        imageUrl: String
    ) {
        val recipe = hashMapOf(
            "id" to recipeId,
            "title" to title,
            "ingredients" to ingredients,
            "steps" to steps,
            "imageUrl" to imageUrl,
            "userId" to userId,
            "lastUpdated" to System.currentTimeMillis()
        )

        firestore.collection("recipes")
            .document(recipeId)
            .set(recipe)
            .addOnSuccessListener {
                val msg = if (isEditMode) "Recipe updated successfully!" else "Recipe saved successfully!"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                clearFields()
                findNavController().navigate(R.id.recipeFeedFragment)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save recipe: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        binding.etUploadTitle.text?.clear()
        binding.etIngredients.text?.clear()
        binding.etSteps.text?.clear()
        binding.imageView.setImageBitmap(null)
        selectedImageBitmap = null
    }
}
