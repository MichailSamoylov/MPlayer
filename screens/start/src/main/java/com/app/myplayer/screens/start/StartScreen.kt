package com.app.myplayer.screens.start

import com.app.myplayer.screens.start.ui.StartFragment
import com.github.terrakok.cicerone.androidx.FragmentScreen

fun getStartScreen() = FragmentScreen { StartFragment.newInstance() }