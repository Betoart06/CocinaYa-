package com.example.fcocinaya

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var toggleIndicator: View
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var loginContainer: LinearLayout
    private lateinit var registerContainer: LinearLayout
    private lateinit var forgotPasswordContainer: LinearLayout
    private lateinit var codeVerificationContainer: LinearLayout
    private lateinit var changePasswordContainer: LinearLayout

    // Variable para almacenar el email durante el proceso de recuperación
    private var recoveryEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Inicializar vistas
        toggleIndicator = findViewById(R.id.toggleIndicator)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        loginContainer = findViewById(R.id.loginContainer)
        registerContainer = findViewById(R.id.registerContainer)
        forgotPasswordContainer = findViewById(R.id.forgotPasswordContainer)
        codeVerificationContainer = findViewById(R.id.codeVerificationContainer)
        changePasswordContainer = findViewById(R.id.changePasswordContainer)

        // Configurar Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://cocinaya.onrender.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        // Botón "Ingresar"
        findViewById<Button>(R.id.btnLoginAction).setOnClickListener {
            if (validateLogin()) {
                val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
                val password = findViewById<EditText>(R.id.etPassword).text.toString().trim()

                val credentials = LoginRequest(email, password)
                apiService.login(credentials).enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        if (response.isSuccessful && response.body() != null) {
                            val token = response.body()!!.token
                            val user = response.body()!!.user

                            // Guardar la sesión del usuario
                            RetrofitClient.saveUserSession(
                                token,
                                user.id,
                                user.nombre_usuario,
                                user.email
                            )

                            Log.d("API_RESPONSE", "Login exitoso. Token: $token, Usuario: ${user.nombre_usuario}")

                            Snackbar.make(it, "Bienvenido, ${user.nombre_usuario}", Snackbar.LENGTH_SHORT).show()

                            startActivity(Intent(this@AuthActivity, HomeActivity::class.java))
                            finish()
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e("API_ERROR", "Error en login: $errorBody")
                            Snackbar.make(it, "Credenciales inválidas", Snackbar.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Log.e("API_FAILURE", "Error en la conexión: ${t.message}")
                        Snackbar.make(it, "Error de conexión: ${t.message}", Snackbar.LENGTH_SHORT).show()
                    }
                })
            }
        }

        // Botón "Registrar"
        findViewById<Button>(R.id.btnRegisterAction).setOnClickListener {
            val username = findViewById<EditText>(R.id.etUsername).text.toString().trim()
            val password = findViewById<EditText>(R.id.etPasswordRegister).text.toString().trim()
            val confirmPassword = findViewById<EditText>(R.id.etConfirmPassword).text.toString().trim()
            val email = findViewById<EditText>(R.id.etEmailRegister).text.toString().trim()

            if (password != confirmPassword) {
                Snackbar.make(it, "Las contraseñas no coinciden", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = RegisterRequest(username, email, password, confirmPassword)
            apiService.register(user).enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        Log.d("API_RESPONSE", "Registro exitoso para usuario: $username")
                        Snackbar.make(it, "Registro exitoso", Snackbar.LENGTH_SHORT).show()
                        toggleToLogin()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("API_ERROR", "Error en registro: $errorBody")
                        Snackbar.make(it, "Error en el registro", Snackbar.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    Log.e("API_FAILURE", "Error en la conexión: ${t.message}")
                    Snackbar.make(it, "Error de conexión: ${t.message}", Snackbar.LENGTH_SHORT).show()
                }
            })
        }

        // Configurar el toggle
        btnLogin.setOnClickListener { toggleToLogin() }
        btnRegister.setOnClickListener { toggleToRegister() }
        toggleToLogin()  // Iniciar con el formulario de login visible

        // Botón "Enviar código" (en el contenedor de recuperación de contraseña)
        findViewById<Button>(R.id.btnSendCode).setOnClickListener {
            val email = findViewById<EditText>(R.id.etForgotEmail).text.toString().trim()
            if (email.isEmpty()) {
                Snackbar.make(it, "El correo es obligatorio", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Guardar el email para usarlo en los siguientes pasos
            recoveryEmail = email

            val request = ForgotPasswordRequest(email)
            apiService.forgotPassword(request).enqueue(object : Callback<ForgotPasswordResponse> {
                override fun onResponse(call: Call<ForgotPasswordResponse>, response: Response<ForgotPasswordResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        Snackbar.make(it, "Código enviado a tu correo", Snackbar.LENGTH_SHORT).show()
                        showCodeVerificationContainer()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("API_ERROR", "Error al enviar código: $errorBody")
                        Snackbar.make(it, "Error al enviar código", Snackbar.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ForgotPasswordResponse>, t: Throwable) {
                    Log.e("API_FAILURE", "Error en la conexión: ${t.message}")
                    Snackbar.make(it, "Error de conexión: ${t.message}", Snackbar.LENGTH_SHORT).show()
                }
            })
        }

        // Botón "Verificar código" (en el contenedor de verificación de código)
        findViewById<Button>(R.id.btnVerifyCode).setOnClickListener {
            val code = findViewById<EditText>(R.id.etCode).text.toString().trim()

            if (code.isEmpty()) {
                Snackbar.make(it, "El código es obligatorio", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = VerifyCodeRequest(recoveryEmail, code)
            apiService.verifyCode(request).enqueue(object : Callback<VerifyCodeResponse> {
                override fun onResponse(call: Call<VerifyCodeResponse>, response: Response<VerifyCodeResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        Snackbar.make(it, "Código verificado correctamente", Snackbar.LENGTH_SHORT).show()

                        // Pasar el código a la pantalla de cambio de contraseña
                        val codeEditText = findViewById<EditText>(R.id.etVerifiedCode)
                        codeEditText.setText(code)

                        showChangePasswordContainer()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("API_ERROR", "Error al verificar código: $errorBody")
                        Snackbar.make(it, "Código inválido o expirado", Snackbar.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<VerifyCodeResponse>, t: Throwable) {
                    Log.e("API_FAILURE", "Error en la conexión: ${t.message}")
                    Snackbar.make(it, "Error de conexión: ${t.message}", Snackbar.LENGTH_SHORT).show()
                }
            })
        }

        // Botón "Guardar" (en el contenedor de cambio de contraseña)
        findViewById<Button>(R.id.btnSavePassword).setOnClickListener {
            val newPassword = findViewById<EditText>(R.id.etNewPassword).text.toString().trim()
            val confirmNewPassword = findViewById<EditText>(R.id.etConfirmNewPassword).text.toString().trim()
            val verifiedCode = findViewById<EditText>(R.id.etVerifiedCode).text.toString().trim()

            if (newPassword.isEmpty()) {
                Snackbar.make(it, "La nueva contraseña es obligatoria", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (confirmNewPassword.isEmpty()) {
                Snackbar.make(it, "Debes confirmar la nueva contraseña", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmNewPassword) {
                Snackbar.make(it, "Las contraseñas no coinciden", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (verifiedCode.isEmpty()) {
                Snackbar.make(it, "Error: código de verificación no disponible", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Crear la solicitud según el formato que espera la API
            val request = ResetPasswordRequest(token = verifiedCode, newPassword = newPassword)

            apiService.resetPassword(request).enqueue(object : Callback<ResetPasswordResponse> {
                override fun onResponse(call: Call<ResetPasswordResponse>, response: Response<ResetPasswordResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        Snackbar.make(it, "Contraseña cambiada exitosamente", Snackbar.LENGTH_SHORT).show()
                        recoveryEmail = "" // Limpiar el email almacenado
                        toggleToLogin()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("API_ERROR", "Error al cambiar contraseña: $errorBody")
                        Snackbar.make(it, "Error al cambiar contraseña", Snackbar.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResetPasswordResponse>, t: Throwable) {
                    Log.e("API_FAILURE", "Error en la conexión: ${t.message}")
                    Snackbar.make(it, "Error de conexión: ${t.message}", Snackbar.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun toggleToLogin() {
        toggleIndicator.animate().translationX(0f).setDuration(400).start()
        btnLogin.setTextColor(ContextCompat.getColor(this, R.color.blanco))
        btnRegister.setTextColor(ContextCompat.getColor(this, R.color.darkRed))
        loginContainer.visibility = View.VISIBLE
        registerContainer.visibility = View.GONE
        forgotPasswordContainer.visibility = View.GONE
        codeVerificationContainer.visibility = View.GONE
        changePasswordContainer.visibility = View.GONE
    }

    private fun toggleToRegister() {
        val endPosition = btnRegister.x - btnLogin.x
        toggleIndicator.animate().translationX(endPosition).setDuration(400).start()
        btnRegister.setTextColor(ContextCompat.getColor(this, R.color.blanco))
        btnLogin.setTextColor(ContextCompat.getColor(this, R.color.darkRed))
        registerContainer.visibility = View.VISIBLE
        loginContainer.visibility = View.GONE
        forgotPasswordContainer.visibility = View.GONE
        codeVerificationContainer.visibility = View.GONE
        changePasswordContainer.visibility = View.GONE
    }

    private fun validateLogin(): Boolean {
        val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
        val password = findViewById<EditText>(R.id.etPassword).text.toString().trim()

        if (email.isEmpty()) {
            findViewById<EditText>(R.id.etEmail).error = "El correo es obligatorio"
            return false
        }

        if (password.isEmpty()) {
            findViewById<EditText>(R.id.etPassword).error = "La contraseña es obligatoria"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            findViewById<EditText>(R.id.etEmail).error = "Correo inválido"
            return false
        }

        return true
    }

    fun onForgotPasswordClick(view: View) {
        showForgotPasswordContainer()
    }

    // Método para mostrar el contenedor de recuperación de contraseña
    private fun showForgotPasswordContainer() {
        loginContainer.visibility = View.GONE
        registerContainer.visibility = View.GONE
        forgotPasswordContainer.visibility = View.VISIBLE
        codeVerificationContainer.visibility = View.GONE
        changePasswordContainer.visibility = View.GONE
        recoveryEmail = "" // Limpiar el email cuando se inicia el proceso
    }

    // Método para mostrar el contenedor de verificación de código
    private fun showCodeVerificationContainer() {
        loginContainer.visibility = View.GONE
        registerContainer.visibility = View.GONE
        forgotPasswordContainer.visibility = View.GONE
        codeVerificationContainer.visibility = View.VISIBLE
        changePasswordContainer.visibility = View.GONE
    }

    // Método para mostrar el contenedor de cambio de contraseña
    private fun showChangePasswordContainer() {
        loginContainer.visibility = View.GONE
        registerContainer.visibility = View.GONE
        forgotPasswordContainer.visibility = View.GONE
        codeVerificationContainer.visibility = View.GONE
        changePasswordContainer.visibility = View.VISIBLE
    }
}