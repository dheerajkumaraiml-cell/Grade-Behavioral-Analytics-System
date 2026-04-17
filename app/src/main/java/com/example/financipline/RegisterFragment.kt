package com.example.financipline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.financipline.databinding.FragmentRegisterBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvGoToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etRegEmail.text.toString()
            val pass = binding.etRegPassword.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                lifecycleScope.launch {
                    try {
                        // 1. Auth Sign Up
                        val userInfo = SupabaseManager.client.auth.signUpWith(Email) {
                            this.email = email
                            this.password = pass
                        }

                        // 2. Save Name to Profiles using Map-insertion
                        userInfo?.let {
                            SupabaseManager.client.postgrest["profiles"].insert(
                                mapOf("id" to it.id, "full_name" to name)
                            )
                        }

                        Toast.makeText(context, "Account Created! Verify email.", Toast.LENGTH_LONG).show()
                        findNavController().navigate(R.id.action_register_to_login)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}