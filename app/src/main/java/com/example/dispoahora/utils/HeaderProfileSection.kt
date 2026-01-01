package com.example.dispoahora.utils

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dispoahora.utils.dialogs.LocationSelectionDialog
import com.example.dispoahora.TextDark
import com.example.dispoahora.TextGrayLight
import com.example.dispoahora.location.LocationService
import com.example.dispoahora.location.LocationViewModel

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
            onDismiss = {},
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
            }
        )
    }
}