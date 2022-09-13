package com.app.screens.listofmusic.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.app.myplayer.shared.mp3scannersystem.domain.MusicEntity

class ListOfMusicListAdapter(
	private val sendDataInViewModelByClick: (MusicEntity) -> Unit
) : ListAdapter<MusicEntity, ListOfMusicViewHolder>(DiffCallBack) {

	private var uriOfPlayingSong: Uri? = null

	@SuppressLint("NotifyDataSetChanged")
	fun resetDataOfPlayingSong(uri: Uri?) {
		uriOfPlayingSong = uri
		this.notifyDataSetChanged()
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListOfMusicViewHolder =
		ListOfMusicViewHolder.from(parent)

	override fun onBindViewHolder(holder: ListOfMusicViewHolder, position: Int) {
		if (uriOfPlayingSong != null && this.getItem(position).uri == uriOfPlayingSong) {
			holder.bing(this.getItem(position), sendDataInViewModelByClick, true)
		} else {
			holder.bing(this.getItem(position), sendDataInViewModelByClick, false)
		}
	}
}

object DiffCallBack : DiffUtil.ItemCallback<MusicEntity>() {

	override fun areItemsTheSame(oldItem: MusicEntity, newItem: MusicEntity): Boolean =
		oldItem == newItem

	override fun areContentsTheSame(oldItem: MusicEntity, newItem: MusicEntity): Boolean =
		oldItem.uri == newItem.uri && oldItem.title == newItem.title && oldItem.duration == newItem.duration
}