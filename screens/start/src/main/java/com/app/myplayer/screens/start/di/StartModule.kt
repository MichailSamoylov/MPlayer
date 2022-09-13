package com.app.myplayer.screens.start.di

import com.app.myplayer.screens.start.presentation.StartViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val startModule = module {
	viewModel {
		StartViewModel(
			get()
		)
	}
}