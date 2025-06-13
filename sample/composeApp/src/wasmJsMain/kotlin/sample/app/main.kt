import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import kotlinx.browser.document
import sample.app.App
import sample.app.applyCustomConfig

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.body ?: return
    SingletonImageLoader.setSafe {
        ImageLoader.Builder(PlatformContext.INSTANCE).applyCustomConfig().build()
    }
    ComposeViewport(body) {
        App()
    }
}
