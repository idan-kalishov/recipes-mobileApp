package com.example.recipes_mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.recipes_mobile.Database.RecipesDatabase
import com.example.recipes_mobile.databinding.FragmentRecipeDetailBinding
import com.example.recipes_mobile.model.Recipe
import com.example.recipes_mobile.repositories.RecipeRepository
import com.example.recipes_mobile.services.FirestoreService
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeDetailFragment() : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recipe: Recipe? = arguments?.getParcelable("recipe")
        recipe?.let {
            displayRecipeDetails(it)


            val currentUser = FirebaseAuth.getInstance().currentUser
            val btnOptions = view.findViewById<ImageView>(R.id.btnOptions)

            if (currentUser == null || currentUser.uid != recipe?.userId) {
                binding.btnOptions.visibility = View.GONE
            } else {
                btnOptions.visibility = View.VISIBLE

                btnOptions.setOnClickListener { button ->
                    val popup = PopupMenu(requireContext(), button)
                    popup.menuInflater.inflate(R.menu.recipe_options_menu, popup.menu)
                    popup.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.menu_edit -> {
                                val bundle = Bundle().apply { putParcelable("recipe", it) }
                                findNavController().navigate(
                                    R.id.action_recipeDetailFragment_to_addRecipeFragment, bundle
                                )
                                true
                            }

                            R.id.menu_delete -> {
                                // Call the function to delete the recipe
                                deleteRecipe(it)
                                true
                            }

                            else -> false
                        }
                    }
                    popup.show()
                }
            }
        }
    }

    private fun deleteRecipe(recipe: Recipe) {
        // Get your repository instance. Adjust this if you already have a repository stored somewhere.
        val recipeDao = RecipesDatabase.getInstance(requireContext()).myRecipesDao()
        val firestoreService = FirestoreService()
        val repository = RecipeRepository(recipeDao, firestoreService, requireContext())

        // Launch a coroutine to delete the recipe
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            repository.deleteRecipe(recipe.id, this)
            // Once deletion is done, switch to Main thread to show a Toast and navigate up.
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Recipe deleted", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    private fun displayRecipeDetails(recipe: Recipe) {
        // Load Image
        Picasso.get().load(recipe.imageUrl).into(binding.ivRecipeImage)
        // Set Title
        binding.tvRecipeTitle.text = recipe.title

        // Format Ingredients
        binding.tvIngredients.text = formatTextToList(recipe.ingredients)

        // Format Steps
        binding.tvSteps.text = formatTextToNumberedList(recipe.steps)
    }

    private fun formatTextToList(text: String): String {
        return text.split("\n").joinToString("\n") { "• $it" }
    }

    private fun formatTextToNumberedList(text: String): String {
        return text.split("\n").mapIndexed { index, step -> "${index + 1}. $step" }
            .joinToString("\n")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}