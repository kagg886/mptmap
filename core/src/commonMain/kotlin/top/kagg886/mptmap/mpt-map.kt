package top.kagg886.mptmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import top.kagg886.mptmap.state.MPTMapService
import top.kagg886.mptmap.state.MPTMapSetting
import top.kagg886.mptmap.state.MPTMapState
import top.kagg886.mptmap.util.detectZoomAndDrag
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun MPTMap(
    modifier: Modifier = Modifier,
    state: MPTMapState,
    service: MPTMapService,
    setting: MPTMapSetting,
    content: @Composable MPTMapScope.() -> Unit = {},
) = BoxWithConstraints(modifier) {
    check(maxWidth != Dp.Infinity) { "maxWidth must be finite" }
    check(maxHeight != Dp.Infinity) { "maxHeight must be finite" }

    val zoomRangeToZIndex = remember(service.zoomRange) {
        val aRange = 0..100
        val bRange = service.zoomRange

        val aSize = aRange.last - aRange.first + 1
        val bSize = bRange.last - bRange.first + 1

        val map = mutableMapOf<IntRange, Int>()

        for (i in 0 until bSize) {
            val start = aRange.first + (i * aSize) / bSize
            val end = if (i == bSize - 1) {
                aRange.last // 最后一段包含到A范围的末尾
            } else {
                aRange.first + ((i + 1) * aSize) / bSize - 1
            }

            val subRange = start..end
            val bValue = bRange.first + i

            map[subRange] = bValue
        }

        map.toMap()
    }

    val z = remember(state.zoom, zoomRangeToZIndex) {
        //对小数点不敏感，所以可以toInt()
        zoomRangeToZIndex.entries.first { state.zoom.toInt() in it.key }.value
    }

    //视口大小
    val (viewPortWidth, viewPortHeight) = with(LocalDensity.current) {
        Size(maxWidth.toPx(), maxHeight.toPx())
    }

    //中心瓦片周围的瓦片数（十字排布）
    val (horizonalLeftTileCount, verticalTopTileCount) = remember(
        viewPortWidth,
        viewPortHeight,
        service.tileSize,
    ) {
        (ceil((viewPortWidth / 2 - service.tileSize / 2) / service.tileSize).toInt() + setting.preloadTileSize) to (ceil(
            (viewPortHeight / 2 - service.tileSize / 2) / service.tileSize
        ).toInt() + setting.preloadTileSize)
    }

    val centerTail = remember(state.lat, state.lng, z) {
        service.getTileParam(state.lat, state.lng, z)
    }

    //瓦片
    val tailsMatrix = remember(centerTail, horizonalLeftTileCount, verticalTopTileCount) {
        val (centerTailX, centerTailY) = centerTail
        (centerTailY - verticalTopTileCount..centerTailY + verticalTopTileCount).map { newY ->
            (centerTailX - horizonalLeftTileCount..centerTailX + horizonalLeftTileCount).map { newX ->
                newX to newY
            }
        }.flatten()
    }

    //经纬度相对于所在瓦片的坐标
    val tailOffset = remember(state.lat, state.lng, z) {
        service.getPixelOffsetByLatLng(state.lat, state.lng, z)
    }

    // 使用 Map 来缓存瓦片，而不是 List
    val bitmapCache = remember(z) {
        mutableStateMapOf<Pair<Int, Int>, ImageBitmap?>()
    }

    LaunchedEffect(tailsMatrix) {
        // 首先移除不再需要的瓦片
        val currentTiles = tailsMatrix.toSet()
        val tilesToRemove = bitmapCache.keys.filter { it !in currentTiles }
        tilesToRemove.forEach { bitmapCache.remove(it) }

        // 只加载缺失的瓦片
        val tilesToLoad = tailsMatrix.filter { it !in bitmapCache.keys }

        val lock = Mutex()
        tilesToLoad.map { tile ->
            async {
                val (x, y) = tile
                val tileCount = 2.0.pow(z).toInt()  // 瓦片总数：2^z
                val xWrapped = ((x % tileCount) + tileCount) % tileCount  // 正确的循环处理
                val data = withContext(setting.dispatcher) {
                    service.requestForImageBitmap(xWrapped, y, z)
                }
                lock.withLock {
                    bitmapCache[tile] = data
                }
            }
        }.awaitAll()
    }

    val firstTileStartX =
        (viewPortWidth / 2 - service.tileSize / 2 - horizonalLeftTileCount * service.tileSize).roundToInt()
    val firstTileStartY =
        (viewPortHeight / 2 - service.tileSize / 2 - verticalTopTileCount * service.tileSize).roundToInt()

    var nonChangeScale by remember(z) {
        mutableStateOf(1f)
    }
    LaunchedEffect(state.zoom) {
        val range = zoomRangeToZIndex.entries.first { it.value == z }.key
        val relativePosition = (state.zoom - range.first) / (range.last - range.first).toFloat()
        nonChangeScale = 1f + relativePosition
    }

    val scope = remember(service, z, this) {
        MPTMapScope(service, z, this)
    }

    Canvas(
        modifier = Modifier
            .matchParentSize()
            .detectZoomAndDrag(
                z = z,
                onZoom = { zoom ->
                    val transform = if (zoom > 1f) setting.speed else if (zoom < 1f) -setting.speed else 0f
                    state.zoom(state.zoom + transform)
                },
                onDrag = { delta ->
                    println(delta)
                    val (latDelta, lngDelta) = service.getLatLngDeltaByPixelOffset(
                        pixelOffset = delta / nonChangeScale, //delta是1x缩放情况下的偏移量，需要除以缩放比例
                        z = z,
                        currentLat = state.lat
                    )

                    // 更新地图状态（注意方向：拖拽方向与地图移动方向相反）
                    state.lat -= latDelta
                    state.lng -= lngDelta
                }
            )
            .graphicsLayer {
                scaleX = nonChangeScale
                scaleY = nonChangeScale
            }
    ) {
        for (y in 0 until 2 * verticalTopTileCount + 1) {
            for (x in 0 until 2 * horizonalLeftTileCount + 1) {
                val index = y * (2 * horizonalLeftTileCount + 1) + x
                val tile = tailsMatrix.getOrNull(index) ?: continue
                val bitmap = bitmapCache[tile] ?: continue

                drawImage(
                    image = bitmap,
                    topLeft = Offset(
                        firstTileStartX + x * service.tileSize.toFloat() + service.tileSize / 2f - tailOffset.x,
                        firstTileStartY + y * service.tileSize.toFloat() + service.tileSize / 2f - tailOffset.y,
                    )
                )
            }
        }
    }

    for (marker in scope.markers) {
        val (markerX, markerY, offset, alignment, modifier, content) = marker

        //当前布局里没有这张瓦片直接返回
        if (!tailsMatrix.any { (matrixX, matrixY) -> matrixX == markerX && matrixY == markerY }) {
            continue
        }

        //这个marker的参考点坐标
        val markerPosition = Offset(
            firstTileStartX + (markerX - tailsMatrix[0].first) * service.tileSize.toFloat() + service.tileSize / 2f - tailOffset.x + offset.x,
            firstTileStartY + (markerY - tailsMatrix[0].second) * service.tileSize.toFloat() + service.tileSize / 2f - tailOffset.y + offset.y,
        )

        // 计算缩放后的位置：以视口中心为原点进行缩放变换
        val scaledMarkerPosition = Offset(
            viewPortWidth / 2 + (markerPosition.x - viewPortWidth / 2) * nonChangeScale,
            viewPortHeight / 2 + (markerPosition.y - viewPortHeight / 2) * nonChangeScale
        )

        Layout(
            content = {
                Box(modifier = modifier) {
                    content()
                }
            },
            modifier = Modifier.offset {
                IntOffset(
                    x = scaledMarkerPosition.x.roundToInt(),
                    y = scaledMarkerPosition.y.roundToInt()
                )
            }
        ) { measurables, constraints ->
            val placeable = measurables.first().measure(constraints)

            val alignmentOffset = alignment.align(
                size = IntSize.Zero,
                space = IntSize(placeable.width, placeable.height),
                layoutDirection = LayoutDirection.Ltr
            ).let { IntOffset(-it.x, -it.y) }

            layout(placeable.width, placeable.height) {
                placeable.place(alignmentOffset)
            }
        }
    }

    content(scope)
}
