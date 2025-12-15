package com.example.dispoahora.login

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
    object Loading: AuthState()
    object SignedOut: AuthState()

    data class SignedIn(val userName: String?, val avatarUrl: String?): AuthState()
    data class Error(val message: String): AuthState()
}

class AuthViewModel: ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkCurrentSession()
    }

    private fun checkCurrentSession() {
        supabase.auth.sessionStatus.onEach { status ->

            when (status) {
                is SessionStatus.Authenticated -> {
                    val user = status.session.user
                    if (user != null) {
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
                    _authState.value = AuthState.Error("Fallo al refrescar token.")
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun getUserDataAndNavigate(user: UserInfo) {
        val metadata = user.userMetadata

        /* Según el proveedor de las identidades, la clave recibida tiene diferente nombre:
           - full_name: habitual en Supabase o Email
           - name: estándar utilizado por OAuth de Google
           - given_name: mandado también por Google por si no está el nombre completo
           Por tanto, vamos comprobando las opciones con el operador (?:) hasta dar con la correcta,
           utilizando .jsonPrimitive.contentOrNull para obtener el texto sin comillas */

        val realName = metadata?.get("full_name")?.jsonPrimitive?.contentOrNull
            ?: metadata?.get("name")?.jsonPrimitive?.contentOrNull
            ?: metadata?.get("given_name")?.jsonPrimitive?.contentOrNull

        /* Le damos formato para que únicamente se muestre el primer nombre con
           la primera letra en mayúscula y el resto en minúsculas */

        val formattedName = realName
            ?.trim()
            ?.substringBefore(" ")
            ?.lowercase()

            /* Reemplazamos el primer carácter de la String, primero combrobando si es LowerCase
               (ya cambiado anteriormente):
               - Si es LowerCase: cambiamos el carácter a mayúsculas con .titlecase(), que
                                  lo devuelve como String
               - Si no es LowerCase (por ejemplo, algún número u otro carácter):
                                  pasamos el carácter como String para mantener el tipo
                                  de dato */
            ?.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }

        /* - avatar_url: utilizado por Github y Supabase
           - picture: estándar que utiliza Google para la imagen de perfil
           Comprobamos ambos para asegurarnos de que exista la foto */

        val avatarUrl = metadata?.get("avatar_url")?.jsonPrimitive?.contentOrNull
            ?: metadata?.get("picture")?.jsonPrimitive?.contentOrNull

        _authState.value = AuthState.SignedIn(userName = formattedName, avatarUrl = avatarUrl)
    }

    fun signInWithGoogleIdToken(idToken: String, rawNonce: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                supabase.auth.signInWith(IDToken) {
                    this.idToken = idToken
                    this.provider = Google
                    this.nonce = rawNonce
                }

                val user = supabase.auth.currentUserOrNull()

                if (user != null) {
                    getUserDataAndNavigate(user)
                } else {
                    _authState.value = AuthState.Error("Sesión iniciada pero no se recuperaron datos de usuario.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error en login con Google: ${e.message}")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                /* Con esta función se borra el Access Token y el Refresh Token
                   que están guardados en el móvil y "olvida" el usuario */
                supabase.auth.signOut()
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Fallo al cerrar sesión: ${e.message}")
            }
        }
    }
}