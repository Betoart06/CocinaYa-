package com.example.fcocinaya

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProfileActivity : AppCompatActivity() {

    private lateinit var btnClose: Button
    private lateinit var profilePhoto: ImageView
    private lateinit var btnEditPhoto: FloatingActionButton
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var btnChangePassword: Button
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Inicializar vistas
        btnClose = findViewById(R.id.btnClose)
        profilePhoto = findViewById(R.id.profilePhoto)
        btnEditPhoto = findViewById(R.id.btnEditPhoto)
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnLogout = findViewById(R.id.btnLogout)

        // Configurar listeners de botones
        btnClose.setOnClickListener {
            finish() // Cerrar la actividad
        }

        btnEditPhoto.setOnClickListener {
            // Aquí puedes implementar la lógica para cambiar la foto de perfil
            Toast.makeText(this, "Cambiar foto de perfil", Toast.LENGTH_SHORT).show()
        }

        btnChangePassword.setOnClickListener {
            // Lógica para cambiar la contraseña
            Toast.makeText(this, "Cambiar contraseña", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            // Mostrar el diálogo de confirmación al hacer clic en "salir de la cuenta"
            showLogoutConfirmationDialog()
        }

        // Cargar datos del usuario (normalmente obtendrías estos datos de tu base de datos o preferencias)
        loadUserData()
    }

    private fun loadUserData() {
        // Aquí implementa la lógica para cargar los datos del usuario
        // Por ejemplo, desde Firebase, SharedPreferences, etc.

        // Ejemplo con datos estáticos (reemplazar con datos reales)
        tvUserName.text = "Vane<3"
        tvUserEmail.text = "angeles.uwu@gmail.com"

        // Si tienes una foto de perfil guardada, la cargarías aquí
        // Por ejemplo, usando Glide o Picasso
        // Glide.with(this).load(profileImageUrl).into(profilePhoto)
    }

    private fun showLogoutConfirmationDialog() {
        // Inflar el layout personalizado para el diálogo
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_logout_confirmation, null)

        // Crear el diálogo personalizado
        val dialogBuilder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(dialogView)
            .setCancelable(false)

        // Mostrar el diálogo
        val dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        // Configurar los botones del diálogo
        val btnContinuar = dialogView.findViewById<Button>(R.id.btnContinuar)
        val btnSalir = dialogView.findViewById<Button>(R.id.btnSalir)

        // Botón "Continuar" (cancelar el cierre de sesión)
        btnContinuar.setOnClickListener {
            dialog.dismiss()
        }

        // Botón "Salir" (confirmar el cierre de sesión)
        btnSalir.setOnClickListener {
            // Cerrar el diálogo
            dialog.dismiss()

            // Mostrar mensaje de cierre de sesión
            Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show()

            // Implementar cierre de sesión aquí (borrar token, limpiar datos, etc.)
            // ...

            // Redirigir al usuario a la pantalla de login (AuthActivity)
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}