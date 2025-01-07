package com.example.recipes_mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recipes_mobile.databinding.FragmentRecipeFeedBinding
import com.example.recipes_mobile.model.Recipe
import com.example.recipes_mobile.utils.MenuUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecipeFeedFragment : Fragment() {

    private lateinit var binding: FragmentRecipeFeedBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val recipesAdapter = RecipesAdapter { recipe ->
        // Handle click on recipe title (for future detail view navigation)
        Toast.makeText(requireContext(), "Clicked: ${recipe.title}", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecipeFeedBinding.inflate(inflater, container, false)


        //calc how many items can fit the screen
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels / displayMetrics.density
        val itemWidth = 150
        val spanCount = (screenWidth / itemWidth).toInt().coerceAtLeast(1)

        // Setup RecyclerView
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), spanCount)
            adapter = recipesAdapter
        }

        // Fetch and display recipes
        fetchRecipes()

        // Setup SearchView
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterRecipes(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterRecipes(newText.orEmpty())
                return true
            }
        })

        binding.menuButton.setOnClickListener {
            setupMenu(it)
        }


        return binding.root
    }

    private fun setupMenu(view: View) {
        MenuUtils.showPopupMenu(
            context = requireContext(),
            view = view,
            navController = findNavController(),
            auth = FirebaseAuth.getInstance()
        )
    }

    private fun fetchRecipes() {
        firestore.collection("recipes")
            .get()
            .addOnSuccessListener { documents ->
                val recipes = documents.mapNotNull { it.toObject(Recipe::class.java) }
                recipesAdapter.submitList(recipes)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to fetch recipes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterRecipes(query: String) {
        recipesAdapter.filter(query)
    }
}
