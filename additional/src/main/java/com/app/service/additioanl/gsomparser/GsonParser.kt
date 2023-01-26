package com.app.service.additioanl.gsomparser

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

inline fun <reified T> reestablishObjectFromGson(gson: Gson, objectInGson: String): T? =
	gson.fromJson(objectInGson, object : TypeToken<T>() {}.type) as? T