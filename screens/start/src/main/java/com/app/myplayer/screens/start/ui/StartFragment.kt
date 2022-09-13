package com.app.myplayer.screens.start.ui

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import com.app.myplayer.additioanl.permisiondialog.PermissionResult
import com.app.myplayer.additioanl.permisiondialog.ReadExternalStorage
import com.app.myplayer.additioanl.permisiondialog.registerPermissionLauncher
import com.app.myplayer.screens.start.databinding.FragmentStartBinding
import com.app.myplayer.screens.start.presentation.StartViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class StartFragment : Fragment() {

	companion object {

		fun newInstance() = StartFragment()
	}

	private lateinit var binding: FragmentStartBinding
	private val viewModel: StartViewModel by viewModel()
	private val readExternalStoragePermissionLauncher = registerPermissionLauncher(ReadExternalStorage, ::handlePermissionResult)
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
		} else {
			viewModel.resetRootScreen()
		}
	}

	private fun handlePermissionResult(result: PermissionResult) {
		when (result) {
			PermissionResult.DENY_PERMANENTLY,
			PermissionResult.DENY    -> viewModel.exit()
			PermissionResult.GRANTED -> viewModel.resetRootScreen()
		}
	}

}