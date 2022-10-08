package com.app.screens.listofmusic.ui.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.app.service.service.MediaService

class MyServiceConnection(private val doOnConnected:(MediaService.LocalBinder)->Unit, private val doOnDisconnected:()->Unit): ServiceConnection {

	override fun onServiceConnected(className: ComponentName, service: IBinder) {
		val binder = service as MediaService.LocalBinder
		doOnConnected(binder)
	}

	override fun onServiceDisconnected(arg0: ComponentName) {
		doOnDisconnected()
	}
}
