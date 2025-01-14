package com.android.grocerymanger

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignIn: Button
    private lateinit var signUpLink: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acitvity_sign_in)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if the user is already signed in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // If user is already logged in, navigate to MainActivity
            navigateToMainActivity()
        }

        // Find views
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnSignIn = findViewById(R.id.btnSignIn)
        signUpLink = findViewById(R.id.signUpLink)

        // Set onClickListener for SignIn Button
        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Sign in the user with email and password
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // If sign-in is successful, navigate to the Main activity
                            navigateToMainActivity()
                        } else {
                            // If sign-in fails, show an error message
                            Toast.makeText(this, "Sign In Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Set onClickListener for navigating to SignUpActivity
        signUpLink.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    // Function to navigate to MainActivity
    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish() // Close the SignInActivity to prevent going back to it
    }
}
