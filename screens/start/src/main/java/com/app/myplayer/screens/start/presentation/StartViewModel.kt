package com.app.myplayer.screens.start.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StartViewModel(
	private val router: StartRouter
) : ViewModel() {

	fun resetRootScreen() {
		viewModelScope.launch {
			delay(2500)
			router.resetRootScreen()
		}
	}

	fun exit() {
		router.exit()
	}
}