package top.kagg886.mptmap.data

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset

data class Marker(
    val x: Int,
    val y: Int,
    val offset: Offset,
    val alignment: Alignment,
    val modifier: Modifier = Modifier,
    val content: @Composable () -> Unit
)
