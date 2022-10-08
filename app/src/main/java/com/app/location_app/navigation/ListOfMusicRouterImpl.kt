package com.app.location_app.navigation

import com.app.screens.listofmusic.presentation.ListOfMusicRouter
import com.github.terrakok.cicerone.Router

class ListOfMusicRouterImpl(private val router:Router): ListOfMusicRouter {

	override fun navigateToTrekScreen() {

	}

	override fun navigateBack() {
		router.exit()
	}
}