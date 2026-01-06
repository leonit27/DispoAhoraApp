package com.example.dispoahora.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dispoahora.PastelBlueBottom
import com.example.dispoahora.PastelBlueTop
import com.example.dispoahora.login.AuthViewModel
import com.example.dispoahora.utils.SectionTitle
import kotlinx.serialization.Serializable

val GradientBackground = Brush.verticalGradient(
    colors = listOf(PastelBlueTop, PastelBlueBottom)
)
val CardBackground = Color.White.copy(alpha = 0.9f)
val TextDark = Color(0xFF1F2937)
val TextGray = Color(0xFF6B7280)
val StatusGreen = Color(0xFF10B981)

@Composable
fun ContactsScreen(authViewModel: AuthViewModel = viewModel()) {
    val realUsers by authViewModel.realUsers.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.fetchContacts()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GradientBackground)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

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

        SearchBarSection()

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle("FAVORITOS")

        if (realUsers.isEmpty()) {
            Text("Cargando contactos...", color = TextGray, modifier = Modifier.padding(16.dp))
        } else {
            ContactGroupCard(realUsers)
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle("CERCA DE TI")

        Spacer(modifier = Modifier.height(50.dp))
    }
}
@Composable
fun SearchBarSection() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Buscar nombre o alias", color = TextGray) },
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

        Button(
            onClick = { /**/ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4F5BE)),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .size(56.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f))
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Filtro",
                tint = Color(0xFF3F6212)
            )
        }
    }
}

@Composable
fun ContactGroupCard(
    contacts: List<ContactModel>
) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF5B8DEF).copy(alpha = 0.1f))
                .background(CardBackground, RoundedCornerShape(24.dp))
                .padding(vertical = 8.dp)
        ) {
            contacts.forEachIndexed { index, contact ->
                ContactItemRow(contact)
                if (index < contacts.lastIndex) {
                    HorizontalDivider(
                        color = Color.Gray.copy(alpha = 0.1f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
}

@Composable
fun ContactItemRow(contact: ContactModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            if (!contact.avatar_url.isNullOrBlank()) {
                AsyncImage(
                    model = contact.avatar_url,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFE0E7FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = contact.full_name.take(1), fontWeight = FontWeight.Bold, color = Color(0xFF4338CA))
                }
            }

            if (contact.status == "Libre") {
                Box(
                    modifier = Modifier.size(14.dp).clip(CircleShape).background(Color.White).padding(2.dp)
                        .clip(CircleShape).background(StatusGreen).align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.full_name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextDark
            )
            Text(
                text = contact.bio ?: "Sin actividad reciente",
                fontSize = 13.sp,
                color = TextGray,
                maxLines = 1
            )
        }

        Text(
            text = contact.status ?: "Ocupado",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if(contact.status == "Libre") StatusGreen else TextGray
        )
    }
}

@Serializable
data class ContactModel(
    val id: String,
    val full_name: String,
    val avatar_url: String? = null,
    val status: String? = "Desconectado",
    val bio: String? = null
)