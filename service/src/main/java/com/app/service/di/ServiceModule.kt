package com.app.service.di

import com.app.service.additioanl.extension.getNotificationManager
import com.app.service.notification.createNotificationChannel
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext

val serviceModule = module {
	single { createNotificationChannel(androidContext(), androidContext().getNotificationManager()) }
}