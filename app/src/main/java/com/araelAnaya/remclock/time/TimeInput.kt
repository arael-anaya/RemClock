package com.araelAnaya.remclock.time

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols

@Composable
fun TimeInput(
    value: Time12,
    onChange: ((Time12) -> Time12) -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = LocalConfiguration.current.locales[0]
    val amPm = DateFormatSymbols(locale).amPmStrings
    val amLabel = amPm.getOrNull(0) ?: "AM"
    val pmLabel = amPm.getOrNull(1) ?: "PM"

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        TimeStepper(
            display = value.hour.toString().padStart(2, '0'),
            onDelta = { d ->
                onChange { it.copy(hour = clampHour12(it.hour + d)) }
            }
        )

        Text(":", fontSize = 34.sp, fontWeight = FontWeight.Bold)

        TimeStepper(
            display = value.minute.toString().padStart(2, '0'),
            onDelta = { d ->
                onChange { it.copy(minute = clampMinute(it.minute + d)) }
            }
        )

        TimeStepper(
            display = if (value.isAm) amLabel else pmLabel,
            onDelta = {
                onChange { it.copy(isAm = !it.isAm) }
            }
        )
    }
}

@Composable
private fun TimeStepper(
    display: String,
    onDelta: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        HoldButton("▲") { onDelta(+1) }

        Spacer(Modifier.height(8.dp))

        Surface(
            tonalElevation = 2.dp,
            shape = MaterialTheme.shapes.medium
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(display, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(8.dp))

        HoldButton("▼") { onDelta(-1) }
    }
}


@Composable
private fun HoldButton(
    label: String,
    onHold: () -> Unit
) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .padding(4.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onHold()

                        val job = scope.launch {
                            delay(300)
                            while (isActive) {
                                onHold()
                                delay(80)
                            }
                        }

                        try {
                            awaitRelease()
                        } finally {
                            job.cancel()
                        }
                    }
                )
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
