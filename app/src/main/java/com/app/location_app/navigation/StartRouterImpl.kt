package com.app.location_app.navigation

import com.app.screens.listofmusic.getMusicListScreen
import com.app.service.screens.start.presentation.StartRouter
import com.github.terrakok.cicerone.Router

class StartRouterImpl(private val router: Router) : StartRouter {

	override fun navigateToListScreen() {
		router.newRootScreen(getMusicListScreen())
	}

	override fun exit() {
		router.exit()
	}
}