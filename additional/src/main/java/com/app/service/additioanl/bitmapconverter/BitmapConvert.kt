package com.app.service.additioanl.bitmapconverter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

fun convertResourceToBitmap(context: Context, id: Int): Bitmap? =
	BitmapFactory.decodeResource(context.resources, id)