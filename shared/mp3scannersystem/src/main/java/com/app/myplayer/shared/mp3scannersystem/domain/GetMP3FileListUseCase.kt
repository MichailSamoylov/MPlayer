package com.app.myplayer.shared.mp3scannersystem.domain

import android.content.Context
import com.app.myplayer.shared.mp3scannersystem.data.getListOfCorrectMP3File

class GetMP3FileListUseCase(private val context: Context) {

	operator fun invoke(): List<MusicEntity> =
		getListOfCorrectMP3File(context)
}