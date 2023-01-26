package com.app.service.shared.musicfinder.domain

import android.content.Context
import com.app.service.shared.musicfinder.data.scanSystemOnMusicFile

class GetMusicFileListUseCase(private val context: Context) {

	operator fun invoke(): List<MusicEntity> =
		scanSystemOnMusicFile(context)
}