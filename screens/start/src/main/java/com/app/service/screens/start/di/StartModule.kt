package com.app.service.screens.start.di

import com.app.service.screens.start.presentation.StartViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val startModule = module {
	viewModel {
		StartViewModel(
			get()
		)
	}
}