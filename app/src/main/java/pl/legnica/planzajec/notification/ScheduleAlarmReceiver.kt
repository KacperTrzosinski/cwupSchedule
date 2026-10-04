package pl.legnica.planzajec.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pl.legnica.planzajec.data.preferences.UserPreferencesRepository
import pl.legnica.planzajec.data.repository.ScheduleRepository
import pl.legnica.planzajec.domain.notification.NotificationSchedulerEngine
import pl.legnica.planzajec.domain.notification.ScheduledAlarm
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

@AndroidEntryPoint
class ScheduleAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: ScheduleRepository

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    private val engine = NotificationSchedulerEngine()

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val isTargetAction = action == ACTION_TRIGGER_UPDATE ||
            action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED

        if (!isTargetAction) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                updateScheduleNotification(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun updateScheduleNotification(context: Context) {
        val prefs = preferencesRepository.userPreferencesFlow.first()
        if (!prefs.notificationsEnabled || prefs.selectedGroupCode == null) {
            NotificationPublisher.dismiss(context)
            pl.legnica.planzajec.widget.ScheduleGlanceWidget.updateWidget(context)
            return
        }

        val groupCode = prefs.selectedGroupCode
        val lessons = repository.getLessons(groupCode, prefs.selectedSubgroup).first()
        val now = LocalDateTime.now()

        val plan = engine.computePlan(lessons, now)

        // 1. Update system notification in place
        NotificationPublisher.updateNotification(context, plan.state)

        // 2. Update home screen widget
        pl.legnica.planzajec.widget.ScheduleGlanceWidget.updateWidget(context)

        // 3. Schedule next alarms
        scheduleAlarms(context, plan.alarms)
    }

    private fun scheduleAlarms(context: Context, alarms: List<ScheduledAlarm>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (alarm in alarms) {
            val intent = Intent(context, ScheduleAlarmReceiver::class.java).apply {
                action = ACTION_TRIGGER_UPDATE
            }

            val requestCode = (alarm.triggerTime.hour * 100 + alarm.triggerTime.minute)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerMillis = alarm.triggerTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        }
    }

    companion object {
        const val ACTION_TRIGGER_UPDATE = "pl.legnica.planzajec.ACTION_TRIGGER_NOTIFICATION_UPDATE"

        fun triggerImmediateUpdate(context: Context) {
            val intent = Intent(context, ScheduleAlarmReceiver::class.java).apply {
                action = ACTION_TRIGGER_UPDATE
            }
            context.sendBroadcast(intent)
        }
    }
}
