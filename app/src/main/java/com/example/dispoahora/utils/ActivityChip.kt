package com.example.dispoahora.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dispoahora.AccentBlue
import com.example.dispoahora.TextDark

@Composable
fun ActivityChip(icon: Painter, text: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) AccentBlue else Color(0xFFF3F4F6)
    val contentColor = if (isSelected) Color.White else TextDark

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}