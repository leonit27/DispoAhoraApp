package com.example.dispoahora.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dispoahora.PastelBlueBottom
import com.example.dispoahora.PastelBlueTop

// --- TUS COLORES PASTEL ---
val GradientBackground = Brush.verticalGradient(
    colors = listOf(PastelBlueTop, PastelBlueBottom)
)
val CardBackground = Color.White.copy(alpha = 0.9f)
val TextDark = Color(0xFF1F2937)
val TextGray = Color(0xFF6B7280)
val StatusGreen = Color(0xFF10B981)
val BadgeGreenBg = Color(0xFFD1FAE5)

@Composable
fun ContactsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GradientBackground) // El fondo degradado de tu app
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // 1. TÍTULO
        Text(
            text = "Contactos",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "Gestiona quién puede verte y quién ve tu estado.",
            fontSize = 14.sp,
            color = TextGray,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // 2. BARRA DE BÚSQUEDA Y FILTRO
        SearchBarSection()

        Spacer(modifier = Modifier.height(24.dp))

        // 3. CARD DE FAVORITOS (Bloque contenedor)
        ContactGroupCard(
            title = "Favoritos",
            count = "3 activos",
            contacts = getFavoritesList()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 4. CARD DE CERCA DE TI (Bloque contenedor)
        ContactGroupCard(
            title = "Cerca de ti",
            count = "5 alrededor",
            contacts = getNearbyList()
        )

        Spacer(modifier = Modifier.height(100.dp)) // Espacio final para la BottomBar
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun SearchBarSection() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Campo de texto
        TextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Buscar nombre o alias", color = TextGray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGray) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .weight(1f)
                .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f))
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Botón de filtro (Verdesito pastel)
        Button(
            onClick = { /* Acción filtro */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4F5BE)), // Verde lima pastel
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .size(56.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f))
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Filtro",
                tint = Color(0xFF3F6212) // Verde oscuro texto
            )
        }
    }
}

@Composable
fun ContactGroupCard(
    title: String,
    count: String,
    contacts: List<ContactModel>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(title.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp)
            }
            Text(count, fontSize = 12.sp, color = TextGray)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF5B8DEF).copy(alpha = 0.1f))
                .background(CardBackground, RoundedCornerShape(24.dp))
                .padding(vertical = 8.dp)
        ) {
            contacts.forEachIndexed { index, contact ->
                ContactItemRow(contact)
                // Separador sutil si no es el último
                if (index < contacts.lastIndex) {
                    Divider(
                        color = Color.Gray.copy(alpha = 0.1f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ContactItemRow(contact: ContactModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp), // Espaciado interno de cada fila
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. AVATAR CON PUNTO DE ESTADO
        Box {
            // Círculo de la foto (o inicial)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E7FF)), // Azulito fondo avatar
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.take(1),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4338CA)
                )
            }
            // Punto verde de "Online"
            if (contact.isOnline) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color.White) // Borde blanco
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(StatusGreen) // Relleno verde
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // 2. DATOS CENTRALES
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = contact.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextDark
                )

                // Badge de "Libre"
                if (contact.statusTag != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BadgeGreenBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = contact.statusTag,
                            color = Color(0xFF065F46), // Verde oscuro texto
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = contact.activity,
                fontSize = 13.sp,
                color = TextGray,
                maxLines = 1
            )
        }

        // 3. DATOS DERECHA (Ubicación)
        Column(horizontalAlignment = Alignment.End) {
            if (contact.distanceTag != null) {
                Text(
                    text = contact.distanceTag,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if(contact.isOnline) StatusGreen else TextGray
                )
            }
            Text(
                text = contact.locationName,
                fontSize = 11.sp,
                color = TextGray.copy(alpha = 0.7f)
            )
        }
    }
}

// --- DATOS FALSOS PARA PROBAR ---
data class ContactModel(
    val name: String,
    val statusTag: String?, // Ej: "Libre", "Ocupado"
    val activity: String,   // Ej: "Café • Hasta las 20:00"
    val distanceTag: String?, // Ej: "Muy cerca"
    val locationName: String, // Ej: "En tu zona"
    val isOnline: Boolean
)

fun getFavoritesList() = listOf(
    ContactModel("Ana", "Libre ahora", "Café • Hasta las 20:30", "Muy cerca", "En tu misma zona", true),
    ContactModel("Javi", "Libre", "Charlar • Esta tarde", "En tu zona", "Barrios cercanos", true),
    ContactModel("Luis", "Libre", "Deporte • Próxima hora", "En tu zona", "Parque o alrededores", true)
)

fun getNearbyList() = listOf(
    ContactModel("María", null, "Ocupada • Responderá luego", null, "En la ciudad", false),
    ContactModel("Paula", "Libre", "Cena • Próximas 2 h", "Muy cerca", "En tu zona", true),
    ContactModel("Carlos", null, "Sin actividad visible", null, "Lejos", false)
)