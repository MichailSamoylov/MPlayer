package com.app.screens.listofmusic.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.app.service.additioanl.timeconverter.convertTimeToString
import com.app.service.shared.musicfinder.domain.MusicEntity
import com.app.screens.listofmusic.R
import com.app.screens.listofmusic.databinding.ItemOfMusicListBinding

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

	private val myAnimVectorIcon = AnimatedVectorDrawableCompat.create(
		binding.root.context,
		R.drawable.anim_icon
	)

	fun bing(
		entity: MusicEntity,
		doOnClickByTrek: (MusicEntity) -> Unit,
		songIsPlaying: Boolean = false
	) {
		with(binding) {
			nameOfTrack.text = entity.title
			duration.text = convertTimeToString(entity.duration)
			if (songsPlayingIcon.drawable != myAnimVectorIcon) {
				songsPlayingIcon.setImageDrawable(myAnimVectorIcon)
			}
			myAnimVectorIcon?.start()

			if (songIsPlaying) {
				songsPlayingIcon.visibility = View.VISIBLE
			} else {
				songsPlayingIcon.visibility = View.INVISIBLE
			}

			itemOfMusicList.setOnClickListener {

				if (songsPlayingIcon.visibility == View.INVISIBLE || songsPlayingIcon.visibility == View.GONE) {
					songsPlayingIcon.visibility = View.VISIBLE
				} else {
					songsPlayingIcon.visibility = View.INVISIBLE
				}
				doOnClickByTrek(entity)
			}
		}
	}
}