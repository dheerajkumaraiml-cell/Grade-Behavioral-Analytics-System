package com.example.financipline

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financipline.databinding.FragmentDashboardBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.put
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.MDC.put

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var pendingAdapter: PendingExpenseAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fabAddManual.setOnClickListener {
            showManualExpenseDialog()
        }
        // Set layout managers
        binding.rvPendingExpenses.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecentHistory.layoutManager = LinearLayoutManager(requireContext())

        // 1. Check & Request Notification Permission
        checkNotificationPermission()

        // 2. Load User Data and Calculate Progress
        loadDashboardData()
    }

    private fun checkNotificationPermission() {
        val enabledListeners = Settings.Secure.getString(requireContext().contentResolver, "enabled_notification_listeners")
        val isEnabled = enabledListeners?.contains(requireContext().packageName) == true

        if (!isEnabled) {
            showPermissionDialog()
        }
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Enable Auto-Tracking")
            .setMessage("To keep your streak alive automatically, Financipline needs to hear your payment notifications. Enable it in the next screen!")
            .setPositiveButton("Go to Settings") { _, _ ->
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            }
            .setNegativeButton("Maybe Later", null)
            .show()
    }

    private fun loadDashboardData() {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return

        lifecycleScope.launch {
            try {
                // Fetch Profile for Daily Limit
                val profileResponse = SupabaseManager.client.postgrest["profiles"]
                    .select { filter { eq("id", userId) } }

                // Fetch Today's Expenses
                val expensesResponse = SupabaseManager.client.postgrest["expenses"]
                    .select { filter { eq("user_id", userId) } }

                val allExpenses = JSONArray(expensesResponse.data)
                val pendingList = mutableListOf<JSONObject>()
                val confirmedList = mutableListOf<JSONObject>()

                // Split expenses into Pending and Confirmed buckets
                for (i in 0 until allExpenses.length()) {
                    val item = allExpenses.getJSONObject(i)
                    if (item.optBoolean("is_pending", true)) {
                        pendingList.add(item)
                    } else {
                        confirmedList.add(item)
                    }
                }

                // --- DYNAMIC SECTION VISIBILITY ---
                if (pendingList.isEmpty()) {
                    binding.tvActionRequiredLabel.visibility = View.GONE
                    binding.rvPendingExpenses.visibility = View.GONE
                } else {
                    binding.tvActionRequiredLabel.visibility = View.VISIBLE
                    binding.rvPendingExpenses.visibility = View.VISIBLE

                    pendingAdapter = PendingExpenseAdapter(pendingList) {
                        loadDashboardData()
                    }
                    binding.rvPendingExpenses.adapter = pendingAdapter
                }

                // --- RECENT HISTORY ---
                if (confirmedList.isNotEmpty()) {
                    binding.rvRecentHistory.adapter = RecentHistoryAdapter(confirmedList)
                    binding.rvRecentHistory.visibility = View.VISIBLE
                    binding.tvRecentActivityLabel.visibility = View.VISIBLE
                } else {
                    binding.rvRecentHistory.visibility = View.GONE
                    binding.tvRecentActivityLabel.visibility = View.GONE
                }

                // Update UI math
                updateUI(profileResponse.data, confirmedList)

            } catch (e: Exception) {
                Toast.makeText(context, "Sync Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showManualExpenseDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_manual_expense, null)
        val etAmount = dialogView.findViewById<android.widget.EditText>(R.id.etManualAmount)
        val etMerchant = dialogView.findViewById<android.widget.EditText>(R.id.etManualMerchant)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Cash Expense")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
                val merchant = etMerchant.text.toString()
                if (amount > 0) saveManualExpense(amount, merchant)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveManualExpense(amount: Double, merchant: String) {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return

        lifecycleScope.launch {
            try {
                // Build JSON manually to avoid serializer plugin issues
                val jsonObject = kotlinx.serialization.json.buildJsonObject {
                    put("user_id", userId)
                    put("amount", amount)
                    put("merchant", merchant)
                    put("category", "Cash")
                    put("is_pending", false) // Confirmed immediately
                }

                SupabaseManager.client.postgrest["expenses"].insert(jsonObject)
                loadDashboardData() // Refresh everything
                Toast.makeText(context, "Added cash expense!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to sync manual entry", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun updateUI(profileRaw: String, confirmedList: List<JSONObject>) {
        try {
            val profileArray = JSONArray(profileRaw)
            if (profileArray.length() == 0) return

            val profileJson = profileArray.getJSONObject(0)

            // Personalize the Welcome text
            val userName = profileJson.optString("full_name", "User")
            binding.tvWelcome.text = "Hi, $userName"

            val dailyLimit = profileJson.optDouble("daily_limit", 0.0)
            val currentStreak = profileJson.optInt("current_streak", 0)

            var totalSpentToday = 0.0
            for (item in confirmedList) {
                totalSpentToday += item.getDouble("amount")
            }

            val remaining = dailyLimit - totalSpentToday
            binding.tvRemainingAmount.text = "₹${String.format("%.0f", remaining)}"
            binding.tvStreakDisplay.text = "🔥 $currentStreak Day Saving Streak!"

            val progressPercent = if (dailyLimit > 0) {
                ((totalSpentToday / dailyLimit) * 100).toInt().coerceAtMost(100)
            } else 0
            binding.budgetProgressRing.progress = progressPercent

            if (remaining < 0) {
                binding.tvRemainingAmount.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            } else {
                binding.tvRemainingAmount.setTextColor(resources.getColor(com.google.android.material.R.color.design_default_color_primary))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
