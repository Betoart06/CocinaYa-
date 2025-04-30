package com.example.fcocinaya

import android.app.Application

class FCocinaYaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inicializar RetrofitClient
        RetrofitClient.initialize(applicationContext)
    }
}