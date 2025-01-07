package com.example.recipes_mobile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.recipes_mobile.databinding.FragmentAddRecipeBinding
import com.example.recipes_mobile.utils.ImagePickerUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class AddRecipeFragment : Fragment() {

    private lateinit var binding: FragmentAddRecipeBinding
    private var selectedImageBitmap: Bitmap? = null
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddRecipeBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()

        // Navigate back to Recipe Feed
        binding.fabHome.setOnClickListener {
            findNavController().navigate(R.id.recipeFeedFragment)
        }

        // Button to select an image
        binding.btnRecipe.setOnClickListener {
            ImagePickerUtils.pickImageFromGalleryOrCamera(this) // Use utility method
        }

        // Button to save recipe
        binding.btnSave.setOnClickListener {
            saveRecipe()
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Use ImagePickerUtils to handle the result
        ImagePickerUtils.handleImageResult(
            requestCode,
            resultCode,
            data,
            requireContext()
        ) { bitmap ->
            selectedImageBitmap = bitmap
            binding.imageView.setImageBitmap(bitmap) // Display the selected image
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

        // Upload image to Imgur
        ImgurUploader.uploadImage(
            selectedImageBitmap!!,
            onSuccess = { imageUrl ->
                // Save recipe with image URL to Firestore
                saveRecipeToFirestore(title, ingredients, steps, imageUrl)
            },
            onFailure = { error ->
                Toast.makeText(requireContext(), "Image upload failed: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun saveRecipeToFirestore(title: String, ingredients: String, steps: String, imageUrl: String) {
        val recipe = hashMapOf(
            "title" to title,
            "ingredients" to ingredients,
            "steps" to steps,
            "imageUrl" to imageUrl
        )

        firestore.collection("recipes")
            .add(recipe)
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
