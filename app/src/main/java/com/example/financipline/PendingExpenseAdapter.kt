package com.example.financipline

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.financipline.databinding.ItemPendingExpenseBinding
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
class PendingExpenseAdapter(
    private var list: MutableList<JSONObject>,
    private val onConfirm: () -> Unit
) : RecyclerView.Adapter<PendingExpenseAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPendingExpenseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPendingExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val id = item.getString("id")

        holder.binding.tvPendingAmount.text = "₹${item.getDouble("amount")}"
        holder.binding.tvPendingSource.text = "From: ${item.optString("merchant", "Notification")}"

        holder.binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    R.id.chipFood -> updateExpense(id, "Food")
                    R.id.chipTravel -> updateExpense(id, "Travel")
                    R.id.chipShopping -> updateExpense(id, "Bills")
                    R.id.chipOther -> showCustomCategoryDialog(id,holder.itemView.context) // 🚀 Trigger Dialog
                }
            }
        }
    }
    // Pass the context into the function
    private fun showCustomCategoryDialog(id: String, context: android.content.Context) {
        val editText = android.widget.EditText(context)
        editText.hint = "e.g., Lent to Raj, Charity, etc."

        android.app.AlertDialog.Builder(context)
            .setTitle("Custom Category")
            .setMessage("Where did this money go?")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val customCat = editText.text.toString()
                if (customCat.isNotEmpty()) {
                    updateExpense(id, customCat)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun updateExpense(id: String, category: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Build a JsonObject instead of a Map to avoid the 'Any' serializer error
                val updateData = buildJsonObject {
                    put("is_pending", false)
                    put("category", category)
                }

                // Perform the update using the JsonObject
                SupabaseManager.client.postgrest["expenses"].update(updateData) {
                    filter { eq("id", id) }
                }

                // Refresh dashboard math on main thread
                withContext(Dispatchers.Main) {
                    onConfirm()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount() = list.size
}