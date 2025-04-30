package com.example.fcocinaya

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CommunityAdapter(
    private var recipes: List<Recipe>,
    private val onRecipeClick: (Recipe) -> Unit,
    private val onLikeClick: (Recipe) -> Unit,
    private val onCommentClick: (Recipe) -> Unit,
    private val onFavoriteClick: (Recipe) -> Unit
) : RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder>() {

    // Estado de likes y favoritos (en una app real estos vendrían de la API)
    private val likedRecipes = mutableSetOf<Int>()
    private val favoriteRecipes = mutableSetOf<Int>()

    class CommunityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRecipeName: TextView = itemView.findViewById(R.id.tvRecipeName)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val ivRecipeImage: ImageView = itemView.findViewById(R.id.ivRecipeImage)
        val btnLike: ImageButton = itemView.findViewById(R.id.btnLike)
        val btnComment: ImageButton = itemView.findViewById(R.id.btnComment)
        val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite)
        val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        val tvCommentCount: TextView = itemView.findViewById(R.id.tvCommentCount)
        val btnViewRecipe: Button = itemView.findViewById(R.id.btnViewRecipe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community_recipe, parent, false)
        return CommunityViewHolder(view)
    }

    override fun getItemCount(): Int = recipes.size

    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        val recipe = recipes[position]

        // Configurar datos básicos
        holder.tvRecipeName.text = recipe.name
        holder.tvUserName.text = "Por ${recipe.userName}"

        // Configurar imagen de la receta
        if (recipe.imageUrl.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(recipe.imageUrl, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.ivRecipeImage.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                // En caso de error, usar imagen por defecto
                holder.ivRecipeImage.setImageResource(R.drawable.comida_fondo)
            }
        } else {
            // Usar imagen por defecto si no hay imagen
            holder.ivRecipeImage.setImageResource(R.drawable.comida_fondo)
        }

        // Configurar contadores (en una app real, estos vendrían de la API)
        holder.tvLikeCount.text = "0 likes" // Esto debería venir de la API
        holder.tvCommentCount.text = "0 comentarios" // Esto debería venir de la API

        // Configurar estado de botones
        updateLikeButton(holder.btnLike, recipe.id)
        updateFavoriteButton(holder.btnFavorite, recipe.id)

        // Configurar listeners
        holder.btnViewRecipe.setOnClickListener {
            onRecipeClick(recipe)
        }

        holder.btnLike.setOnClickListener {
            toggleLikeState(recipe.id)
            updateLikeButton(holder.btnLike, recipe.id)
            onLikeClick(recipe)
        }

        holder.btnComment.setOnClickListener {
            onCommentClick(recipe)
        }

        holder.btnFavorite.setOnClickListener {
            toggleFavoriteState(recipe.id)
            updateFavoriteButton(holder.btnFavorite, recipe.id)
            onFavoriteClick(recipe)
        }

        // La tarjeta completa también puede ser clickeable
        holder.itemView.setOnClickListener {
            onRecipeClick(recipe)
        }
    }

    // Actualizar la lista de recetas
    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }

    // Cambiar estado de like
    private fun toggleLikeState(recipeId: Int) {
        if (likedRecipes.contains(recipeId)) {
            likedRecipes.remove(recipeId)
        } else {
            likedRecipes.add(recipeId)
        }
    }

    // Cambiar estado de favorito
    private fun toggleFavoriteState(recipeId: Int) {
        if (favoriteRecipes.contains(recipeId)) {
            favoriteRecipes.remove(recipeId)
        } else {
            favoriteRecipes.add(recipeId)
        }
    }

    // Actualizar apariencia del botón de like
    private fun updateLikeButton(button: ImageButton, recipeId: Int) {
        if (likedRecipes.contains(recipeId)) {
            button.setImageResource(R.drawable.ic_like_filled)
        } else {
            button.setImageResource(R.drawable.ic_like_outline)
        }
    }

    // Actualizar apariencia del botón de favorito
    private fun updateFavoriteButton(button: ImageButton, recipeId: Int) {
        if (favoriteRecipes.contains(recipeId)) {
            button.setImageResource(R.drawable.ic_favorite_filled)
        } else {
            button.setImageResource(R.drawable.ic_favorite_outline)
        }
    }
}