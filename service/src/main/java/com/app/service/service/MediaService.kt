package com.app.service.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.service.additioanl.gsomparser.reestablishObjectFromGson
import com.app.service.notification.ACTION_CANCEL
import com.app.service.notification.NEXT_TREK
import com.app.service.notification.NotificationData
import com.app.service.notification.PAUSE_OR_PLAY
import com.app.service.notification.PREV_TREK
import com.app.service.notification.createNotification
import com.app.service.service.mediasession.MediaSessionCallBack
import com.app.service.service.mediasession.resetCurrentPosition
import com.app.service.service.mediasession.resetSession
import com.app.service.service.servicestate.ServicePlayerState
import com.app.service.service.servicestate.ServiceState
import com.app.service.service.servicestate.StateObserver
import com.app.service.shared.musicfinder.domain.MusicEntity
import com.google.gson.Gson
import java.io.IOException
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MediaService :
	Service(), MediaPlayer.OnCompletionListener,
	MediaPlayer.OnPreparedListener {

	companion object {

		private const val TIME_OF_UPDATE_SEEK_BAR = 1000L
		private const val MEDIA_SESSION_TAG = "MEDIA_SESSION_TAG"
		private const val NOTIFICATION_ID = 1
		const val LIST_OF_ENTITY_KEY = "LIST_OF_ENTITY_KEY"
		const val MUSIC_ENTITY_KEY = "MUSIC_ENTITY_KEY"
		const val EXCEPTION_TAG = "EXCEPTION_TAG"
	}

	private var mediaPlayer: MediaPlayer = MediaPlayer()
	private var mediaSession: MediaSessionCompat? = null
	private val mMediaSessionCallBack = MediaSessionCallBack(::setSeekBarOnTime)
	private val iBinder: IBinder = LocalBinder()
	inner class LocalBinder : Binder() {

		val service: MediaService
			get() = this@MediaService
	}

	private var listOfMusicEntity: List<MusicEntity>? = null
	private val stateObserver = StateObserver(::handleState)
	private val _state = MutableLiveData<ServiceState>(null)
	val state: LiveData<ServiceState> = _state

	private object ServiceCoroutineContext : CoroutineScope {

		val serviceJob: Job = Job()
		override val coroutineContext: CoroutineContext
			get() = (Dispatchers.Default + serviceJob)
	}

	fun resetTrek(newPlayedTrek: MusicEntity) {
		val tempState = state.value as? ServiceState.Open ?: return
		if (tempState.lastPlayingItemEntity != newPlayedTrek) {
			_state.value = tempState.copy(playerState = ServicePlayerState.Reset, lastPlayingItemEntity = newPlayedTrek, receiveChangeFromSeekBar = false)
			setMediaPlayerSource(newPlayedTrek)
		}
	}

	fun requestResetServiceSeekBar(progress: Int) {
		setSeekBarOnTime(progress.toLong())
	}

	private fun setSeekBarOnTime(newPosition: Long) {
		val tempState = _state.value as? ServiceState.Open ?: return
		mediaPlayer.seekTo(newPosition.toInt())
		_state.value = tempState.copy(currentPosition = mediaPlayer.currentPosition.toLong())
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
		stopForeground(STOP_FOREGROUND_REMOVE)
		stopForeground(true)
		stopSelf()
	}

	private fun startPrevTrek() {
		val tempState = _state.value as? ServiceState.Open ?: return
		stopMedia()
		listOfMusicEntity?.let { list ->
			val indexTrek = list.indexOf(tempState.lastPlayingItemEntity)
			if (indexTrek == 0) {
				val prevIndexTrek = list.size - 1
				resetTrek(list[prevIndexTrek])
			} else {
				val prevIndexTrek = indexTrek - 1
				resetTrek(list[prevIndexTrek])
			}
		}
	}

	private fun startNextTrek() {
		val tempState = _state.value as? ServiceState.Open ?: return
		stopMedia()
		listOfMusicEntity?.let { list ->
			val indexTrek = list.indexOf(tempState.lastPlayingItemEntity)
			if (indexTrek >= list.size - 1) {
				val nextIndexTrek = 0
				resetTrek(list[nextIndexTrek])
			} else {
				val nextIndexTrek = indexTrek + 1
				resetTrek(list[nextIndexTrek])
			}
		}
	}

	private fun turnOverPlayerState() {
		val tempState = _state.value as? ServiceState.Open ?: return
		if (tempState.playerState is ServicePlayerState.Play) {
			pauseMedia()
		} else {
			resumeMedia()
		}
	}

	override fun onBind(intent: Intent?): IBinder {
		_state.observeForever(stateObserver)
		initMediaPlayer()
		mediaSession = MediaSessionCompat(this, MEDIA_SESSION_TAG)
		if (intent != null) {
			initSource(intent)
		}
		return iBinder
	}

	private fun initMediaPlayer() {
		mediaPlayer.setOnCompletionListener(this)
		mediaPlayer.setOnPreparedListener(this)
	}

	private fun initSource(intent: Intent) {
		val gson = Gson()
		listOfMusicEntity = intent.getStringExtra(LIST_OF_ENTITY_KEY)?.let { stringList ->
			reestablishObjectFromGson<List<MusicEntity>>(gson, stringList)
		}
		val lastTrekEntity = intent.getStringExtra(MUSIC_ENTITY_KEY)?.let { stringEntity ->
			reestablishObjectFromGson<MusicEntity>(gson, stringEntity)
		}

		if (lastTrekEntity != null && listOfMusicEntity != null) {
			_state.value = ServiceState.Open(lastTrekEntity, ServicePlayerState.Reset, 0, false)
			setMediaPlayerSource(lastTrekEntity)
		}
	}

	private fun setMediaPlayerSource(trek: MusicEntity) {
		stopMedia()
		mediaPlayer.reset()
		try {
			mediaPlayer.setDataSource(this, Uri.parse(trek.uri))
			mediaPlayer.prepareAsync()
		} catch (e: IOException) {
			Log.e(EXCEPTION_TAG, e.toString())
			closeSelf()
		}
	}

	private fun handleState(state: ServiceState) {
		when (state) {
			ServiceState.Close   -> Unit

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
		when(state.playerState){
			ServicePlayerState.Pause -> {
				val notificationData = NotificationData(state.lastPlayingItemEntity.title, R.drawable.ic_play)
				startForeground(NOTIFICATION_ID, createNotification(notificationData, mediaSession ?: return))
			}
			ServicePlayerState.Play  -> {
				val notificationData = NotificationData(state.lastPlayingItemEntity.title, R.drawable.ic_pause)
				startForeground(NOTIFICATION_ID, createNotification(notificationData, mediaSession ?: return))
			}
			ServicePlayerState.Reset,
			ServicePlayerState.Stop  -> Unit
		}
	}

	private fun playMedia() {
		if (!mediaPlayer.isPlaying) {
			mediaPlayer.start()
			ServiceCoroutineContext.launch {
				startSendingCurrentPosition()
			}
			val tempState = state.value as? ServiceState.Open ?: return
			_state.value = tempState.copy(playerState = ServicePlayerState.Play, receiveChangeFromSeekBar = false)
		}
	}

	private fun stopMedia() {
		if (mediaPlayer.isPlaying) {
			mediaPlayer.stop()
			stopSendingCurrentPosition()
			val tempState = state.value as? ServiceState.Open ?: return
			_state.value = tempState.copy(playerState = ServicePlayerState.Stop, receiveChangeFromSeekBar = false)
		}
	}

	private fun pauseMedia() {
		if (mediaPlayer.isPlaying) {
			mediaPlayer.pause()
			stopSendingCurrentPosition()
			val tempState = _state.value as? ServiceState.Open ?: return
			_state.value = tempState.copy(playerState = ServicePlayerState.Pause, receiveChangeFromSeekBar = false)
		}
	}

	private fun resumeMedia() {
		if (!mediaPlayer.isPlaying) {
			mediaPlayer.start()
			ServiceCoroutineContext.launch {
				startSendingCurrentPosition()
			}
			val tempState = _state.value as? ServiceState.Open ?: return
			_state.value = tempState.copy(playerState = ServicePlayerState.Play, receiveChangeFromSeekBar = false)
		}
	}

	private suspend fun startSendingCurrentPosition() {
		delay(TIME_OF_UPDATE_SEEK_BAR)
		val lastState = _state.value as? ServiceState.Open
		lastState?.let { tempState ->
			_state.postValue(tempState.copy(currentPosition = mediaPlayer.currentPosition.toLong(), receiveChangeFromSeekBar = true))
			startSendingCurrentPosition()
		}
	}

	private fun stopSendingCurrentPosition() {
		ServiceCoroutineContext.serviceJob.cancelChildren()
	}

	override fun onCompletion(mp: MediaPlayer?) {
		stopSendingCurrentPosition()
		val tempState = _state.value as? ServiceState.Open ?: return
		if (tempState.playerState != ServicePlayerState.Reset) {
			startNextTrek()
		}
	}

	override fun onPrepared(mp: MediaPlayer?) {
		playMedia()
	}

	override fun onDestroy() {
		super.onDestroy()
		_state.removeObserver(stateObserver)
	}
}