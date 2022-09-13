package com.app.myplayer.additioanl.timeconverter

import java.text.SimpleDateFormat
import java.util.*

fun convertTimeToString(date: Long): String =
	SimpleDateFormat("mm:ss", Locale("ru")).format(date)