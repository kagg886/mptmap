package top.kagg886.mptmap.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

internal fun Modifier.runIf(condition: Boolean, block: Modifier.() -> Modifier): Modifier = if (condition) block() else this

internal fun Modifier.runIf(condition: () -> Boolean, modifier: Modifier): Modifier = if (condition()) this then modifier else this

@Composable
fun <T> rememberPrevious(value: T): T? {
    val previous = remember { mutableStateOf<T?>(null) }
    val current = remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        if (current.value != value) {
            previous.value = current.value
            current.value = value
        }
    }

    return previous.value
}
