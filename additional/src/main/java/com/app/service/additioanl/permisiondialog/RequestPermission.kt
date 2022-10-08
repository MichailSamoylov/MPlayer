package com.app.service.additioanl.permisiondialog

import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment

fun Fragment.registerPermissionLauncher(
	permission: Permission,
	resultBlock: (result: PermissionResult) -> Unit
): ActivityResultLauncher<Permission> =
	registerForActivityResult(PermissionResultContract()) { rawResult ->
		val result = rawResult
			.takeIf { it.contains(permission) }
			?.mapValues(::mapResult)
			?.takeFirst()
			?: PermissionResult.DENY
		resultBlock(result)
	}

private fun Fragment.mapResult(entry: Map.Entry<String, Boolean>): PermissionResult {
	val (manifestPermission, granted) = entry
	return when {
		granted                                                   -> PermissionResult.GRANTED
		!shouldShowRequestPermissionRationale(manifestPermission) -> PermissionResult.DENY_PERMANENTLY
		else                                                      -> PermissionResult.DENY
	}
}

private fun Map<String, Boolean>.contains(permission: Permission): Boolean {
	val permissionsRequest = listOf(permission.manifestPermission)
	return keys.containsAll(permissionsRequest)
}

private fun Map<String, PermissionResult>.takeFirst(): PermissionResult =
	values.first()