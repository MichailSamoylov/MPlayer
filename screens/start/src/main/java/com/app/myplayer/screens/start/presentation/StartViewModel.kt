package com.app.myplayer.screens.start.presentation

import androidx.lifecycle.ViewModel

class StartViewModel(
	private val router: StartRouter
):ViewModel() {

	fun resetRootScreen(){
		router.resetRootScreen()
	}

	fun exit(){
		router.exit()
	}
}