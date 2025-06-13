package sample.app

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader

class SampleApplication: Application(), SingletonImageLoader.Factory {
    override fun newImageLoader(context: PlatformContext) = ImageLoader.Builder(context).applyCustomConfig().build()
}
