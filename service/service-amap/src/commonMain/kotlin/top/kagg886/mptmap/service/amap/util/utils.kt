package top.kagg886.mptmap.service.amap.util

import kotlin.math.exp

internal fun sinh(x: Double): Double {
    return (exp(x) - exp(-x)) / 2.0
}
