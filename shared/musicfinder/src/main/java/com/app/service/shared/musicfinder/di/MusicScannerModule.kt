package com.app.service.shared.musicfinder.di

import com.app.service.shared.musicfinder.domain.GetMusicFileListUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val musicScannerModule = module {
	factory { GetMusicFileListUseCase(androidContext()) }
}

