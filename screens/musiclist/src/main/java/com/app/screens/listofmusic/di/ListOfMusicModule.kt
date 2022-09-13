package com.app.screens.listofmusic.di

import com.app.screens.listofmusic.presentation.ListOfMusicViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val listOfMusicModule = module {
	viewModel {
		ListOfMusicViewModel(
			get(),
			get()
		)
	}
}