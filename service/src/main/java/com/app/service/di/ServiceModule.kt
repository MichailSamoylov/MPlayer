package com.app.service.di

import com.app.service.additioanl.extension.getNotificationManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val serviceModule = module {
	single { androidContext().getNotificationManager() }
}