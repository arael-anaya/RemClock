package com.araelAnaya.remclock

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BedtimeDialog(
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
            Text("Set Bedtime")
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimeInput(
                    value = tempTime,
                    onChange = { update -> tempTime = update(tempTime) }
                )
            }
        }
    )
}
