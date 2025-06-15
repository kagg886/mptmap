package top.kagg886.mptmap.util

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

internal actual fun Modifier.detectZoomAndDrag(
    z: Int,
    onZoom: (Float) -> Unit,
    onDrag: (Offset) -> Unit
): Modifier = composed {
    val zoomTransform = rememberTransformableState { zoom, _, _ ->
        onZoom(zoom)
    }

    transformable(zoomTransform).pointerInput(z) {
        detectDragGestures { _, delta ->
            onDrag(delta)
        }
    }
}
