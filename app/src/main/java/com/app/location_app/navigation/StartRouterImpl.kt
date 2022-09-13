package com.app.location_app.navigation

import com.app.myplayer.screens.start.presentation.StartRouter
import com.app.screens.listofmusic.getMusicListScreen
import com.github.terrakok.cicerone.Router

class StartRouterImpl(private val router: Router) : StartRouter {

	override fun resetRootScreen() {
		router.newRootScreen(getMusicListScreen())
	}

	override fun exit() {
		router.exit()
	}
}