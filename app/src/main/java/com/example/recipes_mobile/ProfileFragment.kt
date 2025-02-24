package com.example.recipes_mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recipes_mobile.Database.RecipesDatabase
import com.example.recipes_mobile.databinding.FragmentProfileBinding
import com.example.recipes_mobile.model.Recipe
import com.example.recipes_mobile.utils.CircleTransform
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var recipesAdapter: RecipesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.fabHome.setOnClickListener {
            findNavController().navigate(R.id.recipeFeedFragment)
        }


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

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "No Name"
                    val profilePictureUrl = document.getString("profilePictureUrl") ?: ""
                    binding.tvUserName.text = name

                    if (profilePictureUrl.isNotEmpty()) {
                        Picasso.get()
                            .load(profilePictureUrl)
                            .transform(CircleTransform())
                            .error(R.drawable.ic_broken_image)
                            .into(binding.ivProfileImage)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to load profile: ${exception.message}", Toast.LENGTH_SHORT).show()
            }

        recipesAdapter = RecipesAdapter { recipe: Recipe ->
            // בעת לחיצה על מתכון – נווט למסך פרטי המתכון
            val bundle = Bundle().apply { putParcelable("recipe", recipe) }
            findNavController().navigate(R.id.recipeDetailFragment, bundle)
        }
        binding.recyclerViewRecipes.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewRecipes.adapter = recipesAdapter

        val recipeDao = RecipesDatabase.getInstance(requireContext()).myRecipesDao()
        recipeDao.getByOwner(userId).observe(viewLifecycleOwner) { recipes ->
            recipesAdapter.submitList(recipes.toMutableList())
            binding.tvRecipesCount.text = "Your Recipes: ${recipes.size}"
        }

//        binding.btnEditProfile.setOnClickListener {
//            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
