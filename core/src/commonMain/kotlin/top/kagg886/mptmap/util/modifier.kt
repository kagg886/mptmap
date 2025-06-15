package top.kagg886.mptmap.util

import androidx.compose.ui.Modifier

internal fun Modifier.runIf(condition: Boolean, block: Modifier.() -> Modifier): Modifier = if (condition) block() else this

internal fun Modifier.runIf(condition: () -> Boolean, modifier: Modifier): Modifier = if (condition()) this then modifier else this
