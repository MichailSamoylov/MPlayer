package com.app.location_app.di

import com.app.location_app.navigation.ListOfMusicRouterImpl
import com.app.location_app.navigation.StartRouterImpl
import com.app.service.screens.start.presentation.StartRouter
import com.app.screens.listofmusic.presentation.ListOfMusicRouter
import org.koin.dsl.module

val navigationModule = module {
	factory<ListOfMusicRouter> { ListOfMusicRouterImpl(get()) }
	factory<StartRouter> { StartRouterImpl(get()) }
}