package com.app.screens.listofmusic.ui

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.app.screens.listofmusic.R
import com.app.screens.listofmusic.databinding.FragmentListOfMusicBinding
import com.app.screens.listofmusic.presentation.ListOfMusicViewModel
import com.app.screens.listofmusic.presentation.PlayerState
import com.app.screens.listofmusic.presentation.UIState
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListOfMusicFragment : Fragment() {

	companion object {

		private const val LOG_ERROR_TAG = "LOG_ERROR_TAG"
		fun newInstance() = ListOfMusicFragment()
	}

	private val viewModel: ListOfMusicViewModel by viewModel()
	private var mediaPlayer: MediaPlayer = MediaPlayer()
	private lateinit var binding: FragmentListOfMusicBinding
	private lateinit var adapter: ListOfMusicListAdapter
	private val fragmentCoroutineScope = object : CoroutineScope {
		override val coroutineContext: CoroutineContext
			get() = Dispatchers.Main.immediate
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		super.onCreateView(inflater, container, savedInstanceState)
		binding = FragmentListOfMusicBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initAdapter()
		setObservers()
		setListeners()
	}

	private fun initAdapter() {
		adapter = ListOfMusicListAdapter(viewModel::setStateByItemClick)
		binding.list.adapter = adapter
	}

	private fun setObservers() {
		viewModel.state.observe(viewLifecycleOwner, ::handleState)
	}

	private fun setListeners() {
		mediaPlayer.setOnCompletionListener {
			it.stop()
			it.reset()
			it.release()
		}
		binding.buttonStopOrStart.setOnClickListener {
			viewModel.setStateByDownToolBarClick()
		}
		binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

			override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
				if (fromUser) mediaPlayer.seekTo(progress)
			}

			override fun onStartTrackingTouch(seekBar: SeekBar?) {}

			override fun onStopTrackingTouch(seekBar: SeekBar?) {}
		})
	}

	private fun handleState(state: UIState) {
		when (state) {
			is UIState.Initial -> Unit
			is UIState.Content -> renderContent(state)
		}
	}

	@SuppressLint("SetTextI18n")
	private fun renderContent(state: UIState.Content) {
		if (adapter.currentList != state.listOfMP3File) {
			adapter.submitList(state.listOfMP3File)
		}
		if (state.stateOfPlayer != null) {
			binding.lowerMenu.visibility = View.VISIBLE
		}
		binding.countOfTrek.text = "${resources.getString(R.string.title_count_of_trek_text)} ${state.listOfMP3File.size}"
		workWithMediaPlayer(state)
	}

	private fun workWithMediaPlayer(state: UIState.Content) {
		with(mediaPlayer) {
			when (state.stateOfPlayer) {
				PlayerState.Pause    -> {
					doSomethingInTryCatch("Pause") {
						pause()
						adapter.resetDataOfPlayingSong(null)
					}
				}

				PlayerState.Start    -> {
					doSomethingInTryCatch("Start") {
						adapter.resetDataOfPlayingSong(state.lastPlayingItemEntity.uri)
						setOnPreparedListener {
							it.start()
							setSeekBar()
							binding.titleOfPlayingSong.text = state.lastPlayingItemEntity.title
						}
						reset()
						setDataSource(requireContext(), state.lastPlayingItemEntity.uri)
						prepare()
					}
				}

				PlayerState.Continue -> {
					doSomethingInTryCatch("Continue") {
						adapter.resetDataOfPlayingSong(state.lastPlayingItemEntity.uri)
						start()
					}
				}

				PlayerState.Stop     -> {
					doSomethingInTryCatch("Stop") {
						stop()
						adapter.resetDataOfPlayingSong(null)
					}
				}

				null                 -> Unit

			}
		}
	}

	private fun setSeekBar() {
		binding.seekBar.max = mediaPlayer.duration
		/*val handler = Handler()
		handler.postDelayed(object : Runnable {
			override fun run() {
				try {
					binding.seekBar.progress = mediaPlayer.currentPosition
					handler.postDelayed(this, 1000)
				} catch (e: Exception) {
					binding.seekBar.progress = 0
				}
			}
		}, 0)*/
		redefinitionSeekBarProgress()
	}

	private fun redefinitionSeekBarProgress() {
		fragmentCoroutineScope.launch {
			delay(1000)
			try {
				binding.seekBar.progress = mediaPlayer.currentPosition
				redefinitionSeekBarProgress()

			} catch (e: Exception) {
				Log.e(LOG_ERROR_TAG, "$e SeekBar")
				binding.seekBar.progress = 0
			}
		}
	}

	private fun doSomethingInTryCatch(textForLog: String, runBlock: () -> Unit) {
		try {
			runBlock()
		} catch (e: Exception) {
			Log.e(LOG_ERROR_TAG, "$e $textForLog")
		}
	}
}