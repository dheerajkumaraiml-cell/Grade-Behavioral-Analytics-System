package com.example.financipline

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import java.util.regex.Pattern
import kotlinx.serialization.json.put
class NotificationService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getString("android.text") ?: ""

        // 1. Filter for Payment Apps (UPI, Banks, SMS)
        if (text.contains("Paid") || text.contains("debited") || text.contains("spent")) {
            extractAndSaveExpense(text, title)
        }
    }

    private fun extractAndSaveExpense(message: String, source: String) {
        // 2. Regex to find the amount (e.g., ₹500 or Rs. 500)
        val pattern = Pattern.compile("(?:RS|INR|₹|Rs\\.?)\\s*([\\d,]+(?:\\.\\d{1,2})?)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(message)

        if (matcher.find()) {
            val amount = matcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0
            val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return

            serviceScope.launch {
                try {
                    // Use buildJsonObject to create a proper JSON structure that Postgrest understands
                    val jsonObject = buildJsonObject {
                        put("user_id", userId)
                        put("amount", amount)
                        put("merchant", source)
                        put("raw_text", message)
                        put("is_pending", true)
                        put("category", "Pending")
                    }

                    // Insert the JsonObject directly
                    SupabaseManager.client.postgrest["expenses"].insert(jsonObject)

                    Log.d("EarService", "Successfully saved: ₹$amount")

                } catch (e: Exception) {
                    Log.e("EarService", "Final Insert Error: ${e.message}")
                    // Brutal truth: if this fails, print the whole stack trace to find the culprit
                    e.printStackTrace()
                }
            }
        }
    }
}