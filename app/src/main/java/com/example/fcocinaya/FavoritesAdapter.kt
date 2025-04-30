package com.example.fcocinaya

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FavoritesAdapter(
    private val context: Context,
    private val favorites: MutableList<Recipe>,
    private val onFavoriteRemoved: (Int) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder>() {

    // ViewHolder para cada elemento de la lista
    class FavoritesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeName: TextView = itemView.findViewById(R.id.recipeName)
        val buttonViewRecipe: Button = itemView.findViewById(R.id.buttonViewRecipe)
        val buttonDelete: Button = itemView.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesViewHolder {
        // Inflar el layout de cada elemento de la lista
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favoritos, parent, false)
        return FavoritesViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoritesViewHolder, position: Int) {
        // Asignar los datos a cada elemento de la lista
        val recipe = favorites[position]
        holder.recipeName.text = recipe.name

        // Configurar el clic en el botón "Ver Receta"
        holder.buttonViewRecipe.setOnClickListener {
            if (recipe.id <= 0) {
                Toast.makeText(context, "No se puede abrir esta receta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Abrir la actividad de detalle de receta
            val intent = Intent(context, RecipeDetailActivity::class.java).apply {
                putExtra("recipe_id", recipe.id)
                putExtra("recipe_name", recipe.name)

                // Si es una receta estática (ID >= 100), pasar datos adicionales
                if (recipe.id >= 100) {
                    putExtra("is_static_recipe", true)
                    putExtra("recipe_ingredients", recipe.ingredients)
                    putExtra("recipe_steps", recipe.steps)
                }
            }
            context.startActivity(intent)
        }

        // Configurar el clic en el botón "Eliminar"
        holder.buttonDelete.setOnClickListener {
            if (recipe.id <= 0) {
                Toast.makeText(context, "No se puede eliminar esta receta de favoritos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            removeFromFavorites(recipe, position)
        }
    }

    private fun removeFromFavorites(recipe: Recipe, position: Int) {
        val userId = RetrofitClient.getUserId()

        if (userId <= 0) {
            Toast.makeText(context, "Debes iniciar sesión para gestionar favoritos", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar mensaje de progreso
        Toast.makeText(context, "Eliminando de favoritos...", Toast.LENGTH_SHORT).show()

        try {
            RetrofitClient.getApiService().removeFromFavorites(recipe.id, userId)
                .enqueue(object : Callback<FavoriteResponse> {
                    override fun onResponse(call: Call<FavoriteResponse>, response: Response<FavoriteResponse>) {
                        if (response.isSuccessful) {
                            // Eliminar de la lista y notificar cambio
                            favorites.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, favorites.size)

                            // Notificar al fragmento para actualizar la vista si la lista está vacía
                            onFavoriteRemoved(favorites.size)

                            Toast.makeText(context, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error al eliminar de favoritos", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                        Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                    }
                })
        } catch (e: Exception) {
            Toast.makeText(context, "Función de favoritos no disponible", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return favorites.size
    }
}