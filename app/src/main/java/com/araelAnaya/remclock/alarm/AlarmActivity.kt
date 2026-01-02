package com.araelAnaya.remclock.alarm

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.araelAnaya.remclock.ui.theme.RemClockTheme
import android.view.WindowManager


class AlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContext = applicationContext

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )


        setContent {
            RemClockTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Alarm",
                        fontSize = 28.sp
                    )

                    Spacer(Modifier.height(32.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            stopService(
                                Intent(
                                    appContext,
                                    AlarmForegroundService::class.java
                                )
                            )
                            finish()
                        }
                    ) {
                        Text("Dismiss")
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            stopService(
                                Intent(
                                    appContext,
                                    AlarmForegroundService::class.java
                                )
                            )

                            AlarmScheduler.snooze(
                                context = appContext,
                                alarmMode = AlarmMode.EXACT
                            )

                            finish()
                        }
                    ) {
                        Text("Snooze")
                    }
                }
            }

        }
    }
    private fun stopAlarm() {
        stopService(Intent(this, AlarmForegroundService::class.java))
        AlarmNotification.dismiss(this)
    }

}


