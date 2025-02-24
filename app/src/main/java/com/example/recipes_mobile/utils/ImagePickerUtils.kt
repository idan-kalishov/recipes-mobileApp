package com.example.recipes_mobile.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FileOutputStream

object ImagePickerUtils {
    const val IMAGE_PICK_CAMERA_REQUEST = 1003

    fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
        val file = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()
        return Uri.fromFile(file)
    }

    fun saveImageToLocalFile(context: Context, bitmap: Bitmap): String {
        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)

        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }

        return file.absolutePath // Return the file path to save in Room
    }


    // Open gallery or camera picker
    fun pickImageFromGalleryOrCamera(fragment: Fragment) {
        // Intent to pick an image from the gallery
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        // Intent to capture an image using the camera
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Combine the two intents into a chooser
        val chooserIntent = Intent.createChooser(galleryIntent, "Select Image")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

        fragment.startActivityForResult(chooserIntent, IMAGE_PICK_CAMERA_REQUEST)
    }

    // Handle the image selection result
    fun handleImageResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        context: Context,
        onImagePicked: (Bitmap) -> Unit
    ) {
        if (requestCode == IMAGE_PICK_CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                val imageBitmap: Bitmap? = when {
                    data?.data != null -> {
                        // Image selected from gallery
                        val imageUri = data.data
                        MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                    }
                    data?.extras?.get("data") != null -> {
                        // Image captured from camera
                        data.extras?.get("data") as Bitmap
                    }
                    else -> null
                }

                imageBitmap?.let { onImagePicked(it) }
            } catch (e: Exception) {
                Log.e("ImagePickerUtils", "Error handling image result: ${e.message}", e)
            }
        }
    }
}
