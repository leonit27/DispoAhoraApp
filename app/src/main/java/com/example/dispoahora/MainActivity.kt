@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.example.dispoahora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dispoahora.login.AuthViewModel
import coil.compose.AsyncImage

import androidx.compose.material3.Text
import com.example.dispoahora.location.LocationViewModel
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.dispoahora.location.LocationService

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.Duration
import java.time.format.DateTimeParseException

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import com.example.dispoahora.contacts.TextGray
import com.example.dispoahora.supabase.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import java.time.temporal.ChronoUnit

import com.example.dispoahora.utils.*
import com.example.dispoahora.utils.dialogs.AddActivityDialog
import com.mapbox.maps.MapboxExperimental

val PastelBlueTop = Color(0xFFD3E1F0)   // Azul muy pálido, casi blanco
val PastelBlueBottom = Color(0xFFA0B8D7) // Azul lavanda suave
val GradientBackground = Brush.verticalGradient(
    colors = listOf(PastelBlueTop, PastelBlueBottom)
)

// Colores de UI
val CardWhite = Color(0xFFFFFFFF) // Tarjetas blancas limpias
val TextDark = Color(0xFF1F2937)  // Texto principal casi negro
val TextGrayLight = Color(0xFF6B7280) // Texto secundario gris
val AccentBlue = Color(0xFF5B8DEF) // Azul para botones activos (como Café)
val StatusGreenRing = Color(0xFF86EFAC) // Verde pastel brillante para el anillo

val StatusRedRing = Color(0xFFE92C2C)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        MapboxOptions.accessToken = "pk.eyJ1IjoibGVvbmFyZG8yNzA4IiwiYSI6ImNtajdpMzJoNTAwMGUzZHF6Y2sxNHpoYXYifQ.cmOn7gmknDabls4qGOgz4A"

        setContent {
            val authViewModel: AuthViewModel = viewModel()

            MaterialTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = GradientBackground)
                ) {
                    DispoAhoraApp(authViewModel)
                }
            }
        }
    }
}

@Composable
fun Modifier.allowMapGestures(): Modifier {
    val context = LocalView.current
    return this.pointerInput(Unit) {
        awaitEachGesture {
            // 1. Esperar a que el dedo toque la pantalla (ACTION_DOWN)
            // 'requireUnconsumed = false' es clave: detecta el toque aunque Mapbox también lo quiera.
            awaitFirstDown(requireUnconsumed = false)

            // 2. EN ESE INSTANTE, BLOQUEAR AL PADRE
            // Esto busca al padre (el Scroll) y le dice "No interceptes nada"
            context.parent?.requestDisallowInterceptTouchEvent(true)

            // 3. Esperar a que el usuario levante el dedo o cancele
            waitForUpOrCancellation()

            // 4. DESBLOQUEAR AL PADRE
            // Cuando levantas el dedo, el scroll vuelve a funcionar
            context.parent?.requestDisallowInterceptTouchEvent(false)
        }
    }
}

@OptIn(MapboxExperimental::class)
@Composable
fun ContactsMapCard() {
    // Estado inicial de la cámara (Madrid por defecto, luego usaremos GPS real)
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(14.0)
            center(Point.fromLngLat(-3.7038, 40.4168))
            pitch(0.0)
        }
    }

    SectionTitle("CONTACTOS CERCA")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF5B8DEF).copy(alpha = 0.1f))
            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(16.dp))
                .allowMapGestures()
        ) {
            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                mapViewportState = mapViewportState,
                style = {
                    MapStyle(style = "mapbox://styles/mapbox/light-v11")
                }
            ) {
                // AQUÍ IRÁN LOS PINES (MARCADORES) MÁS ADELANTE
            }

            // Opcional: Un pequeño botón o texto flotante sobre el mapa
            // Text("Mapa en vivo", Modifier.align(Alignment.TopEnd).padding(8.dp), fontSize = 10.sp)
        }
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun DispoAhoraScreen(username: String?, avatarUrl: String?, onOpenProfile: () -> Unit) {
    var isMapInteracting by remember { mutableStateOf(false) }
    val locationViewModel: LocationViewModel = viewModel()

    val currentUser = remember { supabase.auth.currentUserOrNull() }
    val myUserId = currentUser?.id ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState(), enabled = !isMapInteracting)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        HeaderProfileSection(username, avatarUrl, onOpenProfile, locationViewModel)

        Spacer(modifier = Modifier.height(20.dp))
        MainStatusCard(myUserId = myUserId)
        Spacer(modifier = Modifier.height(20.dp))
        ActivitySection()
        Spacer(modifier = Modifier.height(20.dp))
        ContactsMapCard()
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun MainStatusCard(
    myUserId: String
) {
    var isLibre by remember { mutableStateOf(true) }
    var expiresAt by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    val textLibreOcupado = if (isLibre) "Ocupado" else "Libre"

    LaunchedEffect(myUserId) {
        if (myUserId.isNotBlank()) {
            try {
                val profile = supabase.from("profiles")
                    .select {
                        filter { eq("id", myUserId) }
                    }.decodeSingle<Map<String, String?>>()

                val statusEnBD = profile["status"]
                val expiryEnBD = profile["status_expires_at"]

                if (statusEnBD == "Libre" && expiryEnBD != null) {
                    val ahora = Instant.now()
                    val expiracion = Instant.parse(expiryEnBD)

                    if (expiracion.isAfter(ahora)) {
                        isLibre = true
                        expiresAt = expiryEnBD
                    } else {
                        isLibre = false
                        expiresAt = null
                    }
                } else {
                    isLibre = statusEnBD == "Libre"
                    expiresAt = expiryEnBD
                }
            } catch (e: Exception) {
                android.util.Log.e("DISPO_LOAD", "Error cargando estado: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    if (isLoading) {
        return
    }

    SectionTitle("TU ESTADO AHORA")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF5B8DEF).copy(alpha = 0.15f))
            .background(CardWhite, RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color(0xFFEFF6FF), RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Box(modifier = Modifier.size(6.dp).background(AccentBlue, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))

            if (isLibre && expiresAt != null) {
                CountdownDisplay(
                    expirationString = expiresAt!!,
                    color = AccentBlue
                )
            } else {
                Text("Visible solo por 1 hora", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Avisamos a tus\n contactos que estás\n disponible.",
                    color = TextGrayLight,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            StatusRingButton(
                isLibre = isLibre,
                onToggle = {
                    val newState = !isLibre
                    isLibre = newState

                    scope.launch {
                        android.util.Log.d("DISPO_DEBUG", "Intentando actualizar. ID Usuario: '$myUserId'")

                        try {
                            if (newState) {
                                val newExpiryTime = Instant.now()
                                    .plus(1, ChronoUnit.HOURS)
                                    .toString()

                                expiresAt = newExpiryTime

                                android.util.Log.d("DISPO_DEBUG", "Enviando estado LIBRE hasta: $newExpiryTime")

                                supabase.from("profiles").update(
                                    mapOf(
                                        "status" to "Libre",
                                        "status_expires_at" to newExpiryTime
                                    )
                                ) {
                                    filter {
                                        eq("id", myUserId)
                                    }
                                }

                            } else {
                                expiresAt = null
                                android.util.Log.d("DISPO_DEBUG", "Enviando estado OCUPADO")

                                supabase.from("profiles").update(
                                    mapOf(
                                        "status" to "Ocupado",
                                        "status_expires_at" to null
                                    )
                                ) {
                                    filter {
                                        eq("id", myUserId)
                                    }
                                }

                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            isLibre = !newState
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Toca para cambiar a \"$textLibreOcupado\"",
            color = TextGrayLight,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun CountdownDisplay(
    expirationString: String,
    color: Color
) {
    var timeLeft by remember { mutableStateOf("Calculando...") }

    LaunchedEffect(expirationString) {
        try {
            val expiresAt = Instant.parse(expirationString)
            while (true) {
                val now = Instant.now()
                val diff = Duration.between(now, expiresAt)

                if (diff.isNegative || diff.isZero) {
                    timeLeft = "00:00:00"
                    break
                } else {
                    val hours = diff.toHours()
                    val minutes = diff.toMinutesPart()
                    val seconds = diff.toSecondsPart()
                    timeLeft = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                }
                delay(1000L)
            }
        } catch (_: DateTimeParseException) {
            timeLeft = "Error fecha"
        }
    }

    Text(
        text = "Libre por: $timeLeft",
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold
    )
}