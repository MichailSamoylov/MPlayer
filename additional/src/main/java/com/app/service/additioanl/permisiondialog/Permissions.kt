package com.app.service.additioanl.permisiondialog

import android.Manifest
import java.io.Serializable

sealed class Permission(internal val manifestPermission: String) : Serializable

object ReadExternalStorage : Permission(Manifest.permission.READ_EXTERNAL_STORAGE)

object ForegroundService : Permission(Manifest.permission.FOREGROUND_SERVICE)