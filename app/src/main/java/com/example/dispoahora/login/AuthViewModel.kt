package com.example.dispoahora.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dispoahora.contacts.ContactModel
import com.example.dispoahora.supabase.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject

import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

sealed class AuthState {
    object Loading: AuthState()
    object SignedOut: AuthState()

    data class SignedIn(val userName: String?, val avatarUrl: String?, val email: String?): AuthState()
    data class Error(val message: String): AuthState()
}

data class UserStats(
    val followers: Int = 0,
    val following: Int = 0,
    val cercanos: Int = 0
)

class AuthViewModel: ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    private val _userStats = MutableStateFlow(UserStats())
    val userStats: StateFlow<UserStats> = _userStats.asStateFlow()

    private val _realUsers = MutableStateFlow<List<ContactModel>>(emptyList())
    val realUsers: StateFlow<List<ContactModel>> = _realUsers.asStateFlow()

    private val _nearbyUsers = MutableStateFlow<List<ContactModel>>(emptyList())
    val nearbyUsers: StateFlow<List<ContactModel>> = _nearbyUsers.asStateFlow()

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

        val userEmail = user.email

        fetchUserStats(user.id)

        _authState.value = AuthState.SignedIn(userName = formattedName, avatarUrl = avatarUrl, email = userEmail)
    }

    fun fetchUserStats(userId: String) {
        viewModelScope.launch {
            try {
                val followersResult = supabase.from("user_follows").select {
                    filter { eq("following_id", userId) }
                    count(Count.EXACT)
                }
                val followersCount = followersResult.countOrNull() ?: 0L

                val followingResult = supabase.from("user_follows").select {
                    filter { eq("follower_id", userId) }
                    count(Count.EXACT)
                }
                val followingCount = followingResult.countOrNull() ?: 0L

                val circleResult = supabase.from("relationships").select {
                    filter {
                        and {
                            or {
                                eq("requester_id", userId)
                                eq("receiver_id", userId)
                            }
                            eq("status", "accepted")
                        }
                    }
                    count(Count.EXACT)
                }
                val circleCount = circleResult.countOrNull() ?: 0L

                _userStats.value = UserStats(
                    followers = followersCount.toInt(),
                    following = followingCount.toInt(),
                    cercanos = circleCount.toInt()
                )

            } catch (e: Exception) {
                android.util.Log.e("STATS_ERROR", "Error: ${e.message}")
            }
        }
    }

    fun fetchContacts() {
        viewModelScope.launch {
            try {
                val result = supabase.from("profiles")
                    .select {
                        limit(3)
                    }
                    .decodeList<ContactModel>()

                _realUsers.value = result

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun searchContacts(query: String) {
        if (query.isBlank()) {
            fetchContacts()
            return
        }

        viewModelScope.launch {
            try {
                val result = supabase.from("profiles")
                    .select {
                        filter { ilike("full_name", "%$query%") }
                    }
                    .decodeList<ContactModel>()

                _realUsers.value = result
            } catch (e: Exception) {
            }
        }
    }

    fun followUser(targetUserId: String) {
        val myUserId = supabase.auth.currentUserOrNull()?.id ?: return

        if (myUserId == targetUserId) return

        viewModelScope.launch {
            try {
                supabase.from("user_follows").insert(
                    mapOf(
                        "follower_id" to myUserId,
                        "following_id" to targetUserId
                    )
                )

                fetchUserStats(myUserId)

            } catch (_: Exception) {
            }
        }
    }

    fun updateLocationInDB(latitude: Double?, longitude: Double?, isLibre: Boolean) {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return

        viewModelScope.launch {
            try {
                val pointWKT = if (isLibre && latitude != null && longitude != null) {
                    "POINT($longitude $latitude)"
                } else {
                    null
                }

                supabase.from("profiles").update(
                    mapOf("location" to pointWKT)
                ) {
                    filter { eq("id", userId) }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun fetchNearbyUsers(lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                val response = supabase.postgrest.rpc(
                    "get_nearby_users",
                    mapOf(
                        "lat" to lat,
                        "lng" to lng,
                        "radius_meters" to 5000.0
                    )
                ).decodeList<ContactModel>()

                val myId = supabase.auth.currentUserOrNull()?.id
                _nearbyUsers.value = response.filter { it.id != myId && it.status == "Libre" }

            } catch (_: Exception) {
            }
        }
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

    fun signInWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email y contraseña son obligatorios")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }

                val user = supabase.auth.currentUserOrNull()

                if (user != null) {
                    getUserDataAndNavigate(user)
                } else {
                    _authState.value = AuthState.Error("No se pudieron obtener los datos del usuario.")
                }

            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Error al iniciar sesión")
            }
        }
    }

    fun signUpWithEmail(email: String, password: String, fullName: String) {
        if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
            _authState.value = AuthState.Error("Todos los campos son obligatorios")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    data = buildJsonObject {
                        put("full_name", fullName)
                    }
                }

                val user = supabase.auth.currentUserOrNull()
                if (user != null) {
                    getUserDataAndNavigate(user)
                } else {
                    _authState.value = AuthState.SignedOut
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Error al registrarse")
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