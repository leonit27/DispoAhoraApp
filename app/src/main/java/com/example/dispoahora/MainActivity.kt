package com.example.dispoahora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.example.dispoahora.contacts.TextGray

import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.Duration
import java.time.format.DateTimeParseException

import androidx.compose.runtime.rememberCoroutineScope
import com.example.dispoahora.supabase.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from // Asegúrate de tener los imports de Supabase
import kotlinx.coroutines.launch
import java.time.temporal.ChronoUnit

import com.example.dispoahora.utils.*

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
val StatusGreenText = Color(0xFF10B981)
val DarkAlertBg = Color(0xFF282B46) // Mantenemos la alerta oscura para contraste

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

@Composable
fun ContactsMapCard(onMapInteraction: (Boolean) -> Unit = {}) {
    // Estado inicial de la cámara (Madrid por defecto, luego usaremos GPS real)
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(14.0) // Zoom cercano tipo calle
            center(Point.fromLngLat(-3.7038, 40.4168))
            pitch(0.0) // Vista cenital (desde arriba)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF5B8DEF).copy(alpha = 0.1f))
            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Contactos cerca", color = Color(0xFF1F2937), fontWeight = FontWeight.Bold)
                Text("Explora quién está libre a tu alrededor", color = Color(0xFF6B7280), fontSize = 11.sp)
            }
        }
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
        QuickActivitySection()
        Spacer(modifier = Modifier.height(20.dp))
        ContactsMapCard(
            onMapInteraction = { isInteracting ->
                isMapInteracting = isInteracting
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun HeaderProfileSection(
    username: String?,
    avatarUrl: String? = null,
    onOpenProfile: () -> Unit,
    locationViewModel: LocationViewModel
) {
    val context = LocalContext.current

    val currentAddress by locationViewModel.currentAddress.collectAsState()
    val isLoading by locationViewModel.isLoading.collectAsState()
    var showLocationDialog by remember { mutableStateOf(false) }

    val locationService = remember { LocationService(context) }

    val gpsSettingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            locationViewModel.detectLocation()
            showLocationDialog = false
        } else {
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            locationService.checkLocationSettings(
                onEnabled = {
                    locationViewModel.detectLocation()
                    showLocationDialog = false
                },
                onDisabled = { exception ->
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    gpsSettingLauncher.launch(intentSenderRequest)
                }
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hola, ${username}!",
                color = TextDark,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showLocationDialog = true }
                    .padding(vertical = 4.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Ubicación",
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                if (isLoading) {
                    Text("Detectando...", fontSize = 14.sp, color = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.width(6.dp))
                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = currentAddress,
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Cambiar",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }


        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable { onOpenProfile() }
                .background(Color(0xFFE5E7EB)),
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = username?.take(1)?.uppercase() ?: "U",
                    color = TextGrayLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showLocationDialog) {
        LocationSelectionDialog(
            onDismiss = { showLocationDialog = false },
            onAutoDetect = {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            onManualEntry = { text ->
                locationViewModel.setManualLocation(text)
                showLocationDialog = false
            }
        )
    }
}

@Composable
fun LocationSelectionDialog(
    onDismiss: () -> Unit,
    onAutoDetect: () -> Unit,
    onManualEntry: (String) -> Unit
) {
    var manualText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar ubicación") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = onAutoDetect,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0F2FE))
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF0284C7))
                    Spacer(Modifier.width(8.dp))
                    Text("Usar ubicación actual (GPS)", color = Color(0xFF0284C7))
                }

                Text("- O escribe una ciudad -", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))

                TextField(
                    value = manualText,
                    onValueChange = { manualText = it },
                    placeholder = { Text("Ej: Madrid, Spain") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (manualText.isNotBlank()) onManualEntry(manualText)
            }) {
                Text("Establecer Manual")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
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

    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp, start = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("TU ESTADO AHORA", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }

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
                    isLibre = newState // 1. Actualizamos UI visualmente al instante (Optimistic UI)

                    scope.launch {
                        android.util.Log.d("DISPO_DEBUG", "Intentando actualizar. ID Usuario: '$myUserId'")

                        try {
                            if (newState) {
                                // CASO: SE PONE LIBRE
                                // Calculamos la hora: Ahora + 1 hora
                                val newExpiryTime = java.time.Instant.now()
                                    .plus(1, ChronoUnit.HOURS)
                                    .toString()

                                expiresAt = newExpiryTime // Actualizamos local

                                android.util.Log.d("DISPO_DEBUG", "Enviando estado LIBRE hasta: $newExpiryTime")

                                // GUARDAMOS EN SUPABASE
                                supabase.from("profiles").update(
                                    mapOf(
                                        "status" to "Libre",
                                        "status_expires_at" to newExpiryTime
                                    )
                                ) {
                                    filter {
                                        eq("id", myUserId) // Solo actualiza MI usuario
                                    }
                                }

                            } else {
                                // CASO: SE PONE OCUPADO
                                expiresAt = null // Borramos local
                                android.util.Log.d("DISPO_DEBUG", "Enviando estado OCUPADO")

                                // GUARDAMOS EN SUPABASE
                                supabase.from("profiles").update(
                                    mapOf(
                                        "status" to "Ocupado",
                                        "status_expires_at" to null // Borramos fecha en BD
                                    )
                                ) {
                                    filter {
                                        eq("id", myUserId)
                                    }
                                }

                            }
                            android.util.Log.d("DISPO_DEBUG", "¡Actualización ÉXITOSA en Supabase!")
                        } catch (e: Exception) {
                            // Si falla internet, aquí podrías revertir el estado visual
                            android.util.Log.e("DISPO_ERROR", "Error al actualizar: ${e.message}")
                            e.printStackTrace()
                            isLibre = !newState // Revertir cambio si falló
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
        } catch (e: DateTimeParseException) {
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

@Composable
fun QuickActivitySection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF5B8DEF).copy(alpha = 0.1f))
            .background(CardWhite.copy(alpha = 0.95f), RoundedCornerShape(24.dp))
            .padding(vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Actividad rápida", color = TextDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Elige qué te apetece ahora", color = TextGrayLight, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ActivityChip(icon = Icons.Default.ShoppingCart, text = "Café", isSelected = true)
            }
            items(listOf("Deporte", "Cena", "Chat")) { activity ->
                ActivityChip(icon = Icons.Default.PlayArrow, text = activity, isSelected = false)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(4.dp)
                .background(Color(0xFFF3F4F6), RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(4.dp)
                    .background(Color(0xFF9CA3AF), RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun ActivityChip(icon: ImageVector, text: String, isSelected: Boolean) {
    val bgColor = if (isSelected) AccentBlue else Color(0xFFF3F4F6)
    val contentColor = if (isSelected) Color.White else TextDark

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun CustomBottomBar(
    onProfileClick: () -> Unit = {},
    onContactsClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .shadow(10.dp, RoundedCornerShape(50), spotColor = Color.Black.copy(alpha = 0.05f))
            .background(Color.White, RoundedCornerShape(50))
            .height(65.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.Home, contentDescription = null, tint = TextGrayLight, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Estado", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onContactsClick() }) {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = TextGrayLight, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(2.dp))
                Text("Contactos", color = TextGrayLight, fontSize = 10.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onProfileClick() }) {
                Icon(Icons.Outlined.Settings, contentDescription = null, tint = TextGrayLight, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(2.dp))
                Text("Ajustes", color = TextGrayLight, fontSize = 10.sp)
            }
        }
    }
}