package top.kagg886.mptmap.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset

internal expect fun Modifier.detectZoomAndDrag(
    z: Int,
    onZoom: (Float) -> Unit,
    onDrag: (Offset) -> Unit,
): Modifier
