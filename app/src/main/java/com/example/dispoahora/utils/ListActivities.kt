package com.example.dispoahora.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.dispoahora.R

data class UserActivity(
    val name: String,
    val iconRes: Int? = null,
    val iconVector: ImageVector? = null
)

@Composable
fun rememberUserActivities(): SnapshotStateList<UserActivity> {
    return remember {
        mutableStateListOf(
            UserActivity("Café", iconRes = R.drawable.coffee),
            UserActivity("Deporte", iconRes = R.drawable.sports),
            UserActivity("Cena", iconRes = R.drawable.dinner),
            UserActivity("Chat", iconRes = R.drawable.chat)
        )
    }
}