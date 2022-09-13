package com.app.myplayer.shared.mp3scannersystem.domain

import android.net.Uri

data class MusicEntity(
	val uri: Uri,
	val title: String,
	val duration: Long
)
