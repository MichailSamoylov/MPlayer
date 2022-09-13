package com.app.screens.listofmusic.presentation

import com.app.myplayer.shared.mp3scannersystem.domain.MusicEntity

sealed class UIState {

	object Initial : UIState()

	data class Content(
		val listOfMP3File: List<MusicEntity>,
		val lastPlayingItemEntity: MusicEntity,
		val stateOfPlayer: PlayerState?
	) : UIState()
}

sealed class PlayerState {
	object Start : PlayerState()

	object Pause : PlayerState()

	object Continue : PlayerState()

	object Stop : PlayerState()
}
