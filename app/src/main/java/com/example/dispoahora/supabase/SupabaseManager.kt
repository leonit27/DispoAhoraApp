package com.example.dispoahora.supabase

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import com.example.dispoahora.BuildConfig

private const val SUPABASE_URL = BuildConfig.SUPABASE_URL
private const val SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY

val supabase = createSupabaseClient(
    supabaseUrl = SUPABASE_URL,
    supabaseKey = SUPABASE_ANON_KEY
) {
    install(Auth)
    install(Postgrest)
}