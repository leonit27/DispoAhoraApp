package com.example.dispoahora.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dispoahora.AccentBlue
import com.example.dispoahora.Screen
import com.example.dispoahora.TextGrayLight

@Composable
fun CustomBottomBar(
    currentRoute: String?,
    onHomeClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
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
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onHomeClick() }) {
                Icon(Icons.Outlined.Home, contentDescription = null, tint = if (currentRoute == Screen.Home.route) AccentBlue else TextGrayLight, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Estado", color = if (currentRoute == Screen.Home.route) AccentBlue else TextGrayLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onContactsClick() }) {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = if (currentRoute == Screen.Contacts.route) AccentBlue else TextGrayLight, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Contactos", color = if (currentRoute == Screen.Contacts.route) AccentBlue else TextGrayLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onSettingsClick() }) {
                Icon(Icons.Outlined.Settings, contentDescription = null, tint = if (currentRoute == Screen.Profile.route) AccentBlue else TextGrayLight, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Ajustes", color = if (currentRoute == Screen.Profile.route) AccentBlue else TextGrayLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}