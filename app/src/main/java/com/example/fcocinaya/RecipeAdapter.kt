package com.example.fcocinaya

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class RecipeAdapter(
    private val context: Context,
    private val recipes: List<Recipe>
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeImage: ImageView = itemView.findViewById(R.id.recipeImage)
        val recipeName: TextView = itemView.findViewById(R.id.recipeName)
        val btnViewRecipe: Button = itemView.findViewById(R.id.buttonViewRecipe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)

        // Asegurarnos que los elementos ocupen el ancho completo
        val layoutParams = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        view.layoutParams = layoutParams

        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]

        Log.d("RecipeAdapter", "Posición: $position, Receta: ${recipe.name}, ID: ${recipe.id}")

        holder.recipeName.text = recipe.name
        setRecipeImage(holder, recipe)

        holder.btnViewRecipe.setOnClickListener {
            Log.d("RecipeAdapter", "Se hizo clic en: ${recipe.name}")
            openRecipeDetail(recipe)
        }
    }


    private fun setRecipeImage(holder: RecipeViewHolder, recipe: Recipe) {
        // Intentar cargar la imagen desde base64 si existe
        if (recipe.imageUrl.isNotEmpty()) {
            try {
                // Verificar si la imagen ya está en formato Base64 o si necesita un prefijo
                var imageString = recipe.imageUrl
                // Eliminar prefijo "data:image/jpeg;base64," si existe
                if (imageString.contains(",")) {
                    imageString = imageString.split(",")[1]
                }

                val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                if (decodedImage != null) {
                    holder.recipeImage.setImageBitmap(decodedImage)
                } else {
                    // Si no se puede decodificar, usar imagen por defecto
                    holder.recipeImage.setImageResource(recipe.imageResId)
                }
            } catch (e: Exception) {
                Log.e("RecipeAdapter", "Error al decodificar imagen", e)
                // Si hay error, usar imagen por defecto
                holder.recipeImage.setImageResource(recipe.imageResId)
            }
        } else {
            // Si no hay imagen base64, usar imagen por defecto
            holder.recipeImage.setImageResource(recipe.imageResId)
        }
    }

    private fun openRecipeDetail(recipe: Recipe) {
        Log.d("RecipeAdapter", "Entrando a openRecipeDetail con ID: ${recipe.id}")

        if (recipe.id <= 0) {
            Log.w("RecipeAdapter", "ID inválido: ${recipe.id}")
            Toast.makeText(context, "No se puede abrir esta receta", Toast.LENGTH_SHORT).show()
            return
        }

        val isStaticRecipe = recipe.id >= 100
        val intent = Intent(context, RecipeDetailActivity::class.java)
        intent.putExtra("recipe_id", recipe.id)
        intent.putExtra("recipe_name", recipe.name)

        if (isStaticRecipe) {
            intent.putExtra("is_static_recipe", true)
            intent.putExtra("recipe_ingredients", recipe.ingredients)
            intent.putExtra("recipe_steps", recipe.steps)
        } else {
            intent.putExtra("is_static_recipe", false)
        }

        context.startActivity(intent)
    }


    override fun getItemCount(): Int = recipes.size
}