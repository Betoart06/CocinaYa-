package com.example.fcocinaya

// Modelos para Register
data class RegisterRequest(
    val nombre_usuario: String,
    val email: String,
    val password: String,
    val password_confirmation: String
)

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val user: User
)

// Modelos para Login
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String,
    val user: User
)

// Modelos para ForgotPassword
data class ForgotPasswordRequest(
    val email: String
)

data class ForgotPasswordResponse(
    val success: Boolean,
    val message: String
)

// Modelos para VerifyCode
data class VerifyCodeRequest(
    val email: String,
    val code: String
)

data class VerifyCodeResponse(
    val success: Boolean,
    val message: String
)

// Modelos para ResetPassword - CORREGIDO según el formato esperado por la API
data class ResetPasswordRequest(
    val token: String,       // Aquí enviamos el código como token
    val newPassword: String  // Nombre del parámetro corregido
)

data class ResetPasswordResponse(
    val success: Boolean,
    val message: String
)

// Modelo de Usuario
data class User(
    val id: Int,
    val nombre_usuario: String,
    val email: String
)