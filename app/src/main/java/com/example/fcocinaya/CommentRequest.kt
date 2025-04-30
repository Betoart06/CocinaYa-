package com.example.fcocinaya

// Modelo para enviar un nuevo comentario a la API
data class CommentRequest(
    val userId: Int,
    val recipeId: Int,
    val content: String
)

// Respuesta al crear un comentario
data class CommentResponse(
    val message: String,
    val commentId: Int
)