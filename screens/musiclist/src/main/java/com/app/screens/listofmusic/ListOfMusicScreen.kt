package com.app.screens.listofmusic

import com.app.screens.listofmusic.ui.ListOfMusicFragment
import com.github.terrakok.cicerone.androidx.FragmentScreen

fun getMusicListScreen() = FragmentScreen { ListOfMusicFragment.newInstance() }