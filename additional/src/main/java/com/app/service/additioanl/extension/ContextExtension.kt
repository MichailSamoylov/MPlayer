package com.app.service.additioanl.extension

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context

@Suppress("DEPRECATION")
inline fun <reified T> Context.isServiceRunning() =
	(getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
		.getRunningServices(Integer.MAX_VALUE)
		.any { it.service.className == T::class.java.name }

@Suppress("DEPRECATION")
fun Context.getNotificationManager() =
	(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)