import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import sample.app.App
import sample.app.applyCustomConfig
import java.awt.Dimension

fun main() = run {
    SingletonImageLoader.setSafe {
        ImageLoader.Builder(PlatformContext.INSTANCE).applyCustomConfig().build()
    }
    application {
        Window(
            title = "sample",
            state = rememberWindowState(width = 800.dp, height = 600.dp),
            onCloseRequest = ::exitApplication,
        ) {
            window.minimumSize = Dimension(350, 600)
            App()
        }
    }
}
