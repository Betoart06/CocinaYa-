package com.example.fcocinaya

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class YourRecipesAdapter(
    private val context: Context,
    private val recipes: MutableList<Recipe>,
    private val onRecipeDeleted: (Int) -> Unit,
    private val onRecipePublished: (Recipe) -> Unit
) : RecyclerView.Adapter<YourRecipesAdapter.YourRecipesViewHolder>() {

    class YourRecipesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeName: TextView = itemView.findViewById(R.id.recipeName)
        val buttonViewRecipe: Button = itemView.findViewById(R.id.buttonViewRecipe)
        val buttonDelete: Button = itemView.findViewById(R.id.buttonDelete)
        val buttonSubirReceta: Button = itemView.findViewById(R.id.buttonSubirReceta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YourRecipesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tus_receta, parent, false)

        // Asegurarnos que los elementos ocupen el ancho completo
        val layoutParams = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        view.layoutParams = layoutParams

        return YourRecipesViewHolder(view)
    }

    override fun onBindViewHolder(holder: YourRecipesViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.recipeName.text = recipe.name

        // Actualizar el texto del botón de publicación según el estado actual
        updatePublishButtonText(holder.buttonSubirReceta, recipe.isPublished)

        // Configurar botón Ver Receta
        holder.buttonViewRecipe.setOnClickListener {
            if (recipe.id <= 0) {
                Toast.makeText(context, "No se puede abrir esta receta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(context, RecipeDetailActivity::class.java).apply {
                putExtra("recipe_id", recipe.id)
                putExtra("recipe_name", recipe.name)
                putExtra("is_static_recipe", recipe.id >= 100)
                if (recipe.id >= 100) {
                    putExtra("recipe_ingredients", recipe.ingredients)
                    putExtra("recipe_steps", recipe.steps)
                }
            }
            context.startActivity(intent)
        }

        // Configurar botón Eliminar
        holder.buttonDelete.setOnClickListener {
            if (recipe.id <= 0) {
                Toast.makeText(context, "No se puede eliminar esta receta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showDeleteConfirmationDialog(recipe, position)
        }

        // Configurar botón Publicar/Despublicar
        holder.buttonSubirReceta.setOnClickListener {
            if (recipe.id <= 0) {
                Toast.makeText(context, "No se puede publicar esta receta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            togglePublishState(recipe, holder.buttonSubirReceta, position)
        }
    }

    private fun updatePublishButtonText(button: Button, isPublished: Boolean) {
        button.text = if (isPublished) "Despublicar" else "Publicar"
    }

    private fun showDeleteConfirmationDialog(recipe: Recipe, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Eliminar receta")
            .setMessage("¿Estás seguro de que deseas eliminar la receta '${recipe.name}'?")
            .setPositiveButton("Sí") { _, _ ->
                deleteRecipe(recipe, position)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteRecipe(recipe: Recipe, position: Int) {
        // Mostrar mensaje de progreso
        Toast.makeText(context, "Eliminando receta...", Toast.LENGTH_SHORT).show()

        RetrofitClient.getApiService().deleteRecipe(recipe.id)
            .enqueue(object : Callback<DeleteRecipeResponse> {
                override fun onResponse(
                    call: Call<DeleteRecipeResponse>,
                    response: Response<DeleteRecipeResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("YourRecipesAdapter", "Receta eliminada: ${recipe.name}")
                        // Eliminar de la lista y notificar cambio
                        recipes.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, recipes.size)

                        // Notificar al fragmento para actualizar la vista si la lista está vacía
                        onRecipeDeleted(recipes.size)

                        Toast.makeText(context, "Receta eliminada correctamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("YourRecipesAdapter", "Error al eliminar receta: ${response.code()}")
                        Toast.makeText(context, "Error al eliminar la receta", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<DeleteRecipeResponse>, t: Throwable) {
                    Log.e("YourRecipesAdapter", "Error de conexión: ${t.message}")
                    Toast.makeText(
                        context,
                        "Error de conexión: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun togglePublishState(recipe: Recipe, button: Button, position: Int) {
        val action = if (recipe.isPublished) "despublicar" else "publicar"
        val newPublishState = !recipe.isPublished

        // Actualizar UI optimistamente
        updatePublishButtonText(button, newPublishState)

        // Actualizar el objeto en la lista
        val updatedRecipe = recipe.copy(isPublished = newPublishState)
        recipes[position] = updatedRecipe

        // Notificar cambio al adaptador
        notifyItemChanged(position)

        // Notificar al fragmento
        onRecipePublished(updatedRecipe)

        // Preparar la solicitud
        val publishRequest = PublishRecipeRequest(isPublished = newPublishState)

        // Llamar a la API para publicar/despublicar
        RetrofitClient.getApiService().publishRecipe(recipe.id, publishRequest)
            .enqueue(object : Callback<PublishRecipeResponse> {
                override fun onResponse(
                    call: Call<PublishRecipeResponse>,
                    response: Response<PublishRecipeResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("YourRecipesAdapter", "Receta ${action}da correctamente: ${recipe.name}")
                        Toast.makeText(
                            context,
                            "Receta ${action}da correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.e("YourRecipesAdapter", "Error al ${action} la receta: ${response.code()}")

                        // Revertir cambio si hay error
                        recipes[position] = recipe.copy(isPublished = !newPublishState)
                        updatePublishButtonText(button, !newPublishState)
                        notifyItemChanged(position)

                        Toast.makeText(
                            context,
                            "Error al ${action} la receta",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<PublishRecipeResponse>, t: Throwable) {
                    Log.e("YourRecipesAdapter", "Error de conexión: ${t.message}")

                    // Revertir cambio si hay error de conexión
                    recipes[position] = recipe.copy(isPublished = !newPublishState)
                    updatePublishButtonText(button, !newPublishState)
                    notifyItemChanged(position)

                    Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun getItemCount(): Int = recipes.size
}