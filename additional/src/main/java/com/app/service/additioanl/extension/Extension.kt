package com.app.service.additioanl.extension

import android.util.Log

fun doThrowableException(logTag: String, throwableAction: () -> Unit, catchAction: () -> Unit) {
	try {
		throwableAction()
	} catch (e: Exception) {
		Log.e(logTag, e.toString())
		catchAction()
	}
}