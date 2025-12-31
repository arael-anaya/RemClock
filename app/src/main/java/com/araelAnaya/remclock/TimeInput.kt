package com.araelAnaya.remclock

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DateFormatSymbols

@Composable
fun TimeInput(
    value: Time12,
    onChange: (Time12) -> Unit,
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
            label = "Hour",
            display = value.hour.toString().padStart(2, '0'),
            onUp = { onChange(value.copy(hour = clampHour12(value.hour + 1))) },
            onDown = { onChange(value.copy(hour = clampHour12(value.hour - 1))) }
        )

        Text(
            text = ":",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold
        )

        TimeStepper(
            label = "Minute",
            display = value.minute.toString().padStart(2, '0'),
            onUp = { onChange(value.copy(minute = clampMinute(value.minute + 1))) },
            onDown = { onChange(value.copy(minute = clampMinute(value.minute - 1))) }
        )

        TimeStepper(
            label = "AM/PM",
            display = if (value.isAm) amLabel else pmLabel,
            onUp = { onChange(value.copy(isAm = !value.isAm)) },
            onDown = { onChange(value.copy(isAm = !value.isAm)) }
        )
    }
}

@Composable
private fun TimeStepper(
    label: String,
    display: String,
    onUp: () -> Unit,
    onDown: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = onUp,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
        ) { Text("▲") }

        Spacer(Modifier.height(8.dp))

        Surface(
            tonalElevation = 2.dp,
            shape = MaterialTheme.shapes.medium
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = display,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onDown,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
        ) { Text("▼") }
    }
}
