package com.app.service.service

import com.app.service.service.servicestate.ServiceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlin.coroutines.CoroutineContext

private val coroutineJob = Job()

private object ServiceCoroutineContext : CoroutineScope {

	override val coroutineContext: CoroutineContext
		get() = (Dispatchers.Default + coroutineJob)
}

fun Job.canselAllChildren(){
	while(this.children.count()!=0){
		this.cancelChildren()
	}
}

val MediaService.serviceCoroutineContext: CoroutineScope
	get() = ServiceCoroutineContext

val MediaService.serviceJob: Job
	get() = coroutineJob



internal fun ServiceState.onOpen(action: ServiceState.Open.() -> Unit) {
	val tempState = this as? ServiceState.Open
	tempState?.let { it.action() }
}