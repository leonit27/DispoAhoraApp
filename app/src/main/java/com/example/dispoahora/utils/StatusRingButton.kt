package com.example.dispoahora.utils

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dispoahora.StatusGreenRing
import com.example.dispoahora.StatusRedRing

@Composable
fun StatusRingButton(
    isLibre: Boolean,
    onToggle: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "anillo_animacion")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angulo_rotacion"
    )

    val primaryColor = if (isLibre) StatusGreenRing else StatusRedRing
    val secondaryColor = if (isLibre) Color(0xFF3B82F6) else Color(0xFFF6A3FF)

    val statusText = if (isLibre) "Libre" else "Ocupado"
    val statusFontSize = if (isLibre) 15.sp else 10.sp

    Box(contentAlignment = Alignment.Center) {

        Canvas(
            modifier = Modifier
                .size(90.dp)
                .rotate(angle)
        ) {
            val brush = Brush.sweepGradient(
                colors = listOf(primaryColor, secondaryColor, primaryColor)
            )
            drawCircle(
                brush = brush,
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F172A))
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                .clickable { onToggle() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ESTADO",
                    color = Color(0xFF94A3B8),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = statusText,
                    color = Color.White,
                    fontSize = statusFontSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}