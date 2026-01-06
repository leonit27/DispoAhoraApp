package com.example.dispoahora.utils

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeParseException

@Composable
fun CountdownDisplay(
    expirationString: String,
    color: Color
) {
    var timeLeft by remember { mutableStateOf("Calculando...") }

    LaunchedEffect(expirationString) {
        try {
            val expiresAt = Instant.parse(expirationString)
            while (true) {
                val now = Instant.now()
                val diff = Duration.between(now, expiresAt)

                if (diff.isNegative || diff.isZero) {
                    timeLeft = "00:00:00"
                    break
                } else {
                    val hours = diff.toHours()
                    val minutes = diff.toMinutesPart()
                    val seconds = diff.toSecondsPart()
                    timeLeft = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                }
                delay(1000L)
            }
        } catch (_: DateTimeParseException) {
            timeLeft = "Error fecha"
        }
    }

    Text(
        text = "Libre por: $timeLeft",
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold
    )
}