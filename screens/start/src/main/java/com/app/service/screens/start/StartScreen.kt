package com.app.service.screens.start

import com.app.service.screens.start.ui.StartFragment
import com.github.terrakok.cicerone.androidx.FragmentScreen

fun getStartScreen() = FragmentScreen { StartFragment.newInstance() }