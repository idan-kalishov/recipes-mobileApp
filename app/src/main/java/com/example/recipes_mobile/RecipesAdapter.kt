package com.example.recipes_mobile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.recipes_mobile.databinding.ItemRecipeBinding
import com.example.recipes_mobile.model.Recipe
import com.squareup.picasso.Picasso

class RecipesAdapter(private val onRecipeClick: (Recipe) -> Unit) :
    ListAdapter<Recipe, RecipesAdapter.RecipeViewHolder>(DiffCallback()) {

    private val allRecipes = mutableListOf<Recipe>() // Stores all recipes
    private val filteredRecipes = mutableListOf<Recipe>() // Stores filtered recipes

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = filteredRecipes[position]
        holder.bind(recipe, onRecipeClick)
    }

    override fun getItemCount(): Int = filteredRecipes.size

    override fun submitList(list: MutableList<Recipe>?) {
        if (list != null) {
            allRecipes.clear()
            allRecipes.addAll(list) // Update original list

            filteredRecipes.clear()
            filteredRecipes.addAll(list) // Update filtered list
            notifyDataSetChanged()
        }
    }

    fun filter(query: String) {
        val lowerCaseQuery = query.lowercase()
        filteredRecipes.clear()
        if (query.isEmpty()) {
            // Reset filtered list to original list
            filteredRecipes.addAll(allRecipes)
        } else {
            // Filter recipes by title
            filteredRecipes.addAll(
                allRecipes.filter { recipe ->
                    recipe.title.lowercase().contains(lowerCaseQuery)
                }
            )
        }
        notifyDataSetChanged()
    }

    class RecipeViewHolder(private val binding: ItemRecipeBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe, onRecipeClick: (Recipe) -> Unit) {
            binding.titleTextView.text = recipe.title

            if (recipe.imageUrl.startsWith("http")) {
                // Load from Firebase or remote URL
                Picasso.get()
                    .load(recipe.imageUrl)
                    .error(R.drawable.ic_broken_image)
                    .into(binding.imageView)
            } else {
                // Load from local path
                Picasso.get()
                    .load("file://${recipe.imageUrl}")
                    .error(R.drawable.ic_broken_image)
                    .into(binding.imageView)
            }

            binding.root.setOnClickListener { onRecipeClick(recipe) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem == newItem
        }
    }
}
