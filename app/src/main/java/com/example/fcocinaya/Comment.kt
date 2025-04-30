package com.example.fcocinaya

// Modelo de comentario para la UI
data class Comment(
    val id: Int,
    val userId: Int,
    val userName: String,
    val recipeId: Int,
    val content: String,
    val createdAt: String
) {
    companion object {
        fun fromCommentModel(model: CommentModel): Comment {
            return Comment(
                id = model.id,
                userId = model.userId,
                userName = model.userName ?: "Usuario",
                recipeId = model.recipeId,
                content = model.content,
                createdAt = model.createdAt
            )
        }
    }
}