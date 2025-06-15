package top.kagg886.mptmap.util

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange

internal actual fun Modifier.detectZoomAndDrag(
    z: Int,
    onZoom: (Float) -> Unit,
    onDrag: (Offset) -> Unit
): Modifier = pointerInput(z) {
    awaitEachGesture {
        // 追踪当前活动的指针数量
        var activePointersCount = 0
        // 追踪是否处于缩放模式
        var isZooming = false

        while (true) {
            val event = awaitPointerEvent()
            val currentPointersCount = event.changes.size

            // 检测指针数量的变化
            if (currentPointersCount != activePointersCount) {
                // 指针数量发生变化，重置状态
                isZooming = false
                activePointersCount = currentPointersCount
            }

            // 处理缩放事件（两个或更多指针）
            if (currentPointersCount >= 2) {
                val zoomChange = event.calculateZoom()
                if (zoomChange != 1f) {
                    isZooming = true

                    onZoom(zoomChange)

                    // 消费所有指针的事件
                    event.changes.forEach {
                        if (it.positionChange() != Offset.Zero) {
                            it.consume()
                        }
                    }
                }
            }
            // 处理拖拽事件（单指且不处于缩放模式）
            else if (currentPointersCount == 1 && !isZooming) {
                val pointerChange = event.changes.first()

                // 获取位置变化
                val delta = pointerChange.position - pointerChange.previousPosition

                if (delta != Offset.Zero) {
                    onDrag(delta)
                    if (pointerChange.positionChange() != Offset.Zero) {
                        pointerChange.consume()
                    }
                }
            }

            // 如果所有指针都抬起，重置状态
            if (event.changes.all { it.changedToUp() }) {
                isZooming = false
                activePointersCount = 0
            }
        }
    }
}
