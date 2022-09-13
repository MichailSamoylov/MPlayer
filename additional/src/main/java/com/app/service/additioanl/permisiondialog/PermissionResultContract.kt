package com.app.service.additioanl.permisiondialog

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts

class PermissionResultContract : ActivityResultContract<Permission, Map<String, Boolean>>() {

	private val rawContract = ActivityResultContracts.RequestMultiplePermissions()

	override fun createIntent(context: Context, input: Permission): Intent =
		rawContract.createIntent(context, getPermissions(input))

	override fun parseResult(resultCode: Int, intent: Intent?): Map<String, Boolean> =
		rawContract.parseResult(resultCode, intent)

	override fun getSynchronousResult(context: Context, input: Permission?): SynchronousResult<Map<String, Boolean>>? =
		rawContract.getSynchronousResult(context, input?.let(::getPermissions))

	private fun getPermissions(input: Permission): Array<String> =
		arrayOf(input.manifestPermission)
}