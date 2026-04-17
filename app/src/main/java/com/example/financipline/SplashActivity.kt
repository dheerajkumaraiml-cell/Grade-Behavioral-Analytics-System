package com.example.financipline

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import io.github.jan.supabase.auth.auth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delay for 2 seconds to show off your beautiful logo
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserSession()
        }, 2000)
    }

    private fun checkUserSession() {
        // Use Supabase to check if a user is already logged in
        val session = SupabaseManager.client.auth.currentSessionOrNull()

        if (session != null) {
            // User is already logged in -> Go to Home
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            // No user found -> Go to Auth
            startActivity(Intent(this, AuthActivity::class.java))
        }
        finish() // Close splash so user can't go back to it
    }
}