package com.example.dispoahora.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dispoahora.api.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

sealed class AuthState {
    object Loading : AuthState()
    object SignedOut : AuthState()

    data class SignedIn(val userName: String?) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkCurrentSession()
    }

    private fun checkCurrentSession() {
        // Usamos onEach + launchIn para un manejo de flujo más seguro en ViewModels
        supabase.auth.sessionStatus.onEach { status ->

            // ❌ ERROR ANTERIOR: _authState.value = when(status) { ... }
            // ✅ SOLUCIÓN: Usamos 'when' solo para decidir, no para asignar globalmente.

            when (status) {
                is SessionStatus.Authenticated -> {
                    val user = status.session.user
                    if (user != null) {
                        // Esta función YA actualiza el _authState internamente.
                        // Al no haber un "=" antes del when, esto ya no da error.
                        getUserDataAndNavigate(user)
                    }
                }

                is SessionStatus.NotAuthenticated -> {
                    _authState.value = AuthState.SignedOut
                }

                is SessionStatus.Initializing -> {
                    _authState.value = AuthState.Loading
                }

                is SessionStatus.RefreshFailure -> {
                    // Asignación explícita aquí
                    _authState.value = AuthState.Error("Fallo al refrescar token.")
                }
            }
        }.launchIn(viewModelScope)
    }

        private fun getUserDataAndNavigate(user: UserInfo) {
            val metadata = user.userMetadata

            // 1. Intentamos obtener el nombre real de varias claves posibles que usa Google
            // Usamos .jsonPrimitive.contentOrNull para obtener el texto limpio (sin comillas)
            val realName = metadata?.get("full_name")?.jsonPrimitive?.contentOrNull
                ?: metadata?.get("name")?.jsonPrimitive?.contentOrNull
                ?: metadata?.get("given_name")?.jsonPrimitive?.contentOrNull // "given_name" es solo el nombre de pila (ej. Tomás)

            val formattedName = realName
                ?.trim()               // Elimina espacios al inicio/final
                ?.substringBefore(" ") // Toma solo la primera palabra (antes del primer espacio)
                ?.lowercase()          // Convierte "LEONARDO" a "leonardo"
                ?.replaceFirstChar {   // Convierte la 'l' a 'L'
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                }

            Log.d("AuthViewModel", "Usuario formateado: $formattedName")
            _authState.value = AuthState.SignedIn(userName = formattedName)
        }

    fun signInWithGoogleIdToken(idToken: String, rawNonce: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                // Metodo nativo de Supabase usando IDToken
                supabase.auth.signInWith(IDToken) {
                    this.idToken = idToken
                    this.provider = Google
                    this.nonce = rawNonce
                }

                val user = supabase.auth.currentUserOrNull() // <--- En v3 es currentUserOrNull()

                if (user != null) {
                    getUserDataAndNavigate(user)
                } else {
                    _authState.value = AuthState.Error("Sesión iniciada pero no se recuperaron datos de usuario.")
                }
                // Si tiene éxito, el sessionStatus (en init) actualizará el estado a SignedIn
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error en login con Google: ${e.message}")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                // LLAMADA ACTUALIZADA
                supabase.auth.signOut()
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Fallo al cerrar sesión: ${e.message}")
            }
        }
    }
}