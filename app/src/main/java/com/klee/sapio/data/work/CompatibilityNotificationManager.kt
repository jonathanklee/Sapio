package com.klee.sapio.data.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.klee.sapio.R
import com.klee.sapio.domain.model.InstalledApplication
import com.klee.sapio.ui.view.EvaluationsActivity

class CompatibilityNotificationManager(
    private val context: Context
) {
    fun show(app: InstalledApplication) {
        notify(app)
    }

    private fun notify(app: InstalledApplication) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(notificationManager)

        val pendingIntent = createEvaluationPendingIntent(app, shareImmediately = false)
        val sharePendingIntent = createEvaluationPendingIntent(app, shareImmediately = true)

        val notification = buildNotification(app, pendingIntent, sharePendingIntent)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun ensureChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.compatibility_check_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.compatibility_check_channel_description)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun createEvaluationPendingIntent(
        app: InstalledApplication,
        shareImmediately: Boolean
    ): PendingIntent {
        val intent = Intent(context, EvaluationsActivity::class.java).apply {
            putExtra(EvaluationsActivity.EXTRA_PACKAGE_NAME, app.packageName)
            putExtra(EvaluationsActivity.EXTRA_APP_NAME, app.name)
            if (shareImmediately) {
                putExtra(EvaluationsActivity.EXTRA_SHARE_IMMEDIATELY, true)
                putExtra(EvaluationsActivity.EXTRA_NOTIFICATION_ID, NOTIFICATION_ID)
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val requestCode = if (shareImmediately) {
            app.packageName.hashCode() + 1
        } else {
            app.packageName.hashCode()
        }

        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildNotification(
        app: InstalledApplication,
        pendingIntent: PendingIntent,
        sharePendingIntent: PendingIntent
    ) = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification_info)
        .setLargeIcon(app.icon.toBitmap())
        .setContentTitle(context.getString(R.string.compatibility_check_notification_title))
        .setContentText(
            context.getString(
                R.string.compatibility_check_notification_body,
                app.name
            )
        )
        .setStyle(
            NotificationCompat.BigTextStyle().bigText(
                context.getString(
                    R.string.compatibility_check_notification_body,
                    app.name
                )
            )
        )
        .setAutoCancel(false)
        .setContentIntent(pendingIntent)
        .addAction(
            android.R.drawable.ic_menu_share,
            context.getString(R.string.compatibility_check_notification_share_action),
            sharePendingIntent
        )
        .build()

    private companion object {
        const val CHANNEL_ID = "compatibility_check"
        const val NOTIFICATION_ID = 2201
    }
}
