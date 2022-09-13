package com.app.screens.listofmusic.presentation

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.myplayer.shared.mp3scannersystem.domain.GetMP3FileListUseCase
import com.app.myplayer.shared.mp3scannersystem.domain.MusicEntity
import kotlinx.coroutines.launch

class ListOfMusicViewModel(
	private val router: ListOfMusicRouter,
	private val getMP3FileListUseCase: GetMP3FileListUseCase
) : ViewModel() {

	private val _state = MutableLiveData<UIState>(UIState.Initial)
	val state: LiveData<UIState> = _state

	init {
		_state.value = UIState.Content(
			getMP3FileListUseCase(),
			MusicEntity(Uri.EMPTY, "", 0),
			null
		)
	}

	fun setStateByDownToolBarClick(){
		val tempState = _state.value as? UIState.Content ?: return
		when(tempState.stateOfPlayer){
			PlayerState.Start,
			PlayerState.Continue -> _state.value = tempState.copy(stateOfPlayer = PlayerState.Pause)
			PlayerState.Pause    -> _state.value = tempState.copy(stateOfPlayer = PlayerState.Continue)
			PlayerState.Stop,
			null                 -> Unit
		}
	}

	fun setStateByItemClick(entity: MusicEntity) {
		val tempState = _state.value as? UIState.Content ?: return
		if (tempState.lastPlayingItemEntity.title == entity.title) {
			setPlayingState(tempState)
		} else {
			_state.value = tempState.copy(lastPlayingItemEntity = entity, stateOfPlayer = PlayerState.Stop)
			_state.value = tempState.copy(lastPlayingItemEntity = entity, stateOfPlayer = PlayerState.Start)
		}
	}

	private fun setPlayingState(state: UIState.Content) {
		if (state.stateOfPlayer == PlayerState.Pause) {
			_state.value = state.copy(stateOfPlayer = PlayerState.Continue)
		} else {
			_state.value = state.copy(stateOfPlayer = PlayerState.Pause)
		}
	}

	fun navigateToSearchScreen() {
		router.navigateToSearchScreen()
	}

	fun navigateBack() {
		router.navigateBack()
	}
}