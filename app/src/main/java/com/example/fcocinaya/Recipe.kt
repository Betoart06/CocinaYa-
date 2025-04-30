package com.example.fcocinaya

import com.example.fcocinaya.model.RecipeModel

data class Recipe(
    val id: Int = -1,
    val name: String,
    val ingredients: String = "",
    val steps: String = "",
    val imageUrl: String = "",
    val imageResId: Int = R.drawable.comida_fondo, // Para compatibilidad con código existente
    val userId: Int = -1,
) {
    // Constructor secundario para mantener compatibilidad con código existente
    constructor(name: String, imageResId: Int = R.drawable.comida_fondo) : this(
        id = -1,
        name = name,
        imageResId = imageResId
    )

    // Método para convertir de RecipeModel a Recipe
    companion object {
        fun fromRecipeModel(model: RecipeModel): Recipe {
            return Recipe(
                id = model.id,
                name = model.titulo,
                ingredients = model.ingredientes,
                steps = model.pasos,
                imageUrl = model.imagen,
                userId = model.userId,
            )
        }
    }

    // Método para convertir a RecipeRequest
    fun toRecipeRequest(): RecipeRequest {
        return RecipeRequest(
            titulo = name,
            ingredientes = ingredients,
            pasos = steps,
            imagen = imageUrl
        )
    }
}