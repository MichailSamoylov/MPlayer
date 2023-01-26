package com.app.service.service

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.service.additioanl.extension.doThrowableException
import com.app.service.additioanl.gsomparser.reestablishObjectFromGson
import com.app.service.notification.*
import com.app.service.service.mediasession.MediaSessionCallBack
import com.app.service.service.mediasession.resetCurrentPosition
import com.app.service.service.mediasession.resetSession
import com.app.service.service.servicestate.ServicePlayerState
import com.app.service.service.servicestate.ServiceState
import com.app.service.service.servicestate.StateObserver
import com.app.service.shared.musicfinder.domain.MusicEntity
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MediaService :
	Service(), MediaPlayer.OnCompletionListener,
	MediaPlayer.OnPreparedListener {

	companion object {

		private const val TIME_OF_UPDATE_SEEK_BAR = 1000L
		private const val MEDIA_SESSION_TAG = "MEDIA_SESSION_TAG"
		private const val NOTIFICATION_ID = 101
		const val LIST_OF_ENTITY_KEY = "LIST_OF_ENTITY_KEY"
		const val MUSIC_ENTITY_KEY = "MUSIC_ENTITY_KEY"
		const val EXCEPTION_TAG = "EXCEPTION_TAG"
	}

	private val notificationManager: NotificationManager by inject()
	private var mediaPlayer: MediaPlayer = MediaPlayer()
	private var mediaSession: MediaSessionCompat? = null
	private val mMediaSessionCallBack = MediaSessionCallBack(::setSeekBarOnTime)
	private val iBinder: IBinder = LocalBinder()

	inner class LocalBinder : Binder() {

		val service: MediaService
			get() = this@MediaService
	}

	private val _state = MutableLiveData<ServiceState>(null)
	val state: LiveData<ServiceState> = _state

	fun resetTrek(newPlayedTrek: MusicEntity) {
		state.value?.onOpen {
			if (lastPlayingItemEntity != newPlayedTrek) {
				_state.value = copy(playerState = ServicePlayerState.Reset, lastPlayingItemEntity = newPlayedTrek, receiveChangeFromSeekBar = false)
				setMediaPlayerSource(newPlayedTrek)
			}
		}
	}

	fun requestResetServiceSeekBar(progress: Int) {
		setSeekBarOnTime(progress.toLong())
	}

	private fun setSeekBarOnTime(newPosition: Long) {
		state.value?.onOpen {
			mediaPlayer.seekTo(newPosition.toInt())
			_state.value = copy(currentPosition = mediaPlayer.currentPosition.toLong())
		}
	}

	fun requestPauseMedia() {
		pauseMedia()
	}

	fun requestPlayMedia() {
		resumeMedia()
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		when (intent?.action) {
			ACTION_CANCEL -> closeSelf()

			PREV_TREK     -> startPrevTrek()

			PAUSE_OR_PLAY -> turnOverPlayerState()

			NEXT_TREK     -> startNextTrek()
		}
		return START_NOT_STICKY
	}

	private fun closeSelf() {
		stopMedia()
		_state.value = ServiceState.Close
		notificationManager.cancelAll()
		stopForeground(STOP_FOREGROUND_REMOVE)
		stopForeground(true)
		stopSelf()
	}

	private fun startPrevTrek() {
		_state.value?.onOpen {
			stopMedia()
			val indexTrek = listOfMusicEntity.indexOf(lastPlayingItemEntity)
			val indexNextTrek = if (indexTrek == 0) {
				listOfMusicEntity.size - 1
			} else {
				indexTrek - 1
			}
			val nextTrek = listOfMusicEntity[indexNextTrek]
			resetTrek(nextTrek)
		}
	}

	private fun startNextTrek() {
		_state.value?.onOpen {
			stopMedia()
			val indexTrek = listOfMusicEntity.indexOf(lastPlayingItemEntity)
			val indexPrevTrek = if (indexTrek >= listOfMusicEntity.size - 1) {
				0
			} else {
				indexTrek + 1
			}
			val nextTrek = listOfMusicEntity[indexPrevTrek]
			resetTrek(nextTrek)
		}
	}

	private fun turnOverPlayerState() {
		_state.value?.onOpen {
			if (playerState is ServicePlayerState.Play) {
				pauseMedia()
			} else {
				resumeMedia()
			}
		}
	}

	override fun onBind(intent: Intent?): IBinder {
		mediaSession = MediaSessionCompat(this, MEDIA_SESSION_TAG)
		createNotificationChannel(this, notificationManager)
		StateObserver.observe(::handleState)
		_state.observeForever(StateObserver)
		initMediaPlayer()
		intent?.let { initSource(it) }
		return iBinder
	}

	private fun initMediaPlayer() {
		mediaPlayer.setOnCompletionListener(this)
		mediaPlayer.setOnPreparedListener(this)
	}

	private fun initSource(intent: Intent) {
		val gson = Gson()
		val lastTrekEntity = reestablishObjectFromGson<MusicEntity>(gson, intent.getStringExtra(MUSIC_ENTITY_KEY) ?: "")
		val listOfMusicEntity = reestablishObjectFromGson<List<MusicEntity>>(gson, intent.getStringExtra(LIST_OF_ENTITY_KEY) ?: "")

		if (lastTrekEntity != null && !listOfMusicEntity.isNullOrEmpty()) {
			_state.value = ServiceState.Open(
				listOfMusicEntity = listOfMusicEntity,
				lastPlayingItemEntity = lastTrekEntity,
				playerState = ServicePlayerState.Reset,
				currentPosition = 0,
				receiveChangeFromSeekBar = false
			)
			setMediaPlayerSource(lastTrekEntity)
		}
	}

	private fun setMediaPlayerSource(trek: MusicEntity) {
		stopMedia()
		mediaPlayer.reset()
		doThrowableException(
			logTag = EXCEPTION_TAG,
			throwableAction = {
				mediaPlayer.setDataSource(this, Uri.parse(trek.uri))
				mediaPlayer.prepareAsync()
			},
			catchAction = {
				closeSelf()
			}
		)
	}

	private fun handleState(state: ServiceState) {
		when (state) {
			ServiceState.Close -> Unit

			is ServiceState.Open -> {
				if (!state.receiveChangeFromSeekBar) {
					updateNotification(state)
				}
				mediaSession?.resetCurrentPosition(mediaPlayer)
			}
		}
	}

	private fun updateNotification(state: ServiceState.Open) {
		mediaSession?.resetSession(mediaPlayer, mMediaSessionCallBack)
		when (state.playerState) {
			ServicePlayerState.Pause -> {
				val notificationData = NotificationData(state.lastPlayingItemEntity.title, R.drawable.ic_play)
				startForeground(NOTIFICATION_ID, createNotification(notificationData, mediaSession ?: return))
			}

			ServicePlayerState.Play  -> {
				val notificationData = NotificationData(state.lastPlayingItemEntity.title, R.drawable.ic_pause)
				startForeground(NOTIFICATION_ID, createNotification(notificationData, mediaSession ?: return))
			}

			else                     -> Unit
		}
	}

	private fun playMedia() {
		_state.value?.onOpen {
			if (!mediaPlayer.isPlaying) {
				mediaPlayer.start()
				serviceCoroutineContext.launch {
					startSendingCurrentPosition()
				}
				_state.value = copy(playerState = ServicePlayerState.Play, receiveChangeFromSeekBar = false)
			}
		}
	}

	private fun stopMedia() {
		_state.value?.onOpen {
			if (mediaPlayer.isPlaying) {
				mediaPlayer.stop()
				stopSendingCurrentPosition()
				_state.value = copy(playerState = ServicePlayerState.Stop, receiveChangeFromSeekBar = false)
			}
		}
	}

	private fun pauseMedia() {
		_state.value?.onOpen {
			if (mediaPlayer.isPlaying) {
				mediaPlayer.pause()
				stopSendingCurrentPosition()
				_state.value = copy(playerState = ServicePlayerState.Pause, receiveChangeFromSeekBar = false)
			}
		}
	}

	private fun resumeMedia() {
		_state.value?.onOpen {
			if (!mediaPlayer.isPlaying) {
				mediaPlayer.start()
				serviceCoroutineContext.launch { startSendingCurrentPosition() }
				_state.value = copy(playerState = ServicePlayerState.Play, receiveChangeFromSeekBar = false)
			}
		}
	}

	private suspend fun startSendingCurrentPosition() {
		delay(TIME_OF_UPDATE_SEEK_BAR)
		_state.value?.onOpen {
			_state.postValue(copy(currentPosition = mediaPlayer.currentPosition.toLong(), receiveChangeFromSeekBar = true))
		}
		startSendingCurrentPosition()
	}

	private fun stopSendingCurrentPosition() {
		serviceJob.canselAllChildren()
	}

	override fun onCompletion(mp: MediaPlayer?) {
		stopSendingCurrentPosition()
		_state.value?.onOpen {
			if (playerState != ServicePlayerState.Reset) {
				startNextTrek()
			}
		}
	}

	override fun onPrepared(mp: MediaPlayer?) {
		playMedia()
	}

	override fun onDestroy() {
		super.onDestroy()
		_state.removeObserver(StateObserver)
	}
}