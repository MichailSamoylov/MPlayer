package com.app.screens.listofmusic.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.myplayer.shared.mp3scannersystem.domain.MusicEntity
import com.app.screens.listofmusic.databinding.ItemOfMusicListBinding

import android.view.View
import androidx.appcompat.graphics.drawable.AnimatedStateListDrawableCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.app.myplayer.additioanl.timeconverter.convertTimeToString
import com.app.screens.listofmusic.R
import java.text.SimpleDateFormat
import java.util.*

class ListOfMusicViewHolder(
	private val binding: ItemOfMusicListBinding
) : RecyclerView.ViewHolder(binding.root) {

	companion object {

		fun from(parent: ViewGroup): ListOfMusicViewHolder {
			val inflater = LayoutInflater.from(parent.context)
			val binding = ItemOfMusicListBinding.inflate(inflater, parent, false)
			return ListOfMusicViewHolder(binding)
		}
	}

	fun bing(
		entity: MusicEntity,
		doOnClickByTrek: (MusicEntity) -> Unit,
		songIsPlaying: Boolean = false
	) {
		with(binding) {
			nameOfTrack.text = entity.title
			duration.text = convertTimeToString(entity.duration)
			val myAnimVectorIcon = AnimatedVectorDrawableCompat.create(
				binding.root.context,
				R.drawable.anim_icon
			)
			songsPlayingIcon.setImageDrawable(myAnimVectorIcon)
			myAnimVectorIcon?.start()

			if (songIsPlaying) {
				songsPlayingIcon.visibility = View.VISIBLE
			} else {
				songsPlayingIcon.visibility = View.GONE
			}

			itemOfMusicList.setOnClickListener {

				if (songsPlayingIcon.visibility == View.INVISIBLE || songsPlayingIcon.visibility == View.GONE) {
					songsPlayingIcon.visibility = View.VISIBLE
				} else {
					songsPlayingIcon.visibility = View.GONE
				}
				doOnClickByTrek(entity)
			}
		}
	}
}