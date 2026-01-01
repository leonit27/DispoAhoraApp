package com.example.dispoahora.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dispoahora.AccentBlue
import com.example.dispoahora.CardWhite
import com.example.dispoahora.TextGrayLight
import com.example.dispoahora.contacts.TextGray
import com.example.dispoahora.utils.dialogs.AddActivityDialog

@Composable
fun ActivitySection() {
    SectionTitle("ACTIVIDAD RÁPIDA")

    val activitiesList = rememberUserActivities()
    var showAddDialog by remember { mutableStateOf(false) }

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
            Text("Elige qué te apetece ahora", color = TextGrayLight, fontSize = 11.sp)


            FilterChip(
                selected = false,
                onClick = { showAddDialog = true },
                label = {
                    Text(
                        "Añadir",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White.copy(alpha = 0.5f),
                    labelColor = TextGray,
                    iconColor = TextGray,
                    selectedContainerColor = AccentBlue.copy(alpha = 0.2f)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = false,
                    borderColor = Color.Gray.copy(alpha = 0.1f),
                    borderWidth = 1.dp
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        var selectedActivity by remember { mutableStateOf("Café") }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(activitiesList) { activity ->
                ActivityChip(
                    icon = if (activity.iconRes != null) {
                        painterResource(id = activity.iconRes)
                    } else {
                        rememberVectorPainter(image = activity.iconVector!!)
                    },
                    text = activity.name,
                    isSelected = selectedActivity == activity.name,
                    onClick = { selectedActivity = activity.name }
                )
            }
        }

        if (showAddDialog) {
            AddActivityDialog(
                onDismiss = { showAddDialog = false },
                onActivityAdded = { name, vectorIcon ->
                    activitiesList.add(UserActivity(name, iconVector = vectorIcon))
                    selectedActivity = name
                    showAddDialog = false
                }
            )
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