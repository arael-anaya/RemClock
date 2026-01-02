package com.araelAnaya.remclock

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.araelAnaya.remclock.time.Time12
import com.araelAnaya.remclock.time.TimeInput

@Composable
fun SetTimePopup(
    isAlarmMode: Boolean, // The variable to toggle between Alarm and Bedtime text
    initialTime: Time12,
    onDismiss: () -> Unit,
    onSave: (Time12) -> Unit
) {
    var tempTime by remember { mutableStateOf(initialTime) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onSave(tempTime) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            // Dynamically set the title based on the mode
            Text(if (isAlarmMode) "Set Alarm" else "Set Bedtime")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimeInput(
                    value = tempTime,
                    // Keeps your existing arrow-update logic working perfectly
                    onChange = { update -> tempTime = update(tempTime) }
                )
            }
        }
    )
}