package sample.app

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import coil3.Image
import coil3.toBitmap

actual fun Image.toImageBitmap(): ImageBitmap = this.toBitmap().asImageBitmap()
