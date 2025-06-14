import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.window.ComposeViewport
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.fetch.Response
import sample.app.App
import sample.app.applyCustomConfig
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.body ?: return
    SingletonImageLoader.setSafe {
        ImageLoader.Builder(PlatformContext.INSTANCE).applyCustomConfig().build()
    }
    ComposeViewport(body) {
        val fontFamilyResolver = LocalFontFamilyResolver.current
        val fontsLoaded = remember { mutableStateOf(false) }

        if (fontsLoaded.value) {
            App()
        } else {
            Text("Loading Fonts...")
        }

        LaunchedEffect(Unit) {
            val fontBytes = loadRes("./msyh.ttc").toByteArray()
            val fontFamily = FontFamily(listOf(Font("微软雅黑", fontBytes)))
            fontFamilyResolver.preload(fontFamily)
            fontsLoaded.value = true
        }
    }
}

suspend fun loadRes(url: String): ArrayBuffer {
    return window.fetch(url).await<Response>().arrayBuffer().await()
}

fun ArrayBuffer.toByteArray(): ByteArray {
    val source = Int8Array(this, 0, byteLength)
    return jsInt8ArrayToKotlinByteArray(source)
}

@JsFun(
    """(src, size, dstAddr) => {
        const mem8 = new Int8Array(wasmExports.memory.buffer, dstAddr, size);
        mem8.set(src);
    }
"""
)
internal external fun jsExportInt8ArrayToWasm(src: Int8Array, size: Int, dstAddr: Int)

internal fun jsInt8ArrayToKotlinByteArray(x: Int8Array): ByteArray {
    val size = x.length

    @OptIn(UnsafeWasmMemoryApi::class)
    return withScopedMemoryAllocator { allocator ->
        val memBuffer = allocator.allocate(size)
        val dstAddress = memBuffer.address.toInt()
        jsExportInt8ArrayToWasm(x, size, dstAddress)
        ByteArray(size) { i -> (memBuffer + i).loadByte() }
    }
}
