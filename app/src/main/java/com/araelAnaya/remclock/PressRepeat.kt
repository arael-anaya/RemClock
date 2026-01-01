package com.araelAnaya.remclock

import androidx.compose.runtime.*
import kotlinx.coroutines.delay

@Composable
fun rememberPressRepeat(
    initialDelayMs: Long = 0L,      // IMPORTANT
    repeatDelayMs: Long = 80L,
    onRepeat: () -> Unit
): Pair<() -> Unit, () -> Unit> {

    var pressed by remember { mutableStateOf(false) }

    LaunchedEffect(pressed) {
        if (!pressed) return@LaunchedEffect

        // fire immediately
        onRepeat()

        delay(initialDelayMs)

        while (pressed) {
            delay(repeatDelayMs)
            onRepeat()
        }
    }

    val start = { pressed = true }
    val stop = { pressed = false }

    return start to stop
}
