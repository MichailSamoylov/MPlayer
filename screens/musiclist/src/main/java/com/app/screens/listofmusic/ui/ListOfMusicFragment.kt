package com.app.screens.listofmusic.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.app.screens.listofmusic.R
import androidx.fragment.app.Fragment
import com.app.screens.listofmusic.databinding.FragmentListOfMusicBinding
import com.app.screens.listofmusic.presentation.ServiceConnectionState
import com.app.screens.listofmusic.presentation.ListOfMusicViewModel
import com.app.service.additioanl.timeconverter.convertTimeToString
import com.app.screens.listofmusic.ui.service.MyServiceConnection
import com.app.screens.listofmusic.ui.list.ListOfMusicListAdapter
import com.app.service.additioanl.extension.isServiceRunning
import com.app.screens.listofmusic.presentation.PlayerState
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.app.screens.listofmusic.presentation.UIState
import com.app.service.service.MediaService
import com.google.gson.Gson

class ListOfMusicFragment : Fragment() {

	companion object {

		fun newInstance() = ListOfMusicFragment()
	}

	private val viewModel: ListOfMusicViewModel by viewModel()
	private lateinit var binding: FragmentListOfMusicBinding
	private lateinit var adapter: ListOfMusicListAdapter

	private var mService: MediaService? = null
	private var mBound: Boolean = false
	private var connection: MyServiceConnection? = null

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		super.onCreateView(inflater, container, savedInstanceState)
		binding = FragmentListOfMusicBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initAdapter()
		setListeners()
		setObservers()
		initConnection()
		checkServiceAndTryConnect()
	}

	private fun initAdapter() {
		adapter = ListOfMusicListAdapter(viewModel::setStateByItemClick)
		binding.recyclerView.adapter = adapter
	}

	private fun setObservers() {
		viewModel.state.observe(viewLifecycleOwner, ::handleState)
	}

	private fun setListeners() {
		with(binding) {
			buttonStopOrStart.setOnClickListener {
				viewModel.setStateByDownToolBarClick()
			}

			seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

				override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
					if (fromUser) mService?.requestResetServiceSeekBar(progress)
				}

				override fun onStartTrackingTouch(seekBar: SeekBar?) {}

				override fun onStopTrackingTouch(seekBar: SeekBar?) {}
			})
		}
	}

	private fun checkServiceAndTryConnect() {
		activity?.applicationContext?.let { context ->
			if (context.isServiceRunning<MediaService>()) {
				val intent = Intent(context, MediaService::class.java)
				tryConnectToMediaService(context, intent)
			}
		}
	}

	private fun tryConnectToMediaService(context: Context, intent: Intent) {
		if (context.bindService(intent, connection ?: return, Context.BIND_AUTO_CREATE)) {
			viewModel.serviceIsStarting()
		}
	}

	private fun initConnection() {
		connection = MyServiceConnection(
			doOnConnected = { binder ->
				mService = binder.service
				mService!!.state.observe(viewLifecycleOwner, viewModel::handelServiceState)
				mBound = true
			},
			doOnDisconnected = {
				closeMediaService()
			})
	}

	private fun handleState(state: UIState) {
		when (state) {
			is UIState.Initial -> Unit
			is UIState.Content -> renderContent(state)
		}
	}

	private fun renderContent(state: UIState.Content) {
		with(binding) {
			countOfTrek.text = StringBuilder()
				.append(resources.getString(R.string.title_count_of_trek_text))
				.append(state.listOfTrekFile.size)
				.toString()
			if (adapter.currentList != state.listOfTrekFile) {
				adapter.submitList(state.listOfTrekFile)
			}
			if (state.serviceState == ServiceConnectionState.Disconnected) {
				lowerMenu.visibility = View.GONE
				adapter.resetDataOfPlayingSong(null)
				if (state.lastTrekEntity.uri.isNotEmpty()) {
					settingMediaService(state)
				}
			}
			if (state.serviceState is ServiceConnectionState.Connected) {
				renderConnectedChanges(state, state.serviceState)
				if (!state.serviceState.sendChangesFromService) {
					sendChangesInService(state, state.serviceState)
				}
			}
		}
	}

	@SuppressLint("UseCompatLoadingForDrawables")
	private fun renderConnectedChanges(state: UIState.Content, connected: ServiceConnectionState.Connected) {
		with(binding) {
			lowerMenu.visibility = View.VISIBLE
			seekBar.progress = connected.currentPosition.toInt()
			currentTime.text = convertTimeToString(connected.currentPosition)
			when (connected.stateOfPlayer) {
				PlayerState.Pause    -> {
					adapter.resetDataOfPlayingSong(null)
					buttonStopOrStart.setImageDrawable(resources.getDrawable(R.drawable.ic_play, null))
				}

				PlayerState.Start    -> {
					adapter.resetDataOfPlayingSong(state.lastTrekEntity.uri)
					seekBar.max = state.lastTrekEntity.duration.toInt()
					maxTime.text = convertTimeToString(state.lastTrekEntity.duration)
					titleOfPlayingSong.text = state.lastTrekEntity.title
					buttonStopOrStart.setImageDrawable(resources.getDrawable(R.drawable.ic_pause, null))
				}

				PlayerState.Continue -> {
					adapter.resetDataOfPlayingSong(state.lastTrekEntity.uri)
					buttonStopOrStart.setImageDrawable(resources.getDrawable(R.drawable.ic_pause, null))
				}

				PlayerState.Stop     -> {
					adapter.resetDataOfPlayingSong(null)
				}

				PlayerState.Close    -> {
					adapter.resetDataOfPlayingSong(null)
					lowerMenu.visibility = View.GONE
					closeMediaService()
				}

				null                 -> Unit
			}
		}
	}

	private fun sendChangesInService(state: UIState.Content, connected: ServiceConnectionState.Connected) {
		when (connected.stateOfPlayer) {
			PlayerState.Pause    -> {
				mService?.requestPauseMedia()
			}

			PlayerState.Start    -> {
				mService?.resetTrek(state.lastTrekEntity)
			}

			PlayerState.Continue -> {
				mService?.requestPlayMedia()
			}

			else                 -> Unit
		}
	}

	private fun settingMediaService(state: UIState.Content) {
		initConnection()

		val appContext = activity?.applicationContext
		val intent = formServiceIntent(state)

		appContext?.let { context ->
			startMediaService(context, intent)
		}
	}

	private fun formServiceIntent(state: UIState.Content): Intent {
		val gson = Gson()
		val intent = Intent(context, MediaService::class.java)
		intent.putExtra(MediaService.LIST_OF_ENTITY_KEY, gson.toJson(state.listOfTrekFile))
		intent.putExtra(MediaService.MUSIC_ENTITY_KEY, gson.toJson(state.lastTrekEntity))
		return intent
	}

	private fun startMediaService(context: Context, intent: Intent) {
		connection?.let { conn ->
			context.startForegroundService(intent)
			context.bindService(intent, conn, Context.BIND_AUTO_CREATE)
		}
		viewModel.serviceIsStarting()
	}

	private fun closeMediaService() {
		mService?.state?.removeObservers(viewLifecycleOwner)
		connection?.let { conn -> requireActivity().applicationContext.unbindService(conn) }
		connection = null
		mBound = false
		mService = null
		viewModel.serviceIsEnding()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		closeMediaService()
	}
}