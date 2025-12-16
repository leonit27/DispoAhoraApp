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
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import android.app.Activity
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.dispoahora.location.LocationService

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
fun ContactsMapCard() {
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
            // Estilo idéntico a tus otras Cards
            .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF5B8DEF).copy(alpha = 0.1f))
            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
            .padding(16.dp) // Padding interno de la tarjeta
    ) {
        // --- Título de la Sección ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Column {
                Text("Contactos cerca", color = Color(0xFF1F2937), fontWeight = FontWeight.Bold)
                Text("Explora quién está libre a tu alrededor", color = Color(0xFF6B7280), fontSize = 11.sp)
            }
        }

        // --- El Mapa ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp) // Altura fija para el mapa
                .clip(RoundedCornerShape(16.dp)) // Redondeamos el mapa para que quede bonito
        ) {
            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                mapViewportState = mapViewportState,
                style = {
                    // Usamos el estilo Light porque combina mejor con tu app pastel
                    // También puedes usar: MapboxStandardStyle.LIGHT
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

@Composable
fun DispoAhoraScreen(username: String?, avatarUrl: String?, onOpenProfile: () -> Unit) {
    val locationViewModel: LocationViewModel = viewModel()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                HeaderProfileSection(username, avatarUrl, onOpenProfile, locationViewModel)

                Spacer(modifier = Modifier.height(24.dp))
                AlertBanner()
                Spacer(modifier = Modifier.height(20.dp))
                MainStatusCard()
                Spacer(modifier = Modifier.height(20.dp))
                QuickActivitySection()
                Spacer(modifier = Modifier.height(20.dp))
                ContactsMapCard()
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

@Composable
fun HeaderProfileSection(username: String?, avatarUrl: String? = null, onOpenProfile: () -> Unit, locationViewModel: LocationViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val currentAddress by locationViewModel.currentAddress.collectAsState()
    val isLoading by locationViewModel.isLoading.collectAsState()
    var showLocationDialog by remember { mutableStateOf(false) }

    val locationService = remember { LocationService(context) }

    val gpsSettingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // El usuario le dio a "ACEPTAR" -> Detectamos ubicación
            locationViewModel.detectLocation()
            showLocationDialog = false
        } else {
            // El usuario le dio a "NO, GRACIAS" -> No hacemos nada o mostramos error
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            // Si nos da permiso, AHORA chequeamos si el GPS está encendido
            locationService.checkLocationSettings(
                onEnabled = {
                    locationViewModel.detectLocation()
                    showLocationDialog = false
                },
                onDisabled = { exception ->
                    // Lanzamos el diálogo del sistema
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
                    .clickable { showLocationDialog = true } // Abre el diálogo
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
                // Pedimos permisos. Si ya los tiene, el launcher ejecuta el callback inmediatamente.
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
                // Opción 1: Automática
                Button(
                    onClick = onAutoDetect,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0F2FE)) // Azul muy clarito
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF0284C7))
                    Spacer(Modifier.width(8.dp))
                    Text("Usar ubicación actual (GPS)", color = Color(0xFF0284C7))
                }

                Text("- O escribe una ciudad -", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))

                // Opción 2: Manual
                TextField(
                    value = manualText,
                    onValueChange = { manualText = it },
                    placeholder = { Text("Ej: Madrid, Centro") },
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
fun AlertBanner() {
    // La alerta se mantiene oscura para destacar sobre el fondo pastel, tal como en la imagen de referencia
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.1f))
            .background(DarkAlertBg, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            // Icono rayo
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.Gray.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "¡Estás cerca y Ana también está libre!",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 16.sp
            )
        }
        Text(
            text = "Ir al chat",
            color = Color(0xFF60A5FA), // Azul claro brillante
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun MainStatusCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF5B8DEF).copy(alpha = 0.15f))
            .background(CardWhite, RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TU ESTADO AHORA", color = TextGrayLight, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

            // Tag Visible
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFFEFF6FF), RoundedCornerShape(20.dp)) // Azul muy muy claro
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Box(modifier = Modifier.size(6.dp).background(AccentBlue, CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Visible solo por 1 hora", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Textos
            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    text = "Libre ahora",
//                    color = TextDark,
//                    fontSize = 28.sp,
//                    fontWeight = FontWeight.ExtraBold
//                )
//                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Avisamos a tus contactos de confianza que estás disponible.",
                    color = TextGrayLight,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            val infiniteTransition = rememberInfiniteTransition(label = "anillo_animacion")
            val angle by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    // durationMillis: Cuanto más bajo, más rápido gira (3000ms = 3 segundos por vuelta)
                    animation = tween(durationMillis = 3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "angulo_rotacion"
            )

// Anillo de Estado
            Box(contentAlignment = Alignment.Center) {

                // Degradado del anillo (ANIMADO)
                Canvas(
                    modifier = Modifier
                        .size(90.dp)
                        .rotate(angle)
                ) {
                    val brush = Brush.sweepGradient(
                        // Nota: Es importante que el primer y el último color sean iguales
                        // para que no se note el corte al girar.
                        colors = listOf(StatusGreenRing, Color(0xFF3B82F6), StatusGreenRing)
                    )
                    drawCircle(
                        brush = brush,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Interior oscuro para contraste (ESTÁTICO - No rota)
                // Al estar fuera del Canvas que rotamos, el texto se mantiene legible.
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(Color(0xFF0F172A), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ESTADO", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("Libre", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Toca para cambiar a \"Ocupado\"",
            color = TextGrayLight,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
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
                // Café seleccionado (Azul)
                ActivityChip(icon = Icons.Default.ShoppingCart, text = "Café", isSelected = true)
            }
            items(listOf("Deporte", "Cena", "Chat")) { activity ->
                ActivityChip(icon = Icons.Default.PlayArrow, text = activity, isSelected = false)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scrollbar visual gris clara
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

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Solo verán este estado las personas que tú eliges.",
            color = TextGrayLight,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}

@Composable
fun ActivityChip(icon: ImageVector, text: String, isSelected: Boolean) {
    // Si está seleccionado: Fondo Azul, Texto Blanco
    // Si no: Fondo Gris muy claro, Texto Gris oscuro
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
fun ContactItem(
    initial: String,
    name: String,
    activity: String,
    status: String,
    distanceText: String,
    distanceBg: Color,
    distanceColor: Color,
    isOnline: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFDBEAFE), CircleShape), // Fondo avatar azul claro
                contentAlignment = Alignment.Center
            ) {
                Text(initial, fontWeight = FontWeight.Bold, color = AccentBlue)
            }
            if (isOnline) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(AccentBlue, CircleShape) // Punto azul para indicar online
                        .border(2.dp, CardWhite, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, color = TextDark, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                // Tag Libre
                Box(
                    modifier = Modifier
                        .background(Color(0xFFDCFCE7), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(status, color = StatusGreenText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (activity.isNotEmpty()) {
                Text(activity, color = TextGrayLight, fontSize = 12.sp)
            }
        }

        // Chip de Distancia
        Box(
            modifier = Modifier
                .background(distanceBg, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(distanceText, color = distanceColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                if(isOnline) {
                    Text("En tu misma zona", color = TextGrayLight, fontSize = 8.sp)
                }
            }
        }
    }
}

@Composable
fun CustomBottomBar(
    onProfileClick: () -> Unit = {}
) {
    // Barra flotante con esquinas redondeadas, fondo blanco
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
            // Item 1: Estado (Activo)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(6.dp).background(AccentBlue, CircleShape))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Estado", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Item 2: Contactos
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = TextGrayLight, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(2.dp))
                Text("Contactos", color = TextGrayLight, fontSize = 10.sp)
            }

            // Item 3: Ajustes (Nuevo)
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onProfileClick() }) {
                Icon(Icons.Outlined.Settings, contentDescription = null, tint = TextGrayLight, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(2.dp))
                Text("Ajustes", color = TextGrayLight, fontSize = 10.sp)
            }
        }
    }
}