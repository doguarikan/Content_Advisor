package com.example.content_advisor

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.util.Log
import android.widget.Toast
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.content_advisor.databinding.ActivityRegisterPageBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

class RegisterPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterPageBinding
    private lateinit var firebaseRef : DatabaseReference
    private val PREF_NAME = "MyAppPrefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d("RegisterActivity", "onCreate started")

        binding = ActivityRegisterPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Google Play Services kontrolü
        checkGooglePlayServices()
        
        // Network connectivity kontrolü
        checkNetworkConnectivity()
        
        try {
            Log.d("RegisterActivity", "Initializing Firebase...")
            firebaseRef = FirebaseDatabase.getInstance().getReference("users")
            Log.d("RegisterActivity", "Firebase reference created successfully")
            
            // Firebase bağlantısını test et
            Log.d("RegisterActivity", "Starting Firebase connection test...")
            firebaseRef.child("connection_test").setValue("test_value_" + System.currentTimeMillis())
                .addOnSuccessListener {
                    Log.d("RegisterActivity", "✅ Firebase connection test SUCCESSFUL!")
                    Toast.makeText(this, "Firebase Connected Successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { error ->
                    Log.e("RegisterActivity", "❌ Firebase connection test FAILED: ${error.message}")
                    Toast.makeText(this, "Firebase Connection Failed: ${error.message}", Toast.LENGTH_LONG).show()
                }
        } catch (e: Exception) {
            Log.e("RegisterActivity", "Exception during Firebase init: ${e.message}")
        }

        binding.registerButton.setOnClickListener {
            Log.d("RegisterActivity", "Register button clicked")
            saveData()
        }
    }
    
    private fun checkGooglePlayServices() {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        
        when (resultCode) {
            ConnectionResult.SUCCESS -> {
                Log.d("RegisterActivity", "✅ Google Play Services is available")
            }
            else -> {
                Log.e("RegisterActivity", "❌ Google Play Services not available. Code: $resultCode")
                val isUserResolvableError = googleApiAvailability.isUserResolvableError(resultCode)
                Log.e("RegisterActivity", "Is user resolvable: $isUserResolvableError")
            }
        }
    }
    
    private fun checkNetworkConnectivity() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        
        val isConnected = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        Log.d("RegisterActivity", "Network connected: $isConnected")
        
        if (!isConnected) {
            Log.e("RegisterActivity", "❌ No internet connection!")
            Toast.makeText(this, "No internet connection!", Toast.LENGTH_LONG).show()
        }
    }
    private fun saveData() {
        Log.d("RegisterActivity", "saveData() called")
        
        val name = binding.registerNameInput.text.toString()
        val email = binding.registerMailInput.text.toString()
        val pass = binding.registerPassInput.text.toString()
        val repass = binding.registerRepassInput.text.toString()

        Log.d("RegisterActivity", "Input values - Name: ${name.length} chars, Email: ${email.length} chars, Pass: ${pass.length} chars, Repass: ${repass.length} chars")

        if (pass != repass) {
            Log.d("RegisterActivity", "Password mismatch")
            Toast.makeText(this,"Passwords not matching", Toast.LENGTH_LONG).show()
            return
        }
        
        if(name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && repass.isNotEmpty()) {
            Log.d("RegisterActivity", "All fields are filled, proceeding with registration...")
            
            try {
                val userId = firebaseRef.push().key!!
                Log.d("RegisterActivity", "Generated userId: $userId")
                val users = User(userId, email, name, pass)
                Log.d("RegisterActivity", "User object created: $users")
                Log.d("RegisterActivity", "Starting Firebase write operation...")
                firebaseRef.child(userId).setValue(users)
                    .addOnSuccessListener {
                        Log.d("RegisterActivity", "✅ Firebase write SUCCESSFUL!")
                        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_LONG).show()
                        val sharedPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                        with (sharedPref.edit()) {
                            putString("email", email)
                            apply()
                        }
                        Log.d("RegisterActivity", "SharedPreferences updated, navigating to MainPageActivity")
                        val intent = Intent(this, MainPageActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { error ->
                        Log.e("RegisterActivity", "❌ Firebase write FAILED: ${error.message}")
                        Toast.makeText(this, "Registration Failed: ${error.message}", Toast.LENGTH_LONG).show()
                    }
            } catch (e: Exception) {
                Log.e("RegisterActivity", "Exception during registration: ${e.message}")
                Toast.makeText(this, "Registration Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        else {
            Log.d("RegisterActivity", "Some fields are empty")
            Toast.makeText(this, "Please enter your data", Toast.LENGTH_LONG).show()
        }
    }
}