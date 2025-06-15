package top.kagg886.mptmap.service.amap

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import top.kagg886.mptmap.service.MPTMapService
import top.kagg886.mptmap.service.amap.util.sinh
import kotlin.math.*

class AMapService(private val fetcher: suspend (String) -> ImageBitmap?): MPTMapService {
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

    override suspend fun requestForImageBitmap(x: Int, y: Int, z: Int): ImageBitmap? {
        return fetcher("https://wprd03.is.autonavi.com/appmaptile?style=7&x=$x&y=$y&z=$z")
    }
}
