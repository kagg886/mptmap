package top.kagg886.mptmap.state

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import top.kagg886.mptmap.data.LatLng
import top.kagg886.mptmap.service.MPTMapService
import top.kagg886.mptmap.data.Marker as InternalMarker

class MPTMapScope(
    private val service: MPTMapService,
    private val z: Int,

    private val originBox: BoxScope,
): BoxScope by originBox {
    val markers = mutableStateSetOf<InternalMarker>()

    /**
     * 添加一个相对于地图移动的标记。
     *
     * @param modifier 修饰符
     * @param latLng 标记的经纬度
     * @param alignment 标记相对于参考点的对齐方式
     * @param content 标记的内容
     */
    @Composable
    fun Marker(
        modifier: Modifier = Modifier,
        latLng: LatLng,
        alignment: Alignment = Alignment.BottomCenter,
        content: @Composable () -> Unit,
    ) {
        DisposableEffect(service,z,modifier,latLng,alignment,content,originBox) {
            val (x, y) = service.getTileParam(latLng.lat, latLng.lng, z)
            val offset = service.getPixelOffsetByLatLng(latLng.lat, latLng.lng, z)
            val it = InternalMarker(x, y, offset, alignment, modifier, content)
            markers.add(it)

            onDispose {
                markers.remove(it)
            }
        }
    }
}
