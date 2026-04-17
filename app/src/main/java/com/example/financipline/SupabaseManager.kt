package com.example.financipline

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseManager {
    val client = createSupabaseClient(
        supabaseUrl = "https://dxqolqpgijusvpkrcsch.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImR4cW9scXBnaWp1c3Zwa3Jjc2NoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzEwOTY3NjAsImV4cCI6MjA4NjY3Mjc2MH0.t__5G8zRmCqA7w3Be_1jD27-aWyx4qpVNQbieHP6OGY"
    ) {
        install(Auth)
        install(Postgrest)
    }
}