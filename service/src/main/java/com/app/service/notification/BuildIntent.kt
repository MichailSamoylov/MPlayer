package com.app.service.notification

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import androidx.core.os.BuildCompat

const val ACTION_CANCEL = "ACTION_CANCEL"
const val PAUSE_OR_PLAY = "PAUSE_OR_PLAY"
const val NEXT_TREK = "NEXT_TREK"
const val PREV_TREK = "PREV_TREK"

private fun createIntent(
	context: Context,
	service: Service,
	action: String
): Intent {
	val intent = Intent(context, service::class.java)
	intent.action = action
	return intent
}

fun createPendingIntent(
	context: Context,
	service: Service,
	action: String
): PendingIntent {
	val intent = createIntent(context, service, action)
	var flags = PendingIntent.FLAG_UPDATE_CURRENT
	if (BuildCompat.isAtLeastS()) {
		flags = flags or PendingIntent.FLAG_MUTABLE
	}

	return PendingIntent.getService(context, 0, intent, flags)
}