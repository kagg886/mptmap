package top.kagg886.mptmap.util

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput

internal actual fun androidx.compose.ui.Modifier.detectZoomAndDrag(
    z: Int,
    onZoom: (Float) -> Unit,
    onDrag: (androidx.compose.ui.geometry.Offset) -> Unit
): androidx.compose.ui.Modifier = composed {
    val zoomTransform = rememberTransformableState { zoom, _, _ ->
        onZoom(zoom)
    }

    transformable(zoomTransform).pointerInput(z) {
        detectDragGestures { _, delta ->
            onDrag(delta)
        }
    }
}
