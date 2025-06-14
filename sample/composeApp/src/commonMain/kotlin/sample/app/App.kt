package sample.app

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import coil3.Image
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import top.kagg886.mptmap.MPTMap
import top.kagg886.mptmap.state.*
import kotlin.math.*

fun ImageLoader.Builder.applyCustomConfig(): ImageLoader.Builder = apply {
    components {
        add(KtorNetworkFetcherFactory())
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val context = LocalPlatformContext.current
    val state = remember { MPTMapState(39.904632, 116.397691, 60) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("TMap Demo")
                }
            )
        },
        floatingActionButton = {
            Row {
                FloatingActionButton(
                    onClick = {
                        state.zoom(state.zoom + 1)
                    }
                ) {
                    Text("+")
                }

                FloatingActionButton(
                    onClick = {
                        state.zoom(state.zoom - 1)
                    }
                ) {
                    Text("-")
                }
            }
        }
    ) {
        MPTMap(
            modifier = Modifier.fillMaxSize().padding(it),
            state = state,
            service = remember { GDMapService(context) },
            setting = remember { MPTMapSetting() }
        )
    }
}

data class GDMapService(private val context: PlatformContext) : MPTMapService {
    override val tileSize: Int = 256
    override val zoomRange: IntRange = 1..19

    override fun getTileParam(
        lat: Double,
        lng: Double,
        z: Int,
    ): Pair<Int, Int> {
        val n = 2.0.pow(z)

        // 根据经度计算 tileX
        val x = (lng + 180.0) / 360.0
        var tileX = floor(x * n).toInt()
        // 实现循环效果，而不是简单限制边界
        val tileCount = 2.0.pow(z).toInt()
        tileX = ((tileX % tileCount) + tileCount) % tileCount

        // 根据纬度计算 tileY
        val latRad = lat * PI / 180
        val y = (1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0
        val tileY = floor(y * n).toInt()

        return tileX to tileY
    }


    override fun getPixelOffsetByLatLng(
        lat: Double,
        lng: Double,
        z: Int
    ): Offset {
        val mapSize = 2.0.pow(z) * tileSize

        val x = ((lng + 180.0) / 360.0) * mapSize
        val latRad = lat * PI / 180
        val y = (1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * mapSize

        val offsetX = (x % tileSize).toFloat()
        val offsetY = (y % tileSize).toFloat()

        return Offset(offsetX, offsetY)
    }

    override fun getLatLngDeltaByPixelOffset(
        pixelOffset: Offset,
        z: Int,
        currentLat: Double
    ): Pair<Double, Double> {
        val mapSize = 2.0.pow(z) * tileSize

        // 经度变化量（线性）
        val lngDelta = (pixelOffset.x / mapSize) * 360.0

        // 纬度变化量（考虑墨卡托投影的非线性）
        val currentLatRad = currentLat * PI / 180
        val currentY = (1.0 - ln(tan(currentLatRad) + 1.0 / cos(currentLatRad)) / PI) / 2.0 * mapSize
        val newY = currentY + pixelOffset.y
        val newYNormalized = newY / mapSize
        val newLatRad = atan(sinh(PI * (1.0 - 2.0 * newYNormalized)))
        val newLat = newLatRad * 180.0 / PI
        val latDelta = newLat - currentLat

        return Pair(latDelta, lngDelta)
    }

    // 实现 sinh 函数
    private fun sinh(x: Double): Double {
        return (exp(x) - exp(-x)) / 2.0
    }

    private val cache = mutableMapOf<String, ImageBitmap>()

    override suspend fun requestForImageBitmap(x: Int, y: Int, z: Int): ImageBitmap? {
        val loader = SingletonImageLoader.get(context)
        val pixel = cache["$x$y$z"]

        if (pixel == null) {
            val req = ImageRequest.Builder(context).data(
                data = "http://wprd03.is.autonavi.com/appmaptile?style=7&x=$x&y=$y&z=$z"
            ).build()

            val data = (loader.execute(req) as? SuccessResult)?.image

            return data?.toImageBitmap()?.apply {
                cache["$x$y$z"] = this
            }
        }
        return pixel
    }
}


expect fun Image.toImageBitmap(): ImageBitmap
