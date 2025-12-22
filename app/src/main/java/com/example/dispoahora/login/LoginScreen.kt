package com.example.dispoahora.login

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID
import com.example.dispoahora.BuildConfig

const val WEB_GOOGLE_CLIENT_ID = BuildConfig.WEB_GOOGLE_CLIENT_ID

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0B0F19)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "DISPOAHORA",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Encuentros espontáneos,\nsin fatiga de planificación.",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = {
                        performGoogleSignIn(context, coroutineScope, authViewModel)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    // Aquí podrías añadir un Icono de Google si lo tienes en tus recursos
                    /* Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Logo Google",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(12.dp)) */

                    Text(
                        text = "Continuar con Google",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Al registrarte, aceptas nuestros términos y políticas de privacidad.",
                    color = Color.Gray.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

private fun performGoogleSignIn(
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    viewModel: AuthViewModel
) {
    val credentialManager = CredentialManager.create(context)

    // 1. Generar Nonce y hashearlo (Requisito de seguridad de Supabase + Google)
    // Supabase necesita el 'rawNonce' y Google necesita el 'hashedNonce'.
    val rawNonce = UUID.randomUUID().toString()
    val bytes = rawNonce.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

    // 2. Configurar la petición de Google ID
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false) // false para permitir elegir cualquier cuenta
        .setServerClientId(WEB_GOOGLE_CLIENT_ID) // Usar el ID de Cliente Web
        .setNonce(hashedNonce)
        .build()

    // 3. Crear la solicitud de credenciales
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    scope.launch {
        try {
            // 4. Lanzar el selector de cuentas nativo de Android
            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )

            // 5. Procesar la credencial recibida
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val googleIdToken = googleIdTokenCredential.idToken

            // 6. Enviar el token y el nonce crudo a Supabase para iniciar sesión
            Log.d("Login", "Google ID Token recibido. Iniciando sesión en Supabase...")
            viewModel.signInWithGoogleIdToken(googleIdToken, rawNonce)

        } catch (e: GetCredentialException) {
            Log.e("Login", "Error en Credential Manager: ${e.message}")
            // Aquí podrías mostrar un Snackbar o Toast al usuario si falla
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("Login", "Error al parsear el token de Google: ${e.message}")
        } catch (e: Exception) {
            Log.e("Login", "Error desconocido durante el login: ${e.message}")
        }
    }
}