package com.example.fcocinaya

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash) // Asegúrate de que el layout sea correcto

        // Configura el GestureDetector para detectar gestos
        gestureDetector = GestureDetector(this, GestureListener())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Pasa el evento táctil al GestureDetector
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            // Detecta el gesto de deslizamiento hacia arriba
            if (e1 != null && e2 != null && e1.y - e2.y > 50) { // Deslizamiento hacia arriba
                startActivity(Intent(this@SplashActivity, AuthActivity::class.java)) // Navega a AuthActivity
                finish() // Cierra la SplashActivity
                return true
            }
            return false
        }
    }
}