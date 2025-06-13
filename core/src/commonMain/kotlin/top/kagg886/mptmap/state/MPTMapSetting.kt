package top.kagg886.mptmap.state

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

data class MPTMapSetting(
    val preloadTileSize: Int = 3,
    val dispatcher: CoroutineContext = Dispatchers.Default,
)
