package com.example.recipes_mobile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.recipes_mobile.databinding.ItemRecipeBinding
import com.example.recipes_mobile.model.Recipe
import com.squareup.picasso.Picasso

class RecipesAdapter(private val onRecipeClick: (Recipe) -> Unit) : RecyclerView.Adapter<RecipesAdapter.RecipeViewHolder>() {

    private val allRecipes = mutableListOf<Recipe>() // Original list of recipes
    private val filteredRecipes = mutableListOf<Recipe>() // Filtered list

    // Updates the list of recipes
    fun submitList(newRecipes: List<Recipe>) {
        allRecipes.clear()
        allRecipes.addAll(newRecipes)
        filteredRecipes.clear()
        filteredRecipes.addAll(newRecipes)
        notifyDataSetChanged()
    }

    // Filters recipes based on the query
    fun filter(query: String) {
        val lowerCaseQuery = query.lowercase()
        filteredRecipes.clear()
        filteredRecipes.addAll(
            allRecipes.filter { recipe ->
                recipe.title.lowercase().contains(lowerCaseQuery)
            }
        )
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(filteredRecipes[position], onRecipeClick)
    }

    override fun getItemCount(): Int = filteredRecipes.size

    class RecipeViewHolder(private val binding: ItemRecipeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(recipe: Recipe, onRecipeClick: (Recipe) -> Unit) {
            // Set the recipe title
            binding.titleTextView.text = recipe.title

            // Load the image using Picasso
            if (!recipe.imageUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(recipe.imageUrl)
                    .error(R.drawable.ic_broken_image)             // Fallback image on error
                    .into(binding.imageView)
            } else {
                // Default image if no URL is provided
                binding.imageView.setImageResource(R.drawable.ic_broken_image)
            }

            // Handle click on the recipe item
            binding.root.setOnClickListener { onRecipeClick(recipe) }
        }
    }
}
