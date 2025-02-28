package com.example.recipes_mobile

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipes_mobile.databinding.FragmentEditProfileBinding
import com.example.recipes_mobile.utils.ImagePickerUtils
import com.example.recipes_mobile.utils.CircleTransform
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private var selectedImageBitmap: Bitmap? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            findNavController().navigate(R.id.loginFragment)
            return
        }
        val userId = currentUser.uid

        // Fetch current user details from Firestore
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    binding.etEditName.setText(document.getString("name"))
                    val profilePictureUrl = document.getString("profilePictureUrl") ?: ""
                    if (profilePictureUrl.isNotEmpty()) {
                        Picasso.get()
                            .load(profilePictureUrl)
                            .transform(CircleTransform())
                            .error(R.drawable.ic_broken_image)
                            .into(binding.ivEditProfileImage)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error loading profile: ${exception.message}", Toast.LENGTH_SHORT).show()
            }

        // Handle image change
        binding.btnChangeImage.setOnClickListener {
            ImagePickerUtils.pickImageFromGalleryOrCamera(this)
        }

        // Handle save button click
        binding.btnSaveProfile.setOnClickListener {
            val newName = binding.etEditName.text.toString().trim()
            if (newName.isEmpty()) {
                Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedImageBitmap != null) {
                // Upload new image if selected
                ImgurUploader.uploadImage(
                    selectedImageBitmap!!,
                    onSuccess = { imageUrl ->
                        updateUserProfile(userId, newName, imageUrl)
                    },
                    onFailure = { error ->
                        Toast.makeText(requireContext(), "Image upload failed: $error", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                updateUserProfile(userId, newName, null)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })
    }

    private fun updateUserProfile(userId: String, name: String, profileImageUrl: String?) {
        val updateData = mutableMapOf<String, Any>()
        updateData["name"] = name
        if (profileImageUrl != null) {
            updateData["profilePictureUrl"] = profileImageUrl
        }
        firestore.collection("users").document(userId).update(updateData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to update profile: ${exception.message}", Toast.LENGTH_SHORT).show()
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
            Picasso.get()
                .load(ImagePickerUtils.getImageUriFromBitmap(requireContext(), bitmap))
                .transform(CircleTransform())
                .into(binding.ivEditProfileImage)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
