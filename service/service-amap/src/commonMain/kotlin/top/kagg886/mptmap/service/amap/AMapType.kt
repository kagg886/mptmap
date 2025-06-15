package top.kagg886.mptmap.service.amap

enum class AMapType(internal val urlFormat: (Int, Int, Int) -> String) {
    NORMAL(
        urlFormat = { x, y, z ->
            "https://wprd03.is.autonavi.com/appmaptile?style=7&x=$x&y=$y&z=$z"
        }
    ),
    SATELLITE(
        urlFormat = { x, y, z ->
            "https://wprd03.is.autonavi.com/appmaptile?style=6&x=$x&y=$y&z=$z"
        }
    )
}
