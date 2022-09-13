package com.app.location_app

import android.app.Application
import com.app.location_app.di.ciceroneModule
import com.app.location_app.di.navigationModule
import com.app.screens.listofmusic.di.listOfMusicModule
import com.app.service.di.serviceModule
import com.app.service.screens.start.di.startModule
import com.app.service.shared.musicfinder.di.musicScannerModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : Application() {

	override fun onCreate() {
		super.onCreate()
		startKoin {
			androidLogger(Level.ERROR)
			androidContext(this@App)

			modules(
				ciceroneModule,
				navigationModule,
				startModule,
				listOfMusicModule,
				musicScannerModule,
				serviceModule,
			)
		}
	}
}