package com.example.recipes_mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.recipes_mobile.databinding.FragmentRecipeGeneratorBinding
import com.example.recipes_mobile.model.Content
import com.example.recipes_mobile.model.GeneratedRecipeRecipe
import com.example.recipes_mobile.model.Part
import com.example.recipes_mobile.model.Recipe
import com.example.recipes_mobile.model.RecipeRequest
import com.example.recipes_mobile.network.RecipeApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.gson.Gson

class RecipeGeneratorFragment : Fragment() {

    private var _binding: FragmentRecipeGeneratorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeGeneratorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGenerate.setOnClickListener {
            val ingredients = binding.etIngredients.text.toString()
            if (ingredients.isNotEmpty()) {
                fetchRecipe(ingredients)
            }
        }
    }

    private fun fetchRecipe(ingredients: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvRecipeResult.visibility = View.GONE

        val prompt = """
            Generate a recipe in JSON format using these ingredients: $ingredients.
            The JSON should have the following structure:
            {
                "name": "Recipe Name",
                "ingredients": [
                    { "name": "Ingredient 1", "amount": "Quantity and unit" },
                    { "name": "Ingredient 2", "amount": "Quantity and unit" }
                ],
                "steps": [
                    "Step 1",
                    "Step 2",
                    "Step 3"
                ]
            }
            Provide ONLY the valid JSON response.
        """.trimIndent()

        val request = RecipeRequest(listOf(Content(listOf(Part(prompt)))))

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RecipeApi.instance.getRecipe(request)
                val json = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                val noBackticks = json.replace("```", "").trim()

                val jsonStartIndex = noBackticks.indexOf("{")
                val jsonEndIndex = noBackticks.lastIndexOf("}")

                val cleanedJson = if (jsonStartIndex != -1 && jsonEndIndex != -1) {
                    noBackticks.substring(jsonStartIndex, jsonEndIndex + 1)
                } else {
                    noBackticks // Fallback if not found (shouldn't happen)
                }

                val recipe = Gson().fromJson(cleanedJson, GeneratedRecipeRecipe::class.java)


                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.tvRecipeResult.text =
                        "Recipe: ${recipe.name}\n\nIngredients:\n${recipe.ingredients.joinToString("\n") { "${it.name}: ${it.amount}" }}\n\nSteps:\n${recipe.steps.joinToString("\n")}"
                    binding.tvRecipeResult.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.tvRecipeResult.text = "Error: ${e.message}"
                    binding.tvRecipeResult.visibility = View.VISIBLE

                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}