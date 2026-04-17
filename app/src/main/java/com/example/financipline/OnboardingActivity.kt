package com.example.financipline

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.financipline.databinding.ActivityOnboardingBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Real-time Daily Math
        binding.etMonthlyBudget.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val monthly = s.toString().toDoubleOrNull() ?: 0.0
                val daily = monthly / 30
                binding.tvDailyCalculated.text = "₹${String.format("%.2f", daily)}"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnStartDiscipline.setOnClickListener {
            val monthlyAmount = binding.etMonthlyBudget.text.toString().toDoubleOrNull() ?: 0.0
            if (monthlyAmount > 0) {
                saveBudget(monthlyAmount)
            } else {
                Toast.makeText(this, "Please enter a valid budget", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveBudget(monthly: Double) {
        val daily: Double = monthly / 30.0
        val userId: String = SupabaseManager.client.auth.currentUserOrNull()?.id ?: ""

        lifecycleScope.launch {
            try {
                // Explicitly define the types for the map
                val updates = mapOf<String, Double>(
                    "monthly_budget" to monthly,
                    "daily_limit" to daily
                )

                SupabaseManager.client.postgrest["profiles"].update(updates) {
                    filter { eq("id", userId) }
                }

                startActivity(Intent(this@OnboardingActivity, HomeActivity::class.java))
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@OnboardingActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}