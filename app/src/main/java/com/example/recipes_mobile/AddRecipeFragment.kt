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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.recipes_mobile.databinding.FragmentAddRecipeBinding
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

        // Button to select image
        binding.btnRecipe.setOnClickListener {
            pickImageFromGalleryOrCamera()
        }

        // Button to save recipe
        binding.btnSave.setOnClickListener {
            saveRecipe()
        }

        return binding.root
    }

    private fun pickImageFromGalleryOrCamera() {
        // Intent to pick an image from the gallery
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        // Intent to capture an image using the camera
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Combine the two intents into a chooser
        val chooserIntent = Intent.createChooser(galleryIntent, "Select Image")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

        startActivityForResult(chooserIntent, IMAGE_PICK_CAMERA_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            val imageBitmap: Bitmap? = when {
                data?.data != null -> {
                    // Image selected from gallery
                    val imageUri = data.data
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
                }
                data?.extras?.get("data") != null -> {
                    // Image captured from camera
                    data.extras?.get("data") as Bitmap
                }
                else -> null
            }

            imageBitmap?.let {
                selectedImageBitmap = it
                binding.imageView.setImageBitmap(it)
            }
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

        // Convert the image to Base64
        val imageBase64 = encodeImageToBase64(selectedImageBitmap!!)

        // Create a recipe object
        val recipe = hashMapOf(
            "title" to title,
            "ingredients" to ingredients,
            "steps" to steps,
            "imageBase64" to imageBase64
        )

        // Save the recipe to Firestore
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

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
    }

    private fun clearFields() {
        binding.etUploadTitle.text?.clear()
        binding.etIngredients.text?.clear()
        binding.etSteps.text?.clear()
        binding.imageView.setImageBitmap(null)
        selectedImageBitmap = null
    }

    companion object {
        private const val IMAGE_PICK_CAMERA_REQUEST = 1003
    }
}
