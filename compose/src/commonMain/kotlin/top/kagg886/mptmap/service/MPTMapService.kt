package top.kagg886.mptmap.service

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap

interface MPTMapService {
    /**
     * 单个瓦片的大小
     */
    val tileSize: Int

    /**
     * z轴的范围。
     */
    val zoomRange: IntRange


    /**
     * 根据经纬度和z轴获取x和y参数。
     */
    fun getTileParam(
        lat: Double,
        lng: Double,
        z: Int
    ): Pair<Int, Int>

    /**
     * 根据经纬度和z轴获取该经纬度在瓦片内的像素坐标
     */
    fun getPixelOffsetByLatLng(
        lat: Double,
        lng: Double,
        z: Int
    ): Offset

    /**
     * 获取bitmap实现
     */
    suspend fun requestForImageBitmap(x: Int,y: Int,z: Int): ImageBitmap?

    /**
     * 根据像素偏移量、z值、当前纬度 获取经纬度偏移量。
     */
    fun getLatLngDeltaByPixelOffset(pixelOffset: Offset, z: Int, currentLat: Double): Pair<Double, Double>
}
