package com.example.recipes_mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.recipes_mobile.databinding.FragmentRecipeDetailBinding
import com.example.recipes_mobile.model.Recipe
import com.squareup.picasso.Picasso

class RecipeDetailFragment() : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recipe: Recipe? = arguments?.getParcelable("recipe")
        recipe?.let { displayRecipeDetails(it) }
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
        return text.split("\n").mapIndexed { index, step -> "${index + 1}. $step" }.joinToString("\n")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}