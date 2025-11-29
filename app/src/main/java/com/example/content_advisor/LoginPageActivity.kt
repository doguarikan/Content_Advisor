package com.example.content_advisor

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.graphics.Paint
import android.util.Log
import android.widget.Toast
import com.example.content_advisor.databinding.ActivityLoginPageBinding
import com.google.firebase.database.*

class LoginPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginPageBinding
    private lateinit var firebaseRef: DatabaseReference
    private val PREF_NAME = "MyAppPrefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Eğer kullanıcı zaten giriş yapmışsa MainPageActivity'ye yönlendir
        checkExistingSession()

        Log.d("LoginActivity", "onCreate started")

        binding = ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        try {
            Log.d("LoginActivity", "Initializing Firebase...")
            firebaseRef = FirebaseDatabase.getInstance().getReference("users")
            Log.d("LoginActivity", "Firebase reference created successfully")
        } catch (e: Exception) {
            Log.e("LoginActivity", "Exception during Firebase init: ${e.message}")
        }

        binding.notRegisteredText.paintFlags = binding.notRegisteredText.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.notRegisteredText.setOnClickListener {
            val intent = Intent(this, RegisterPageActivity::class.java)
            startActivity(intent)
        }

        binding.loginButton.setOnClickListener {
            Log.d("LoginActivity", "Login button clicked")
            login()
        }
    }

    private fun checkExistingSession() {
        val sharedPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val userEmail = sharedPref.getString("email", null)
        
        if (userEmail != null && userEmail.isNotEmpty()) {
            val intent = Intent(this, MainPageActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun login() {
        Log.d("LoginActivity", "login() called")
        
        val email = binding.loginMailInput.text.toString()
        val pass = binding.loginPassInput.text.toString()

        Log.d("LoginActivity", "Input values - Email: ${email.length} chars, Pass: ${pass.length} chars")

        if (email.isEmpty() || pass.isEmpty()) {
            Log.d("LoginActivity", "Empty fields detected")
            Toast.makeText(this, "Please Enter Your Data!", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("LoginActivity", "Starting Firebase query...")
        firebaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("LoginActivity", "Firebase query onDataChange called - snapshot exists: ${snapshot.exists()}")
                Log.d("LoginActivity", "Children count: ${snapshot.childrenCount}")
                
                var userFound = false

                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    Log.d("LoginActivity", "Checking user: ${user?.mail}")

                    if (user?.mail == email && user.pass == pass) {
                        Log.d("LoginActivity", "✅ User found and credentials match!")
                        userFound = true
                        val sharedPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                        with (sharedPref.edit()) {
                            putString("email", email)
                            apply()
                        }
                        Toast.makeText(this@LoginPageActivity, "Login Successful!", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@LoginPageActivity, MainPageActivity::class.java)
                        startActivity(intent)
                        finish()
                        break
                    }
                }

                if (!userFound) {
                    Log.d("LoginActivity", "❌ User not found or credentials don't match")
                    Toast.makeText(this@LoginPageActivity, "Email Or Password Is Wrong!", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("LoginActivity", "❌ Firebase query cancelled: ${error.message}")
                Toast.makeText(this@LoginPageActivity, "Connection Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

}