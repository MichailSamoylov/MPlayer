package com.app.service.service.mediasession

import android.support.v4.media.session.MediaSessionCompat

class MediaSessionCallBack(private val doOnSeekTo: (Long) -> Unit) : MediaSessionCompat.Callback() {

	override fun onSeekTo(pos: Long) {
		super.onSeekTo(pos)
		doOnSeekTo(pos)
	}
}