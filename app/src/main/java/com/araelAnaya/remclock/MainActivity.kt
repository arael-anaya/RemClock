package com.araelAnaya.remclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.araelAnaya.remclock.ui.theme.RemClockTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RemClockTheme {
                var time by remember { mutableStateOf(Time12(hour = 7, minute = 0, isAm = true)) }

                val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
                val amPm = java.text.DateFormatSymbols(locale).amPmStrings
                val amLabel = amPm.getOrNull(0) ?: "AM"
                val pmLabel = amPm.getOrNull(1) ?: "PM"

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "RemClock",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')} ${if (time.isAm) amLabel else pmLabel}",
                        fontSize = 28.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    TimeInput(
                        value = time,
                        onChange = { time = it }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(onClick = {
                        ensureExactAlarmPermission()
                        val (h24, m) = time.to24Hour()
                        scheduleAlarm(h24, m)
                    }) {
                        Text("Set Alarm")
                    }
                }
            }
        }

    }
     private fun scheduleAlarm(hour: Int, minute: Int) {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)

            if (before(java.util.Calendar.getInstance())) {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = android.content.Intent(this, AlarmReceiver::class.java)
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            this,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                    android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            getSystemService(android.content.Context.ALARM_SERVICE)
                    as android.app.AlarmManager

        alarmManager.setExact(
            android.app.AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
    private fun ensureExactAlarmPermission() {
        val alarmManager = getSystemService(android.app.AlarmManager::class.java)
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = android.content.Intent(
                android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
            )
            startActivity(intent)
        }
    }



}

