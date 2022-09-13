package com.app.location_app

import android.app.Application
import com.app.location_app.di.ciceroneModule
import com.app.location_app.di.navigationModule
import com.app.myplayer.screens.start.di.startModule
import com.app.myplayer.shared.mp3scannersystem.di.mp3ScannerSystemModule
import com.app.screens.listofmusic.di.listOfMusicModule
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
				mp3ScannerSystemModule,
			)
		}
	}
}