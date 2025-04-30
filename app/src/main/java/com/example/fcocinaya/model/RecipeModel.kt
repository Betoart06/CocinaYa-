package com.example.fcocinaya.model

import com.google.gson.annotations.SerializedName

data class RecipeModel(
    @SerializedName("ID_receta") val id: Int,
    @SerializedName("cuenta_id") val userId: Int,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("ingredientes") val ingredientes: String,
    @SerializedName("pasos") val pasos: String,
    @SerializedName("imagen") val imagen: String,
    @SerializedName("fecha_publicacion") val fechaPublicacion: String?
)