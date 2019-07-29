package com.github.notificationcron.data.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.notificationcron.data.local.AppDatabase
import com.github.notificationcron.data.model.NotificationCron
import com.github.notificationcron.data.scheduleNextAlarm
import com.github.notificationcron.ui.showNotification

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val notificationCronId = intent.getLongExtra(NOTIFICATION_CRON_ID_EXTRA, Long.MIN_VALUE)
            if (notificationCronId > Long.MIN_VALUE) {
                val database = AppDatabase.getDatabase(context)
                val notificationCronDao = database.notificationCronDao()
                Thread(Runnable {
                    val notificationCron = notificationCronDao.findById(notificationCronId)
                    showNotification(context, notificationCron.notificationTitle, notificationCron.notificationText)
                    scheduleNextAlarm(context, notificationCronDao, notificationCron)
                }).start()
            }
        }
    }

    companion object {

        private const val ALARM_INTENT_ACTION = "com.github.notificationcron.ALARM"
        private const val NOTIFICATION_CRON_ID_EXTRA = "NOTIFICATION_CRON_ID_EXTRA"

        fun getPendingIntent(context: Context, notificationCron: NotificationCron): PendingIntent {
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.action = ALARM_INTENT_ACTION
            // a unique type is needed so that the alarm manager regards two intent as different (Intent.filterEquals)
            intent.type = "$notificationCron.id"
            intent.putExtra(NOTIFICATION_CRON_ID_EXTRA, notificationCron.id)
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        }
    }
}