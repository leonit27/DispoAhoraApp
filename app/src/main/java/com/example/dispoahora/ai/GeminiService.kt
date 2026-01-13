package com.example.dispoahora.ai

import com.example.dispoahora.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class GeminiService {
    private val client = OkHttpClient()
    private val apiKey = BuildConfig.API_KEY
    private val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=$apiKey"

    suspend fun suggestActivity(contexto: String): String? = withContext(Dispatchers.IO) {
        val prompt = "Eres un asistente de red social. El usuario está en: $contexto. Sugiere una actividad de máximo 2 palabras. Solo el texto, sin comillas."

        val json = JSONObject().apply {
            put("contents", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        return@withContext try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null

                val responseBody = response.body?.string()
                val jsonResponse = JSONObject(responseBody ?: "")

                jsonResponse.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text").trim()
            }
        } catch (e: Exception) {
            android.util.Log.e("GEMINI_ERROR", "Error: ${e.message}")
            null
        }
    }
}