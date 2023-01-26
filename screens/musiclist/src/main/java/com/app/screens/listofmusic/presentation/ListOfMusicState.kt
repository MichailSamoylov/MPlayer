package com.app.screens.listofmusic.presentation

import com.app.service.shared.musicfinder.domain.MusicEntity

sealed class UIState {

	object Initial : UIState()

	data class Content(
		val listOfTrekFile: List<MusicEntity>,
		val lastTrekEntity: MusicEntity,
		val serviceState: ServiceConnectionState,
	) : UIState()
}

sealed class ServiceConnectionState {
	object Disconnected : ServiceConnectionState()

	data class Connected(
		val sendChangesFromService: Boolean,
		val stateOfPlayer: PlayerState?,
		val currentPosition: Long,
	) : ServiceConnectionState()
}

sealed class PlayerState {
	object Start : PlayerState()

	object Pause : PlayerState()

	object Continue : PlayerState()

	object Stop : PlayerState()

	object Close : PlayerState()

}
