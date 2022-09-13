package com.app.myplayer.shared.mp3scannersystem.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.app.myplayer.shared.mp3scannersystem.domain.MusicEntity

private val projection = arrayOf(
	MediaStore.Audio.Media._ID,
	MediaStore.Audio.Media.TITLE,
	MediaStore.Audio.Media.DURATION
)
private const val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

private val selectionArgs: Array<String>? = null

private val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

private fun systemScannOnMP3File(context: Context): List<MusicEntity> {
	val songs = mutableListOf<MusicEntity>()

	context.contentResolver.query(
		MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
		projection,
		selection,
		selectionArgs,
		sortOrder,
	)?.use { cursor ->
		while (cursor.moveToNext()) {
			val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
			val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
			val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

			val id = cursor.getLong(idColumn)
			val title = cursor.getString(titleColumn)
			val duration = cursor.getLong(durationColumn)

			val contentUri: Uri = ContentUris.withAppendedId(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				id,
			)
			songs.add(MusicEntity(contentUri, title, duration))
		}
		cursor.close()
	}
	return songs
}


internal fun getListOfCorrectMP3File(context: Context): List<MusicEntity> {
	val groupsSameItems = systemScannOnMP3File(context).groupBy { it.title } as MutableMap<String, List<MusicEntity>>
	val listMP3 = mutableListOf<MusicEntity>()
	groupsSameItems.forEach {
		listMP3.add(it.value.sortedBy { entity -> entity.uri }.last())
	}
	return listMP3
}