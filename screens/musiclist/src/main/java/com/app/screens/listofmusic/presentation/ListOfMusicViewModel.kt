package com.app.screens.listofmusic.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.service.service.servicestate.ServicePlayerState
import com.app.service.service.servicestate.ServiceState
import com.app.service.shared.musicfinder.domain.GetMusicFileListUseCase
import com.app.service.shared.musicfinder.domain.MusicEntity

class ListOfMusicViewModel(
	private val router: ListOfMusicRouter,
	getMP3FileListUseCase: GetMusicFileListUseCase
) : ViewModel() {

	private val _state = MutableLiveData<UIState>(UIState.Initial)
	val state: LiveData<UIState> = _state

	private companion object {

		const val START_CURRENT_POSITION = 0L
		val emptyMusicEntity = MusicEntity("", "", 0)
	}

	init {
		_state.value = UIState.Content(
			getMP3FileListUseCase(),
			emptyMusicEntity,
			ServiceConnectionState.Disconnected
		)
	}

	fun serviceIsStarting() {
		val tempState = _state.value as? UIState.Content ?: return
		_state.value = tempState.copy(serviceState = ServiceConnectionState.Connected(false, null, START_CURRENT_POSITION))
	}

	fun serviceIsEnding() {
		val tempState = _state.value as? UIState.Content ?: return
		_state.value = tempState.copy(serviceState = ServiceConnectionState.Disconnected)
	}

	fun handelServiceState(serviceState: ServiceState) {
		val lastState = _state.value as? UIState.Content ?: return
		lastState.onConnected { connectionState ->

			when (serviceState) {
				is ServiceState.Close -> _state.postValue(
					lastState.copy(
						lastTrekEntity = emptyMusicEntity,
						serviceState = connectionState.copy(
							sendChangesFromService = true,
							stateOfPlayer = PlayerState.Close,
							currentPosition = START_CURRENT_POSITION
						)
					)
				)

				is ServiceState.Open  -> {

					val openedConnection = connectionState.copy(
						sendChangesFromService = true,
						currentPosition = serviceState.currentPosition
					)

					when (serviceState.playerState) {
						ServicePlayerState.Play  -> doOnServiceStatePlay(
							uiState = lastState,
							serviceConnection = openedConnection,
							serviceState = serviceState
						)

						ServicePlayerState.Pause -> _state.postValue(
							lastState.copy(
								serviceState = openedConnection.copy(
									stateOfPlayer = PlayerState.Pause,
								)
							)
						)

						ServicePlayerState.Stop  -> _state.postValue(
							lastState.copy(
								serviceState = openedConnection.copy(
									stateOfPlayer = PlayerState.Stop
								)
							)
						)

						else                     -> Unit
					}
				}
			}
		}
	}

	private fun doOnServiceStatePlay(uiState: UIState.Content, serviceConnection: ServiceConnectionState.Connected, serviceState: ServiceState.Open) {
		if (serviceConnection.stateOfPlayer == null || uiState.lastTrekEntity != serviceState.lastPlayingItemEntity) {
			_state.postValue(
				uiState.copy(
					lastTrekEntity = serviceState.lastPlayingItemEntity,
					serviceState = serviceConnection.copy(
						stateOfPlayer = PlayerState.Start
					)
				)
			)
		} else {
			_state.postValue(
				uiState.copy(
					lastTrekEntity = serviceState.lastPlayingItemEntity,
					serviceState = serviceConnection.copy(
						stateOfPlayer = PlayerState.Continue
					)
				)
			)
		}
	}

	fun setStateByDownToolBarClick() {
		val tempState = _state.value as? UIState.Content ?: return
		tempState.onConnected { connectionState ->
			val serviceConnection = connectionState.copy(sendChangesFromService = false)
			when (connectionState.stateOfPlayer) {
				PlayerState.Start,

				PlayerState.Continue -> _state.value =
					tempState.copy(
						serviceState = serviceConnection.copy(
							stateOfPlayer = PlayerState.Pause
						)
					)

				PlayerState.Pause    -> _state.value =
					tempState.copy(
						serviceState = serviceConnection.copy(
							stateOfPlayer = PlayerState.Continue
						)
					)

				PlayerState.Close,
				PlayerState.Stop     -> Unit

				null                 -> _state.value =
					tempState.copy(
						serviceState = serviceConnection.copy(
							stateOfPlayer = PlayerState.Start
						)
					)
			}
		}
	}

	fun setStateByItemClick(entity: MusicEntity) {
		val tempState = _state.value as? UIState.Content ?: return
		if (tempState.lastTrekEntity.title == entity.title) {
			changeStateForPlayingTrek(tempState)
		} else {
			changeStateOnOtherTrek(tempState, entity)
		}
	}

	private fun changeStateForPlayingTrek(state: UIState.Content) {
		state.onConnected { connectionState ->
			val serviceConnection = connectionState.copy(sendChangesFromService = false)
			if (connectionState.stateOfPlayer == PlayerState.Pause) {
				_state.value = state.copy(
					serviceState = serviceConnection.copy(
						stateOfPlayer = PlayerState.Continue
					)
				)
			} else {
				_state.value = state.copy(
					serviceState = serviceConnection.copy(
						stateOfPlayer = PlayerState.Pause
					)
				)
			}
		}
	}

	private fun changeStateOnOtherTrek(state: UIState.Content, entity: MusicEntity) {
		state.onConnected { connectionState ->
			val serviceConnection = connectionState.copy(sendChangesFromService = false)
			if (connectionState.stateOfPlayer == null) {
				_state.value = state.copy(lastTrekEntity = entity)
			} else {
				state.setRestartTrek(serviceConnection, entity)
			}
		}
		state.onDisconnected {
			_state.value = state.copy(lastTrekEntity = entity)
		}
	}

	private fun UIState.Content.setRestartTrek(serviceConnection: ServiceConnectionState.Connected, newTrek: MusicEntity) {
		_state.value = copy(
			lastTrekEntity = newTrek,
			serviceState = serviceConnection.copy(stateOfPlayer = PlayerState.Stop)
		)
		_state.value = copy(
			lastTrekEntity = newTrek,
			serviceState = serviceConnection.copy(stateOfPlayer = PlayerState.Start)
		)
	}

	private inline fun UIState.onConnected(action: (ServiceConnectionState.Connected) -> Unit) {
		((this as? UIState.Content)?.serviceState as? ServiceConnectionState.Connected)?.let {
			action(it)
		}
	}

	private inline fun UIState.onDisconnected(action: (ServiceConnectionState.Disconnected) -> Unit) {
		((this as? UIState.Content)?.serviceState as? ServiceConnectionState.Disconnected)?.let {
			action(it)
		}
	}
}