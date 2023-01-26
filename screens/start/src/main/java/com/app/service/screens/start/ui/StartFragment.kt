package com.app.service.screens.start.ui

import android.Manifest.permission.*
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.*
import androidx.fragment.app.Fragment
import com.app.service.additioanl.permisiondialog.ForegroundService
import com.app.service.additioanl.permisiondialog.PermissionResult
import com.app.service.additioanl.permisiondialog.ReadExternalStorage
import com.app.service.additioanl.permisiondialog.registerPermissionLauncher
import com.app.service.screens.start.databinding.FragmentStartBinding
import com.app.service.screens.start.presentation.StartViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class StartFragment : Fragment() {

	companion object {

		private const val ANIMATION_DURATION = 1000L
		private const val ANIMATION_START_DELAY = 1500L
		fun newInstance() = StartFragment()
	}

	private lateinit var binding: FragmentStartBinding
	private val viewModel: StartViewModel by viewModel()
	private val readExternalStoragePermissionLauncher = registerPermissionLauncher(ReadExternalStorage, ::handlePermissionResult)
	private val foregroundServicePermissionLauncher = registerPermissionLauncher(ForegroundService, ::handlePermissionResult)

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentStartBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		if (checkSelfPermission(requireContext(), READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			readExternalStoragePermissionLauncher.launch(ReadExternalStorage)
		} else if (checkSelfPermission(requireContext(), FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
			foregroundServicePermissionLauncher.launch(ForegroundService)
		}else{
			setAndStartLogoAnimation(binding.logo)
			//setAndStartLogoAnimation(binding.nameOfApp)
			viewModel.navigateToListScreen()
		}
	}

	private fun handlePermissionResult(result: PermissionResult) {
		setAndStartLogoAnimation(binding.logo)
		//setAndStartLogoAnimation(binding.nameOfApp)
		when (result) {
			PermissionResult.DENY_PERMANENTLY,
			PermissionResult.DENY    -> viewModel.exit()
			PermissionResult.GRANTED -> viewModel.navigateToListScreen()
		}
	}

	private fun setAndStartLogoAnimation(view: View) {
		val animationShow = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).setDuration(ANIMATION_DURATION)
		val animationHide = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f).setDuration(ANIMATION_DURATION)
		animationHide.startDelay = ANIMATION_START_DELAY

		animationShow.start()
		animationHide.start()
	}
}