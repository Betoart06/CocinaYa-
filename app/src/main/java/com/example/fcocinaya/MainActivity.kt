package com.example.fcocinaya // Cambia esto por el nombre de tu paquete

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.LinearLayout

class MainActivity : AppCompatActivity() { // Cambia el nombre de la clase si es necesario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth) // Asegúrate de que el layout sea correcto

        // Ejemplo de inicialización de vistas
        val loginContainer = findViewById<LinearLayout>(R.id.loginContainer)
        val registerContainer = findViewById<LinearLayout>(R.id.registerContainer)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnLogin.setOnClickListener {
            loginContainer.visibility = View.VISIBLE
            registerContainer.visibility = View.GONE
        }

        btnRegister.setOnClickListener {
            loginContainer.visibility = View.GONE
            registerContainer.visibility = View.VISIBLE
        }
    }
}