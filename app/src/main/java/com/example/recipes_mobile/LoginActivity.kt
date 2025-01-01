package com.example.recipes_mobile

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.emailEditText)
        val passwordEditText = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.passwordEditText)
        val submitButton = findViewById<Button>(R.id.loginButton)
        val toggleModeButton = findViewById<Button>(R.id.signupButton)

        submitButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Email or password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isLoginMode) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Log.d("LoginActivity", "Login successful")
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("LoginActivity", "Login failed", task.exception)
                            Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Log.d("LoginActivity", "Signup successful")
                            Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("LoginActivity", "Signup failed", task.exception)
                            Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        toggleModeButton.setOnClickListener {
            isLoginMode = !isLoginMode
            submitButton.text = if (isLoginMode) "Login" else "Sign Up"
            toggleModeButton.text = if (isLoginMode) "Switch to Sign Up" else "Switch to Login"
        }
    }
}
