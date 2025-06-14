package top.kagg886.mptmap

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import top.kagg886.mptmap.data.LatLng
import top.kagg886.mptmap.state.MPTMapService
import top.kagg886.mptmap.data.Marker as InternalMarker

class MPTMapScope(
    private val service: MPTMapService,
    private val z: Int,
) {
    val markers = mutableStateListOf<InternalMarker>()

    /**
     * 添加一个标记。
     *
     * 在布局组件时，组件的正中心会恰好落在latLng中。
     */
    fun Marker(
        modifier: Modifier = Modifier,
        latLng: LatLng,
        alignment: Alignment = Alignment.BottomCenter,
        content: @Composable () -> Unit,
    ) {
        val (x, y) = service.getTileParam(latLng.lat, latLng.lng, z)
        val offset = service.getPixelOffsetByLatLng(latLng.lat, latLng.lng, z)
        val it = InternalMarker(x, y, offset, alignment, modifier, content)
        markers.add(it)
    }
}
