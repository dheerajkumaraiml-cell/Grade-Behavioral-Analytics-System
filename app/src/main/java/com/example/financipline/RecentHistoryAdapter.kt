package com.example.financipline

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.financipline.databinding.ItemRecentHistoryBinding
import org.json.JSONObject

class RecentHistoryAdapter(private val list: List<JSONObject>) :
    RecyclerView.Adapter<RecentHistoryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemRecentHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.tvHistoryMerchant.text = item.optString("merchant", "Expense")
        holder.binding.tvHistoryCategory.text = item.optString("category", "Other")
        holder.binding.tvHistoryAmount.text = "-₹${item.optDouble("amount", 0.0)}"

        // Dynamic Icon Selection based on Category
        val icon = when(item.optString("category").lowercase()) {
            "food" -> android.R.drawable.ic_menu_report_image
            "travel" -> android.R.drawable.ic_menu_directions
            else -> android.R.drawable.ic_menu_agenda
        }
        holder.binding.ivCategoryIcon.setImageResource(icon)
    }

    override fun getItemCount() = list.size
}