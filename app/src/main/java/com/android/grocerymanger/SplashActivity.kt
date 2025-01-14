package com.android.grocerymanger

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the splash screen layout
        setContentView(R.layout.activity_splash)
        // Disable the ActionBar (App's name) from showing
        supportActionBar?.hide()
        // Use a handler to create a delay before starting MainActivity
        Handler().postDelayed({

            // Intent to navigate from SplashActivity to MainActivity
            var intent = Intent(this, HomeActivity::class.java)
            // Initialize Firebase Auth
            var auth = FirebaseAuth.getInstance()
            // Check if the user is already signed in
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // If user is already logged in, navigate to MainActivity
                intent = Intent(this,MainActivity::class.java)
            }
            startActivity(intent)

            // Finish SplashActivity to remove it from the back stack
            finish()
        }, 3000) // 3 seconds delay
    }
}
