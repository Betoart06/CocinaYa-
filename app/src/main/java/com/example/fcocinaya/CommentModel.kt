package com.example.fcocinaya

// Modelo que representa los comentarios recibidos de la API
data class CommentModel(
    val id: Int,
    val userId: Int,
    val userName: String?,
    val recipeId: Int,
    val content: String,
    val createdAt: String
)