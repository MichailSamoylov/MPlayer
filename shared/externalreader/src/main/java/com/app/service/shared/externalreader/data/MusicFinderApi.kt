package com.app.service.shared.musicfinder.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.app.service.shared.musicfinder.domain.MusicEntity

private val projection = arrayOf(
	MediaStore.Audio.Media._ID,
	MediaStore.Audio.Media.TITLE,
	MediaStore.Audio.Media.DURATION
)
private const val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

private val selectionArgs: Array<String>? = null

private val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

internal fun scanSystemOnMusicFile(context: Context): List<MusicEntity> {
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
			songs.add(MusicEntity(contentUri.toString(), title, duration))
		}
		cursor.close()
	}
	return discardFakeMusicItem(songs)
}

private fun discardFakeMusicItem(listOfMusic:List<MusicEntity>): List<MusicEntity> {
	val groupsSameItems = listOfMusic.groupBy { it.title } as MutableMap<String, List<MusicEntity>>
	val correctListOfMusic = mutableListOf<MusicEntity>()
	groupsSameItems.forEach {
		correctListOfMusic.add(it.value.sortedBy { entity -> entity.uri }.last())
	}
	return correctListOfMusic
}