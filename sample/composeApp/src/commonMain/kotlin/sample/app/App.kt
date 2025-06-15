package sample.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.launch
import top.kagg886.mptmap.MPTMap
import top.kagg886.mptmap.data.LatLng
import top.kagg886.mptmap.data.MPTMapSetting
import top.kagg886.mptmap.service.amap.AMapService
import top.kagg886.mptmap.service.amap.AMapType
import top.kagg886.mptmap.state.MPTMapState

fun ImageLoader.Builder.applyCustomConfig(): ImageLoader.Builder = apply {
    components {
        add(KtorNetworkFetcherFactory())
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val context = LocalPlatformContext.current
    val state = remember { MPTMapState(39.905024, 116.393823, 60f) }

    val scope = rememberCoroutineScope()
    val drawer = rememberDrawerState(initialValue = DrawerValue.Closed)

    var select by remember { mutableStateOf(AMapType.NORMAL) }

    ModalNavigationDrawer(
        drawerState = drawer,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Map Type",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
                for (type in AMapType.entries) {
                    NavigationDrawerItem(
                        label = {
                            Text(type.name)
                        },
                        selected = select == type,
                        onClick = {
                            select = type
                            scope.launch {
                                drawer.close()
                            }
                        },
                    )
                }
            }
        }
    ) {
        val markers = remember { mutableStateListOf<LatLng>() }
        var showMarkerDialog by remember { mutableStateOf(false) }

        if (showMarkerDialog) {
            AlertDialog(
                onDismissRequest = { showMarkerDialog = false },
                confirmButton = {},
                title = {
                    Text("管理Marker")
                },
                text = {
                    LazyColumn(Modifier.fillMaxWidth().height(200.dp)) {
                        item {
                            var latLng by remember { mutableStateOf("") }
                            var errorTip by remember { mutableStateOf("") }
                            val error by remember {
                                derivedStateOf {
                                    errorTip.isNotEmpty()
                                }
                            }
                            OutlinedTextField(
                                value = latLng,
                                onValueChange = { latLng = it },
                                placeholder = {
                                    Text("输入经纬度，格式为：lat,lng")
                                },
                                isError = error,
                                supportingText = {
                                    if (error) {
                                        Text(errorTip, color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                label = {
                                    Text("经纬度")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            val split = latLng.split(",")
                                            if (split.size == 2) {
                                                val lat = split[0].toDoubleOrNull()
                                                val lng = split[1].toDoubleOrNull()
                                                if (lat != null && lng != null) {
                                                    try {
                                                        markers.add(LatLng(lat, lng))
                                                    } catch (e: Exception) {
                                                        errorTip = e.message ?: "未知错误"
                                                    }
                                                    latLng = ""
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add"
                                        )
                                    }
                                }
                            )
                        }
                        items(markers) {
                            ListItem(
                                headlineContent = {
                                    Text("${it.lat}, ${it.lng}")
                                },
                                trailingContent = {
                                    IconButton(
                                        onClick = {
                                            markers.remove(it)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close"
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("TMap Demo")
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawer.isOpen) {
                                        drawer.close()
                                    } else {
                                        drawer.open()
                                    }
                                }
                            }
                        ) {
                            AnimatedContent(
                                targetState = drawer.currentValue,
                                label = "icon",
                                transitionSpec = {
                                    if (initialState == DrawerValue.Open) {
                                        slideInVertically { height -> height } + fadeIn() togetherWith
                                                slideOutVertically { height -> -height } + fadeOut()
                                    } else {
                                        slideInVertically { height -> -height } + fadeIn() togetherWith
                                                slideOutVertically { height -> height } + fadeOut()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (it == DrawerValue.Open) Icons.Default.Close else Icons.Default.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        showMarkerDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu"
                    )
                }
            }
        ) {
            val service = remember(select) {
                AMapService(type = select, fetcher = ktor3CoilFetcher(context))
            }
            MPTMap(
                modifier = Modifier.fillMaxSize().padding(it),
                state = state,
                service = service,
                setting = remember { MPTMapSetting() }
            ) {
                for (marker in markers) {
                    Marker(
                        latLng = marker,
                        alignment = Alignment.BottomCenter,
                        modifier = Modifier.size(24.dp),
                    ) {
                        AsyncImage(
                            model = "https://a.amap.com/jsapi/static/image/plugin/marker_red.png",
                            contentDescription = "Marker",
                        )
                    }
                }

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

}

fun ktor3CoilFetcher(context: PlatformContext): suspend (String) -> ImageBitmap? {
    val cache = mutableMapOf<String, ImageBitmap>()

    return a@{ url ->
        val loader = SingletonImageLoader.get(context)
        val pixel = cache[url]

        if (pixel == null) {
            val req = ImageRequest.Builder(context).data(url).build()
            val data = (loader.execute(req) as? SuccessResult)?.image
            return@a data?.toImageBitmap()?.apply {
                cache[url] = this
            }
        }
        pixel
    }
}

expect fun Image.toImageBitmap(): ImageBitmap
