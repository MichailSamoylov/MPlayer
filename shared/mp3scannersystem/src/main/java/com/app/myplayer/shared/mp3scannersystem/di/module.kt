package com.app.myplayer.shared.mp3scannersystem.di

import com.app.myplayer.shared.mp3scannersystem.domain.GetMP3FileListUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val mp3ScannerSystemModule = module {
	factory { GetMP3FileListUseCase(androidContext()) }
}

