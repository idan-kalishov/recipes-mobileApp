package com.example.recipes_mobile

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipes_mobile.databinding.FragmentSignupBinding
import com.example.recipes_mobile.utils.CircleTransform
import com.example.recipes_mobile.utils.ImagePickerUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class SignUpFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    private var selectedImageBitmap: Bitmap? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // Select an image using ImagePickerUtils
        binding.uploadProfileImageButton.setOnClickListener {
            ImagePickerUtils.pickImageFromGalleryOrCamera(this)
        }

        binding.backToLoginButton.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }

        // Sign up user
        binding.signupButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val name = binding.nameEditText.text.toString().trim()

            if (email.isBlank() || password.isBlank() || name.isBlank()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            binding.signupButton.isEnabled = false

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid!!
                        if (selectedImageBitmap != null) {
                            uploadProfileImage(userId, name, email)
                        } else {
                            saveUserDetails(userId, name, email, "")
                        }
                    } else {
                        binding.progressBar.visibility = View.GONE
                        binding.signupButton.isEnabled = true
                        Toast.makeText(requireContext(), "Sign-up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    // Handle image picker result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        ImagePickerUtils.handleImageResult(
            requestCode,
            resultCode,
            data,
            requireContext()
        ) { bitmap ->
            selectedImageBitmap = bitmap
            val imageUri = ImagePickerUtils.getImageUriFromBitmap(requireContext(), bitmap)

            Picasso.get()
                .load(imageUri)
                .transform(CircleTransform())
                .into(binding.profileImageView)
        }
    }

    // Upload image to Imgur
    private fun uploadProfileImage(userId: String, name: String, email: String) {
        ImgurUploader.uploadImage(
            selectedImageBitmap!!,
            onSuccess = { imageUrl ->
                saveUserDetails(userId, name, email, imageUrl)
            },
            onFailure = { error ->
                binding.progressBar.visibility = View.GONE
                binding.signupButton.isEnabled = true
                Toast.makeText(requireContext(), "Image upload failed: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Save user details
    private fun saveUserDetails(userId: String, name: String, email: String, profilePictureUrl: String) {
        val userProfile = mapOf(
            "userId" to userId,
            "name" to name,
            "email" to email,
            "profilePictureUrl" to profilePictureUrl
        )

        // Assuming you're using Firestore for user details storage
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .set(userProfile)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                binding.signupButton.isEnabled = true
                Toast.makeText(requireContext(), "Sign-up successful!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.signupButton.isEnabled = true
                Toast.makeText(requireContext(), "Failed to save user details: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
