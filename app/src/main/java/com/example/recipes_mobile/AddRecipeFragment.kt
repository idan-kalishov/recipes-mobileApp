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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddRecipeBinding.inflate(inflater, container, false)

        // Navigate back to Recipe Feed
        binding.fabHome.setOnClickListener {
            findNavController().navigate(R.id.recipeFeedFragment)
        }

        // Button to select an image
        binding.btnRecipe.setOnClickListener {
            ImagePickerUtils.pickImageFromGalleryOrCamera(this)
        }

        // Button to save recipe
        binding.btnSave.setOnClickListener {
            saveRecipe()
        }

        return binding.root
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

        if (title.isEmpty() || ingredients.isEmpty() || steps.isEmpty() || selectedImageBitmap == null) {
            Toast.makeText(requireContext(), "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show()
            return
        }

        val recipeId = UUID.randomUUID().toString()

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
                Toast.makeText(requireContext(), "Recipe saved successfully!", Toast.LENGTH_SHORT).show()
                clearFields()
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
