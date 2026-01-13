package com.example.dispoahora.utils.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dispoahora.AccentBlue

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dispoahora.login.AuthViewModel
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState

@Composable
fun AddActivityDialog(
    onDismiss: () -> Unit,
    onActivityAdded: (String, ImageVector) -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var activityName by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf(Icons.Default.Add) }

    val aiSuggestion by authViewModel.aiSuggestion.collectAsState()

    LaunchedEffect(aiSuggestion) {
        if (aiSuggestion.isNotBlank()) {
            activityName = aiSuggestion
        }
    }

    val availableIcons = listOf(
        Icons.Default.Add, Icons.Default.Home, Icons.Default.Edit,
        Icons.Default.LocationOn, Icons.Default.ShoppingCart, Icons.Default.Warning,
        Icons.Default.CheckCircle, Icons.Default.Check, Icons.Default.PlayArrow,
        Icons.Default.Person, Icons.Default.AccountBox, Icons.Default.AccountCircle,
        Icons.Default.AddCircle, Icons.Default.ArrowDropDown, Icons.Default.Build,
        Icons.Default.Call, Icons.Default.Clear, Icons.Default.ThumbUp,
        Icons.Default.Create, Icons.Default.DateRange, Icons.Default.Delete,
        Icons.Default.Done, Icons.Default.Email, Icons.Default.Face,
        Icons.Default.Favorite, Icons.Default.FavoriteBorder, Icons.Default.Info,
        Icons.Default.KeyboardArrowDown, Icons.Default.KeyboardArrowUp, Icons.Default.Lock,
        Icons.Default.MailOutline, Icons.Default.Menu, Icons.Default.Notifications,
        Icons.Default.Refresh, Icons.Default.Search, Icons.Default.Settings,
        Icons.Default.Star
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Actividad", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = activityName,
                    onValueChange = { activityName = it },
                    label = { Text("Nombre de la actividad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = {
                            authViewModel.generateActivitySuggestion("un plan espontáneo y corto")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Sugerencia IA",
                                tint = Color(0xFF6200EE)
                            )
                        }
                    }
                )

                Text("Elige un icono", fontSize = 14.sp, fontWeight = FontWeight.Medium)

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(availableIcons) { icon ->
                        Box(
                            modifier = Modifier
                                .size(45.dp)
                                .clip(CircleShape)
                                .background(if (selectedIcon == icon) AccentBlue else Color.Transparent)
                                .clickable { selectedIcon = icon },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (selectedIcon == icon) Color(0xFF1D4ED8) else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (activityName.isNotBlank()) {
                        onActivityAdded(activityName, selectedIcon)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}