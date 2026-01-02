package com.araelAnaya.remclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.araelAnaya.remclock.alarm.*
import com.araelAnaya.remclock.storage.AlarmModeStorage
import com.araelAnaya.remclock.time.findClosestRemTime
import com.araelAnaya.remclock.time.formatSleepDuration
import com.araelAnaya.remclock.time.minutesToTime12
import com.araelAnaya.remclock.ui.theme.RemClockTheme
import com.araelAnaya.remclock.viewmodel.MainViewModel
import androidx.lifecycle.SavedStateHandle
import com.araelAnaya.remclock.storage.repository.impl.AlarmModeRepositoryImpl
import com.araelAnaya.remclock.storage.repository.impl.AlarmSettingsRepositoryImpl
import com.araelAnaya.remclock.storage.repository.impl.RemSettingsRepositoryImpl
import com.araelAnaya.remclock.storage.repository.impl.SleepSettingsRepositoryImpl
import com.araelAnaya.remclock.storage.repository.impl.SleepStreakRepositoryImpl
import com.araelAnaya.remclock.viewmodel.model
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import androidx.compose.runtime.saveable.rememberSaveable
import com.araelAnaya.remclock.storage.morning.MorningCheckInStore
import com.araelAnaya.remclock.ui.MorningCheckInSheet


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RemClockTheme {
                val appContext = LocalContext.current.applicationContext
                val vmFactory = remember(appContext) {
                    object : AbstractSavedStateViewModelFactory(this@MainActivity, null) {
                        override fun <T : ViewModel> create(
                            key: String,
                            modelClass: Class<T>,
                            handle: SavedStateHandle
                        ): T {
                            @Suppress("UNCHECKED_CAST")
                            return MainViewModel(
                                appContext = appContext,
                                alarmRepo = AlarmSettingsRepositoryImpl(appContext),
                                sleepRepo = SleepSettingsRepositoryImpl(appContext),
                                remRepo = RemSettingsRepositoryImpl(appContext),
                                alarmModeRepo = AlarmModeRepositoryImpl(AlarmModeStorage(appContext)),
                                streakRepo = SleepStreakRepositoryImpl(appContext),
                                savedStateHandle = handle
                            )as T
                        }
                    }
                }


                val vm: MainViewModel = viewModel(factory = vmFactory)
                RemClockScreen(vm)




            }
        }
    }

    override fun onResume() {
        super.onResume()
        WakeConfidenceEvaluator.recordUserInteraction()
    }

    fun ensureExactAlarmPermission() {
        val alarmManager = getSystemService(android.app.AlarmManager::class.java)
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
        }
    }

    private val requestNotifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            // you can log this if you want
        }

    fun ensureNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true

        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemClockScreen(vm: MainViewModel) {
    val alarmTime by vm.alarmTime.collectAsState()
    val bedTime by vm.bedtime.collectAsState()
    val sleepMinutes by vm.sleepMinutes.collectAsState()
    val alarmMode by vm.alarmMode.collectAsState()
    val remEnabled by vm.remEnabled.collectAsState()
    val remCycleMinutes by vm.remCycleMinutes.collectAsState()
    val sleepStreak by vm.sleepStreak.collectAsState()
    val streakStage by vm.streakStage.collectAsState()
    var showBedtimeDialog by remember { mutableStateOf(false) }
    var showAlarmTimeDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val appContext = context.applicationContext
    val checkInStore = remember(appContext) {
        MorningCheckInStore(appContext)
    }
    var showCheckIn by rememberSaveable {
        mutableStateOf(checkInStore.shouldShow())
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("RemClock", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- HERO SECTION: ALARM TIME ---
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("WAKE UP AT", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                Text(
                    text = "${alarmTime.hour.toString().padStart(2, '0')}:${alarmTime.minute.toString().padStart(2, '0')} ${if (alarmTime.isAm) "AM" else "PM"}",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(onClick = { showAlarmTimeDialog = true }) {
                    Text("Edit Alarm Time")
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        val activity = context as? MainActivity ?: return@Button
                        activity.ensureNotificationPermission()
                        activity.ensureExactAlarmPermission()
                        val (hour24, minute) = vm.getNextAlarmTime24()
                        when (alarmMode) {

                            AlarmMode.EXACT -> {
                                AlarmScheduler.scheduleAlarm(
                                    context = context,
                                    hour24 = hour24,
                                    minute = minute
                                )
                            }

                            AlarmMode.SMART_WINDOW -> {
                                val targetMillis = java.util.Calendar.getInstance().apply {
                                    set(java.util.Calendar.HOUR_OF_DAY, hour24)
                                    set(java.util.Calendar.MINUTE, minute)
                                    set(java.util.Calendar.SECOND, 0)
                                    if (timeInMillis <= System.currentTimeMillis()) {
                                        add(java.util.Calendar.DAY_OF_YEAR, 1)
                                    }
                                }.timeInMillis

                                val now = System.currentTimeMillis()
                                val window = WakeWindowCalculator.fromTargetTime(
                                    nowMillis = now,
                                    targetMillis = targetMillis
                                )

                                AlarmScheduler.scheduleWakeWindow(
                                    context,
                                    window.windowStartMillis,
                                    window.hardStopMillis
                                )
                            }
                        }

                    },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Set Alarm")
                }
                OutlinedButton(
                    onClick = { AlarmScheduler.cancelAlarm(context) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Cancel")
                }
            }
            InfoCard(title = "Sleep Duration", icon = Icons.Default.Star) {
                val (hours, mins) = formatSleepDuration(sleepMinutes)
                Text("$hours hours $mins minutes", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Bedtime: ${bedTime.hour}:${bedTime.minute.toString().padStart(2, '0')} ${if (bedTime.isAm) "AM" else "PM"}")
                    TextButton(onClick = { showBedtimeDialog = true }) {
                        Text("Change Bedtime")
                    }
                }
            }
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("REM Smart Mode", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = if (alarmMode == AlarmMode.SMART_WINDOW) "Waking during light sleep." else "Alarm rings exactly on time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = alarmMode == AlarmMode.SMART_WINDOW,
                            onCheckedChange = { vm.setAlarmMode(if (it) AlarmMode.SMART_WINDOW else AlarmMode.EXACT) }
                        )
                    }
                    if (remEnabled && alarmMode == AlarmMode.SMART_WINDOW) {
                        val remWakeTimes by vm.remWakeTimes.collectAsState()
                        val closestRemMinutes = findClosestRemTime(remWakeTimes, alarmTime)
                        val currentAlarmMinutes = remember(alarmTime) {
                            val h24 = when {
                                alarmTime.isAm && alarmTime.hour == 12 -> 0
                                !alarmTime.isAm && alarmTime.hour != 12 -> alarmTime.hour + 12
                                else -> alarmTime.hour
                            }
                            h24 * 60 + alarmTime.minute
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "REM Cycle Length",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            "$remCycleMinutes minutes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = remCycleMinutes.toFloat(),
                            onValueChange = { vm.setRemCycleMinutes(it.toInt()) },
                            valueRange = 70f..110f,
                            steps = 7, // 5-minute increments
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Text(
                            "If you wake up groggy, try adjusting this.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("Recommended times:", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 16.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            remWakeTimes.take(3).forEach { minutes ->
                                val t = minutesToTime12(minutes)
                                val isSelected = minutes == currentAlarmMinutes
                                val isBest = minutes == closestRemMinutes
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { vm.onRemTimeClicked(minutes) },
                                    label = {
                                        Text(
                                            text = "${t.hour}:${t.minute.toString().padStart(2, '0')} ${if (t.isAm) "AM" else "PM"}",
                                            fontWeight = if (isBest) FontWeight.ExtraBold else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                        }
                    }
                    Text(
                        "Sleep streak: ${sleepStreak} nights",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        when (streakStage) {
                            model.StreakStage.EMBER -> "Your routine is catching fire 🔥"
                            model.StreakStage.CAMPFIRE -> "You’re building consistency 🌙"
                            model.StreakStage.BONFIRE -> "Sleep mastery unlocked 🌕"
                            else -> "Start a streak tonight"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
    if (showCheckIn) {
        MorningCheckInSheet(
            onMoodSelected = { mood ->
                vm.submitMorningVibe(mood)
                checkInStore.markCompleted()
                showCheckIn = false
            },
            onDismiss = {
                showCheckIn = false
            }
        )
    }
    if (showAlarmTimeDialog) {
        SetTimePopup(
            isAlarmMode = true,
            initialTime = alarmTime,
            onDismiss = { showAlarmTimeDialog = false },
            onSave = { newTime ->
                vm.manualTimeBackupMinutes = null
                vm.updateAlarmTime { newTime }
                showAlarmTimeDialog = false
            }
        )
    }
    if (showBedtimeDialog) {
        SetTimePopup(
            isAlarmMode = false,
            initialTime = bedTime,
            onDismiss = { showBedtimeDialog = false },
            onSave = { newTime ->
                vm.setBedtime(newTime)
                showBedtimeDialog = false
            }
        )
    }
}
@Composable
fun InfoCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}


