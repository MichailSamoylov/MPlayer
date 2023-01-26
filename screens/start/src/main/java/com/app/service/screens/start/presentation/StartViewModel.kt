package com.app.service.screens.start.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StartViewModel(
	private val router: StartRouter
) : ViewModel() {

	fun navigateToListScreen() {
		viewModelScope.launch {
			delay(2500)
			router.navigateToListScreen()
		}
	}

	fun exit() {
		router.exit()
	}
}