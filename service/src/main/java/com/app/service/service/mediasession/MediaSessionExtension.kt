package com.app.service.service.mediasession

import android.media.MediaPlayer
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log

private const val SEEK_BAR_UPDATE_SPEED = 1F

internal fun MediaSessionCompat.resetCurrentPosition(mediaPlayer: MediaPlayer) {
	var mediaSessionPlayBackState = PlaybackStateCompat.STATE_PLAYING
	if (!mediaPlayer.isPlaying) {
		mediaSessionPlayBackState = PlaybackStateCompat.STATE_PAUSED
	}
	this.setPlaybackState(
		PlaybackStateCompat.Builder()
			.setState(mediaSessionPlayBackState, mediaPlayer.currentPosition.toLong(), SEEK_BAR_UPDATE_SPEED)
			.setActions(PlaybackStateCompat.ACTION_SEEK_TO)
			.build()
	)
}

internal fun MediaSessionCompat.resetSession(mediaPlayer: MediaPlayer, medialSessionCallBack: MediaSessionCallBack) {
	this.setMetadata(
		MediaMetadataCompat.Builder()
			.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer.duration.toLong())
			.build()
	)
	this.setCallback(medialSessionCallBack)
}