package top.kagg886.mptmap.data

data class LatLng(
    val lat: Double,
    val lng: Double
) {
    init {
        require(lat in -90.0..90.0) { "Latitude must be between -90.0 and 90.0" }
        require(lng in -180.0..180.0) { "Longitude must be between -180.0 and 180.0" }
    }
}
