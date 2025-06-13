import androidx.compose.ui.window.ComposeUIViewController
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import platform.UIKit.UIViewController
import sample.app.App
import sample.app.applyCustomConfig

fun MainViewController(): UIViewController = run {
    SingletonImageLoader.setSafe {
        ImageLoader.Builder(PlatformContext.INSTANCE).applyCustomConfig().build()
    }
    ComposeUIViewController { App() }
}
