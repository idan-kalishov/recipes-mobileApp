package com.example.recipes_mobile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.recipes_mobile.Database.RecipesDatabase
import com.example.recipes_mobile.databinding.FragmentRecipeFeedBinding
import com.example.recipes_mobile.model.MyRecipesDao
import com.example.recipes_mobile.repositories.RecipeRepository
import com.example.recipes_mobile.services.FirestoreService
import com.example.recipes_mobile.utils.MenuUtils
import com.example.recipes_mobile.viewModels.RecipeViewModel
import com.example.recipes_mobile.viewModels.RecipeViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecipeFeedFragment: Fragment() {

    private var _binding: FragmentRecipeFeedBinding? = null
    private val binding get() = _binding!!

    private lateinit var recipeViewModel: RecipeViewModel

    private val recipesAdapter = RecipesAdapter { recipe ->
        Toast.makeText(requireContext(), "Clicked: ${recipe.title}", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeFeedBinding.inflate(inflater, container, false)

        // Initialize ViewModel
        val context = requireContext()
        val recipeDao = RecipesDatabase.getInstance(context).myRecipesDao()
        val firestoreService = FirestoreService()
        val repository = RecipeRepository(recipeDao, firestoreService, context)
        val factory = RecipeViewModelFactory(repository)
        recipeViewModel = ViewModelProvider(this, factory).get(RecipeViewModel::class.java)

        // Initialize ViewModel logic
        recipeViewModel.init()

        // Setup UI and Observers
        setupUI()
        setupObservers()

        return binding.root
    }

    private fun setupUI() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels / displayMetrics.density
        val itemWidth = 150
        val spanCount = (screenWidth / itemWidth).toInt().coerceAtLeast(1)

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), spanCount)
            adapter = recipesAdapter
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                recipeViewModel.filterRecipes(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                recipeViewModel.filterRecipes(newText.orEmpty())
                return true
            }
        })

        binding.menuButton.setOnClickListener {
            setupMenu(it)
        }
    }

    private fun setupObservers() {
        // Observe the LiveData for the recipe list
        recipeViewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            Log.d("RecipeFeedFragment", "Received recipes: $recipes")
            CoroutineScope(Dispatchers.IO).launch {
                recipeViewModel.test1();

            }
            recipesAdapter.submitList(recipes.toMutableList())
            binding.progressBar.visibility = if (recipes.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setupMenu(view: View) {
        MenuUtils.showPopupMenu(
            context = requireContext(),
            view = view,
            navController = findNavController(),
            auth = FirebaseAuth.getInstance()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
