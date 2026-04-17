package com.example.financipline

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.financipline.databinding.FragmentLoginBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                lifecycleScope.launch {
                    try {
                        // 1. Sign in the user
                        SupabaseManager.client.auth.signInWith(Email) {
                            this.email = email
                            this.password = pass
                        }

                        // 2. Determine where to send the user
                        checkOnboardingStatus()

                    } catch (e: Exception) {
                        Toast.makeText(context, "Login Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun checkOnboardingStatus() {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return

        lifecycleScope.launch {
            try {
                // Fetch the profile as a raw result to avoid serialization plugin errors
                val response = SupabaseManager.client.postgrest["profiles"]
                    .select {
                        filter { eq("id", userId) }
                    }

                val rawData = response.data

                // Check if the user is "Fresh" (Daily limit is 0 or no profile exists)
                if (rawData.contains("\"daily_limit\":0") || rawData == "[]") {
                    // Start Onboarding Activity
                    startActivity(Intent(requireContext(), OnboardingActivity::class.java))
                } else {
                    // Start Home Activity
                    startActivity(Intent(requireContext(), HomeActivity::class.java))
                }

                // Close the Auth activity entirely
                requireActivity().finish()

            } catch (e: Exception) {
                // Safety net: if DB check fails, assume onboarding is needed
                startActivity(Intent(requireContext(), OnboardingActivity::class.java))
                requireActivity().finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}