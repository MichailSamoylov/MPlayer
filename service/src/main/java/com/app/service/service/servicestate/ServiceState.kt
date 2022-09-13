package com.app.service.service.servicestate

import com.app.service.shared.musicfinder.domain.MusicEntity

sealed class ServiceState {

	data class Open(
		val listOfMusicEntity: List<MusicEntity>,
		val lastPlayingItemEntity: MusicEntity,
		val playerState: ServicePlayerState,
		val currentPosition: Long,
		val receiveChangeFromSeekBar: Boolean,
	) : ServiceState()

	object Close : ServiceState()
}

sealed class ServicePlayerState {
	object Play : ServicePlayerState()

	object Pause : ServicePlayerState()

	object Stop : ServicePlayerState()

	object Reset : ServicePlayerState()
}
