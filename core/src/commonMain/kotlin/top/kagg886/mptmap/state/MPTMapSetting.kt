package top.kagg886.mptmap.state

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

data class MPTMapSetting(
    val draggable: Boolean = true,
    val dispatcher: CoroutineContext = Dispatchers.Default,
    val speed: Float = 0.4f,
)
