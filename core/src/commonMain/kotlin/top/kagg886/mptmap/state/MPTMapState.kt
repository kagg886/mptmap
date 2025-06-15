package top.kagg886.mptmap.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import top.kagg886.mptmap.data.LatLng

class MPTMapState(
    lat: Double,
    lng: Double,
    @androidx.annotation.FloatRange(0.0, 100.0) zoom: Float = 80f,
) {

    constructor(
        latLng: LatLng,
        @androidx.annotation.FloatRange(0.0, 100.0) zoom: Float = 80f,
    ) : this(
        latLng.lat,
        latLng.lng,
        zoom
    )

    init {
        require(lat in -90.0..90.0) { "lat must be in -90.0..90.0" }
        require(lng in -180.0..180.0) { "lng must be in -180.0..180.0" }
    }

    var lat by mutableStateOf(lat)
    var lng by mutableStateOf(lng)

    @delegate:androidx.annotation.FloatRange(0.0, 100.0)
    var zoom by mutableStateOf(zoom)
        internal set

    fun zoom(zoom: Float) = apply {
        this@MPTMapState.zoom = zoom.coerceIn(0f, 100f)
    }
}
