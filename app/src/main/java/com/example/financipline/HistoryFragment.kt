package com.example.financipline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financipline.databinding.FragmentHistoryBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvFullHistory.layoutManager = LinearLayoutManager(requireContext())
        loadFullHistory()
    }

    private fun loadFullHistory() {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return

        lifecycleScope.launch {
            try {
                // Fetch only confirmed expenses
                val response = SupabaseManager.client.postgrest["expenses"]
                    .select {
                        filter {
                            eq("user_id", userId)
                            eq("is_pending", false)
                        }
                    }

                val historyList = mutableListOf<JSONObject>()
                val jsonArray = JSONArray(response.data)
                for (i in 0 until jsonArray.length()) {
                    historyList.add(jsonArray.getJSONObject(i))
                }

                // Reuse the adapter we built for the Dashboard
                binding.rvFullHistory.adapter = RecentHistoryAdapter(historyList)

            } catch (e: Exception) {
                Toast.makeText(context, "Error loading history", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}