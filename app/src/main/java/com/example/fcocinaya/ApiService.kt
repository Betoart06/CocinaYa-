package com.example.fcocinaya

import com.example.fcocinaya.model.RecipeModel
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // Endpoints de autenticación existentes
    @POST("cuenta")
    fun register(@Body user: RegisterRequest): Call<RegisterResponse>

    @POST("login")
    fun login(@Body credentials: LoginRequest): Call<LoginResponse>

    @POST("auth/forgot-password")
    fun forgotPassword(@Body request: ForgotPasswordRequest): Call<ForgotPasswordResponse>

    @POST("auth/reset-password")
    fun resetPassword(@Body request: ResetPasswordRequest): Call<ResetPasswordResponse>

    @POST("auth/verify-code")
    fun verifyCode(@Body request: VerifyCodeRequest): Call<VerifyCodeResponse>

    // Endpoints para recetas
    

    @POST("receta")
    fun createRecipe(@Body recipe: RecipeRequest): Call<RecipeResponse>

    @GET("receta")
    fun getAllRecipes(): Call<List<RecipeModel>>

    @GET("receta/explorar")
    fun getExploreRecipes(@Query("userId") userId: Int): Call<List<RecipeModel>>

    @GET("receta/mis-recetas/{userId}")
    fun getUserRecipes(@Path("userId") userId: Int): Call<List<RecipeModel>>

    @GET("receta/{recipeId}")
    fun getRecipeById(@Path("recipeId") recipeId: Int): Call<RecipeModel>

    @PUT("receta/{recipeId}")
    fun updateRecipe(@Path("recipeId") recipeId: Int, @Body recipe: RecipeRequest): Call<RecipeResponse>

    @DELETE("receta/{recipeId}")
    fun deleteRecipe(@Path("recipeId") recipeId: Int): Call<DeleteRecipeResponse>

    // NUEVO: Endpoint para publicar/despublicar una receta
    @POST("receta/{recipeId}/publicar")
    fun publishRecipe(
        @Path("recipeId") recipeId: Int,
        @Body request: PublishRecipeRequest
    ): Call<PublishRecipeResponse>

    // Endpoints para comentarios
    @GET("receta/{recipeId}/comentarios")
    fun getRecipeComments(@Path("recipeId") recipeId: Int): Call<List<CommentModel>>

    @POST("comentario")
    fun addComment(@Body comment: CommentRequest): Call<CommentResponse>

    // Endpoint para recetas de la comunidad
    @GET("receta/comunidad")
    fun getCommunityRecipes(): Call<List<RecipeModel>>

    // Endpoints para favoritos
    @GET("favorito/check/{recipeId}/{userId}")
    fun checkFavorite(
        @Path("recipeId") recipeId: Int,
        @Path("userId") userId: Int
    ): Call<FavoriteResponse>

    @GET("favorito/usuario/{userId}")
    fun getUserFavorites(@Path("userId") userId: Int): Call<List<RecipeModel>>

    @POST("favorito")
    fun addToFavorites(@Body request: FavoriteRequest): Call<FavoriteResponse>

    @DELETE("favorito/{recipeId}/{userId}")
    fun removeFromFavorites(
        @Path("recipeId") recipeId: Int,
        @Path("userId") userId: Int
    ): Call<FavoriteResponse>
}

// Clases existentes (RegisterRequest, LoginResponse, etc.)

// Clases para recetas
data class RecipeRequest(
    val titulo: String,
    val ingredientes: String,
    val pasos: String,
    val imagen: String
)

data class RecipeResponse(
    val message: String,
    val recipeId: Int
)

data class RecipeModel(
    val id: Int,
    val titulo: String,
    val ingredientes: String,
    val pasos: String,
    val imagen: String,
    val userId: Int,
    val isPublished: Boolean = false,
    val userName: String? = null
)

data class DeleteRecipeResponse(
    val message: String,
    val recipeId: Int
)

// Nuevas clases para publicar/despublicar recetas
data class PublishRecipeRequest(
    val isPublished: Boolean
)

data class PublishRecipeResponse(
    val message: String,
    val success: Boolean,
    val recipe: RecipeModel? = null
)

// Clases para favoritos
data class FavoriteRequest(
    val userId: Int,
    val recipeId: Int
)

data class FavoriteResponse(
    val message: String,
    val status: Boolean
)


