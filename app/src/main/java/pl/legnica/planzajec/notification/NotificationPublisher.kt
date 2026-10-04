package pl.legnica.planzajec.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import pl.legnica.planzajec.MainActivity
import pl.legnica.planzajec.R
import pl.legnica.planzajec.domain.notification.NotificationState
import pl.legnica.planzajec.parser.model.LessonType

object NotificationPublisher {

    const val NOTIFICATION_ID = 1001
    const val CHANNEL_ID = "cwup_next_lesson_channel"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Następne zajęcia"
            val descriptionText = "Stałe powiadomienie informujące o najbliższych zajęciach"
            val importance = NotificationManager.IMPORTANCE_LOW // No sound, no vibration
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun updateNotification(context: Context, state: NotificationState) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (!state.shouldShow) {
            notificationManager.cancel(NOTIFICATION_ID)
            return
        }

        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val accentColor = when (state.lessonType) {
            LessonType.LECTURE -> 0xFF4F46E5.toInt()
            LessonType.EXERCISE -> 0xFF10B981.toInt()
            LessonType.LABORATORY -> 0xFFF97316.toInt()
            LessonType.SEMINAR -> 0xFF8B5CF6.toInt()
            LessonType.PROJECT -> 0xFF14B8A6.toInt()
            LessonType.WORKSHOP -> 0xFF06B6D4.toInt()
            LessonType.FOREIGN_LANGUAGE -> 0xFF8B5CF6.toInt()
            LessonType.OTHER -> 0xFF00D2FF.toInt()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasPermission) return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(state.title)
            .setContentText(state.content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(state.expandedText.ifBlank { state.content }))
            .setColor(accentColor)
            .setColorized(true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Permission revoked concurrently
        }
    }

    fun dismiss(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
