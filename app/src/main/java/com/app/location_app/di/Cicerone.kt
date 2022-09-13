package com.app.location_app.di

import com.app.location_app.navigation.buildCicerone
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.Router
import org.koin.dsl.module

val ciceroneModule = module {
	single { buildCicerone() }
	single { get<Cicerone<Router>>().router }
	single { get<Cicerone<Router>>().getNavigatorHolder() }
}