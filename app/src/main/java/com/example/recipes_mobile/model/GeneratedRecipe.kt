package com.example.recipes_mobile.model

data class GeneratedRecipeRecipe(
    val name: String,
    val ingredients: List<Ingredient>,
    val steps: List<String>
)

data class Ingredient(
    val name: String,
    val amount: String
)

data class RecipeRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class RecipeResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: Content
)
