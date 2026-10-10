package com.klee.sapio

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.klee.sapio.domain.model.InstalledApplication
import com.klee.sapio.work.CompatibilityNotificationManager
import com.klee.sapio.ui.view.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class CompatibilityNotificationManagerTest {

    private val context = RuntimeEnvironment.getApplication()
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Test
    fun `show posts a notification naming the application`() {
        CompatibilityNotificationManager(context).show(app())

        val notification = shadowOf(notificationManager).allNotifications.single()
        val body = context.getString(R.string.compatibility_check_notification_body, APP_NAME)

        assertEquals(
            context.getString(R.string.compatibility_check_notification_title),
            shadowOf(notification).contentTitle
        )
        assertEquals(body, shadowOf(notification).contentText)
    }

    @Test
    fun `show creates the compatibility channel`() {
        CompatibilityNotificationManager(context).show(app())

        val channel = notificationManager.notificationChannels.single()

        assertEquals(
            context.getString(R.string.compatibility_check_channel_name),
            channel.name
        )
        assertEquals(
            context.getString(R.string.compatibility_check_channel_description),
            channel.description
        )
    }

    @Test
    fun `show offers a share action next to the content intent`() {
        CompatibilityNotificationManager(context).show(app())

        val notification = shadowOf(notificationManager).allNotifications.single()

        assertNotNull(notification.contentIntent)
        assertEquals(1, notification.actions.size)
        assertEquals(
            context.getString(R.string.compatibility_check_notification_share_action),
            notification.actions.single().title
        )
    }

    @Test
    fun `show survives an application whose icon cannot be read`() {
        CompatibilityNotificationManager(context).show(app())

        assertTrue(shadowOf(notificationManager).allNotifications.isNotEmpty())
    }

    @Test
    fun `content intent targets the application without sharing`() {
        CompatibilityNotificationManager(context).show(app())

        val intent = shadowOf(notification().contentIntent).savedIntent

        assertEquals(PACKAGE_NAME, intent.getStringExtra(MainActivity.EXTRA_PACKAGE_NAME))
        assertEquals(APP_NAME, intent.getStringExtra(MainActivity.EXTRA_APP_NAME))
        assertFalse(intent.getBooleanExtra(MainActivity.EXTRA_SHARE_IMMEDIATELY, false))
    }

    @Test
    fun `share action asks the activity to share straight away`() {
        CompatibilityNotificationManager(context).show(app())

        val intent = shadowOf(notification().actions.single().actionIntent).savedIntent

        assertEquals(PACKAGE_NAME, intent.getStringExtra(MainActivity.EXTRA_PACKAGE_NAME))
        assertTrue(intent.getBooleanExtra(MainActivity.EXTRA_SHARE_IMMEDIATELY, false))
        assertTrue(intent.getIntExtra(MainActivity.EXTRA_NOTIFICATION_ID, -1) > 0)
    }

    private fun notification() = shadowOf(notificationManager).allNotifications.single()

    private fun app(packageName: String = PACKAGE_NAME) =
        InstalledApplication(APP_NAME, packageName, "1.0.0")

    private companion object {
        const val APP_NAME = "Sapio"
        const val PACKAGE_NAME = "com.klee.sapio.sample"
    }
}
