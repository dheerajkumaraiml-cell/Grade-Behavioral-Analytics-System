package com.example.financipline

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.financipline.databinding.FragmentAnalyticsBinding
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import org.json.JSONArray

class AnalyticsFragment : Fragment(R.layout.fragment_analytics) {
    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAnalyticsBinding.bind(view)
        loadAnalyticsData()
    }

    private fun loadAnalyticsData() {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return

        lifecycleScope.launch {
            try {
                // Fetch confirmed expenses only
                val response = SupabaseManager.client.postgrest["expenses"]
                    .select {
                        filter {
                            eq("user_id", userId)
                            eq("is_pending", false)
                        }
                    }

                val expenses = JSONArray(response.data)
                val categoryMap = mutableMapOf<String, Float>()

                for (i in 0 until expenses.length()) {
                    val item = expenses.getJSONObject(i)
                    val cat = item.optString("category", "Other")
                    val amt = item.optDouble("amount", 0.0).toFloat()
                    categoryMap[cat] = categoryMap.getOrDefault(cat, 0f) + amt
                }

                if (categoryMap.isNotEmpty()) {
                    // Update Charts
                    setupPieChart(categoryMap)
                    setupBarChart(categoryMap)

                    // Dynamic Insights
                    val topCategory = categoryMap.maxByOrNull { it.value }?.key ?: "Other"
                    binding.tvInsightText.text = "You spent the most on $topCategory today. Keep an eye on your discipline! 🎯"
                } else {
                    binding.tvInsightText.text = "No confirmed data yet. Start categorizing your expenses to see insights! 💡"
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupPieChart(dataMap: Map<String, Float>) {
        val entries = dataMap.map { PieEntry(it.value, it.key) }
        val dataSet = PieDataSet(entries, "").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }

        binding.pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            centerText = "Composition"
            setEntryLabelColor(Color.BLACK)
            animateY(1000)
            invalidate()
        }
    }

    private fun setupBarChart(dataMap: Map<String, Float>) {
        val labels = dataMap.keys.toList()
        val entries = dataMap.values.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }

        val dataSet = BarDataSet(entries, "Expense by Category").apply {
            color = Color.parseColor("#6200EE") // Matching your primary purple
            valueTextSize = 10f
        }

        binding.barChart.apply {
            data = BarData(dataSet)
            description.isEnabled = false

            // Customize X-Axis
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)

            axisRight.isEnabled = false
            animateX(1000)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}