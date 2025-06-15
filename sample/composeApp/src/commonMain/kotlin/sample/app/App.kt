package sample.app

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.Image
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import top.kagg886.mptmap.MPTMap
import top.kagg886.mptmap.data.LatLng
import top.kagg886.mptmap.data.MPTMapSetting
import top.kagg886.mptmap.service.amap.AMapService
import top.kagg886.mptmap.state.MPTMapState
import kotlin.math.*

fun ImageLoader.Builder.applyCustomConfig(): ImageLoader.Builder = apply {
    components {
        add(KtorNetworkFetcherFactory())
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val markers = remember { mutableStateListOf<LatLng>() }
    val context = LocalPlatformContext.current
    val state = remember { MPTMapState(39.905024, 116.393823, 60f) }

    var dialog by remember { mutableStateOf(false) }
    var lat by remember { mutableStateOf("") }
    var lng by remember { mutableStateOf("") }
    if (dialog) {
        AlertDialog(
            onDismissRequest = { dialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        markers.add(LatLng(lat.toDouble(), lng.toDouble()))
                        dialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            title = { Text("添加标记") },
            text = {
                Row {
                    TextField(
                        value = lng,
                        onValueChange = { lng = it },
                        label = { Text("经度") },
                        modifier = Modifier.weight(1f)
                    )
                    TextField(
                        value = lat,
                        onValueChange = { lat = it },
                        label = { Text("纬度") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("TMap Demo")
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    dialog = true
                }
            ) {
                Text("+")
            }
        }
    ) {
        MPTMap(
            modifier = Modifier.fillMaxSize().padding(it),
            state = state,
            service = remember { AMapService(ktor3CoilFetcher(context)) },
            setting = remember { MPTMapSetting() }
        ) {
            Marker(
                latLng = LatLng(39.905024, 116.393823),
                alignment = Alignment.BottomCenter,
                modifier = Modifier.size(24.dp),
            ) {
                AsyncImage(
                    model = "https://a.amap.com/jsapi/static/image/plugin/marker_red.png",
                    contentDescription = "Marker",
                )
            }

            for (marker in markers) {
                Marker(
                    latLng = marker,
                    alignment = Alignment.BottomEnd,
                    modifier = Modifier.size(24.dp),
                ) {
                    AsyncImage(
                        model = "https://a.amap.com/jsapi/static/image/plugin/marker_red.png",
                        contentDescription = "Marker",
                    )
                }
            }


            AsyncImage(
                model = "https://webapi.amap.com/theme/v2.0/logo@1x.png",
                contentDescription = "Logo",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .graphicsLayer(
                        scaleX = 1.5f,
                        scaleY = 1.5f,
                        transformOrigin = TransformOrigin(0f, 1f)  // 左下角为变换原点
                    )
            )
        }
    }
}

fun ktor3CoilFetcher(context: PlatformContext): suspend (String) -> ImageBitmap? {
    val cache = mutableMapOf<String, ImageBitmap>()

    return { url ->
        val loader = SingletonImageLoader.get(context)
        val pixel = cache[url]

        if (pixel == null) {
            val req = ImageRequest.Builder(context).data(url).build()
            val data = (loader.execute(req) as? SuccessResult)?.image
            data?.toImageBitmap()?.apply {
                cache[url] = this
            }
        }
        pixel
    }
}

expect fun Image.toImageBitmap(): ImageBitmap
