package com.app.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.app.service.additioanl.bitmapconverter.convertResourceToBitmap
import com.app.service.service.R

private const val CHANNEL_ID = "MPLAYER_CHANNEL_ID"

internal fun createNotificationChannel(context: Context, notificationManager: NotificationManager) {
	val name = context.resources.getString(R.string.channel_name)
	val descriptionText = context.resources.getString(R.string.channel_description)
	val importance = NotificationManager.IMPORTANCE_DEFAULT
	val channel = NotificationChannel(CHANNEL_ID, name, importance).apply { description = descriptionText }
	notificationManager.createNotificationChannel(channel)
}

fun Service.createNotification(notificationData: NotificationData, mediaSession: MediaSessionCompat): Notification {
	val service = this
	val canselIntent = createPendingIntent(service, service, ACTION_CANCEL)
	val prevTrekIntent = createPendingIntent(service, service, PREV_TREK)
	val pauseOrPlayIntent = createPendingIntent(service, service, PAUSE_OR_PLAY)
	val nextTrekIntent = createPendingIntent(service, service, NEXT_TREK)

	return NotificationCompat.Builder(service, CHANNEL_ID)
		.setSmallIcon(R.drawable.ic_notification)
		.setLargeIcon(convertResourceToBitmap(service, R.drawable.ic_notification))
		.setContentTitle(notificationData.title)
		.setPriority(NotificationCompat.FLAG_FOREGROUND_SERVICE)
		.setCategory(NotificationCompat.CATEGORY_PROGRESS)
		.addAction(R.drawable.ic_cansel, null, canselIntent)
		.addAction(R.drawable.ic_reset_trek_prev, null, prevTrekIntent)
		.addAction(notificationData.iconUri, null, pauseOrPlayIntent)
		.addAction(R.drawable.ic_reset_trek_next, null, nextTrekIntent)
		.setStyle(
			androidx.media.app.NotificationCompat.MediaStyle()
				.setShowActionsInCompactView()
				.setMediaSession(mediaSession.sessionToken)
		)
		.setAutoCancel(true)
		.build()
}