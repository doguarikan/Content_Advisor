package com.example.content_advisor

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private val PREF_NAME = "MyAppPrefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Oturum kontrolü yap
        checkUserSession()
    }

    private fun checkUserSession() {
        val sharedPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val userEmail = sharedPref.getString("email", null)
        
        if (userEmail != null && userEmail.isNotEmpty()) {
            // Kullanıcı zaten giriş yapmış, MainPageActivity'ye yönlendir
            val intent = Intent(this, MainPageActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Kullanıcı giriş yapmamış, LoginPageActivity'ye yönlendir
            val intent = Intent(this, LoginPageActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
} 