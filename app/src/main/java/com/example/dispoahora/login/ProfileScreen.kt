package com.example.dispoahora.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dispoahora.GradientBackground
import com.example.dispoahora.TextDark
import com.example.dispoahora.TextGrayLight

// Colores específicos para esta pantalla (basados en tu imagen)
val LightPurpleBg = Color(0xFFEBEBF5) // Fondo de las secciones
val EditButtonBg = Color(0xFFE0E7FF) // Azul muy claro para botón editar
val EditButtonText = Color(0xFF4F46E5) // Azul índigo
val DangerBg = Color(0xFFFEE2E2) // Rojo muy claro
val DangerText = Color(0xFFDC2626) // Rojo fuerte

@Composable
fun ProfileScreen(
    username: String?,
    email: String,
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {
    // Usamos el mismo fondo degradado que la home
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = GradientBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // --- Cabecera (Botón atrás y Título) ---
            // Nota: Aquí reutilizamos el estilo de "Hola, Tomás" pero adaptado
            ProfileHeader(username, onBack)

            Spacer(modifier = Modifier.height(20.dp))

            // --- Tarjeta Principal del Perfil ---
            MainProfileCard(username, email)

            Spacer(modifier = Modifier.height(24.dp))

            // --- Sección: Cuenta ---
            SectionHeader(title = "Cuenta", subtitle = "Gestiona tus datos básicos de acceso")
            Spacer(modifier = Modifier.height(8.dp))
            SectionCard {
                InfoItem(label = "Nombre", value = username)
                Divider(color = Color.Black.copy(alpha = 0.05f))
                InfoItem(label = "Usuario", value = "@${username?.lowercase()?.replace(" ", "")}")
                Divider(color = Color.Black.copy(alpha = 0.05f))
                InfoItem(label = "Teléfono", value = "+34 ••• •• 321")
                Divider(color = Color.Black.copy(alpha = 0.05f))
                InfoItem(label = "Correo", value = email, showArrow = false)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Sección: Red y visibilidad ---
            SectionHeader(title = "Red y visibilidad", subtitle = "Decide quién puede verte y cómo te encuentran")
            Spacer(modifier = Modifier.height(8.dp))
            SectionCard {
                InfoItem(label = "Seguidores", value = "Personas que pueden ver tu estado público", badgeCount = 128)
                Divider(color = Color.Black.copy(alpha = 0.05f))
                InfoItem(label = "Siguiendo", value = "Contactos de los que ves disponibilidad", badgeCount = 94)
                Divider(color = Color.Black.copy(alpha = 0.05f))
                InfoItem(label = "Círculo cercano", value = "Más prioridad en estados y coincidencias", badgeCount = 18)
                Divider(color = Color.Black.copy(alpha = 0.05f))
                InfoItem(label = "Bloqueados", value = "Personas que no podrán verte ni escribirte")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Sección: Seguridad y sesión ---
            SectionHeader(title = "Seguridad y sesión", subtitle = "Controla tu acceso y dónde has iniciado sesión")
            Spacer(modifier = Modifier.height(12.dp))

            ActionButton(text = "Cambiar contraseña", icon = Icons.Default.Edit)
            Spacer(modifier = Modifier.height(12.dp))
            ActionButton(text = "Dispositivos con sesión iniciada", icon = Icons.Outlined.CheckCircle)
            Spacer(modifier = Modifier.height(12.dp))

            // Botón Cerrar Sesión (Estilo Peligro)
            Button(
                onClick = onSignOut,
                colors = ButtonDefaults.buttonColors(containerColor = DangerBg, contentColor = DangerText),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Icon(Icons.Outlined.Warning, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar sesión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun ProfileHeader(username: String?, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hola, $username!",
                color = TextDark,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Barcelona, Eixample",
                color = TextGrayLight,
                fontSize = 14.sp
            )
        }
        // Avatar pequeño (hace de botón atrás si quieres, o solo visual)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.5f))
                .clickable { onBack() }, // Al hacer clic volvemos
            contentAlignment = Alignment.Center
        ) {
            Text("T", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 18.sp)
        }
    }
}

@Composable
fun MainProfileCard(username: String?, email: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar Grande
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(Color(0xFFE0E7FF), Color(0xFFC7D2FE))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = username?.take(1)?.uppercase() ?: "",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3730A3)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    if (username != null) {
                        Text(username, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                    Text("@${username?.lowercase()?.replace(" ", "")}", fontSize = 14.sp, color = TextGrayLight)
                    Text("Mi círculo cercano y amigos de confianza", fontSize = 12.sp, color = TextGrayLight, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Stats
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    ProfileStat("128", "Seguidores")
                    ProfileStat("94", "Siguiendo")
                    ProfileStat("18", "Círculo")
                }

                // Botón Editar
                Button(
                    onClick = { /* TODO: Editar */ },
                    colors = ButtonDefaults.buttonColors(containerColor = EditButtonBg, contentColor = EditButtonText),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Editar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ProfileStat(count: String, label: String) {
    Column {
        Text(label, fontSize = 11.sp, color = TextGrayLight)
        Text(count, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Text(subtitle, fontSize = 10.sp, color = TextGrayLight, modifier = Modifier.widthIn(max = 180.dp), lineHeight = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            content()
        }
    }
}

@Composable
fun InfoItem(label: String, value: String?, showArrow: Boolean = true, badgeCount: Int? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            if (value != null) {
                Text(value, fontSize = 12.sp, color = TextGrayLight)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (badgeCount != null) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFE0E7FF), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(badgeCount.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            if (showArrow) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Button(
        onClick = { },
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.6f), contentColor = TextDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().height(56.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextDark.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}