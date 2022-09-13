package com.app.service.service.servicestate

import androidx.lifecycle.Observer

object StateObserver : Observer<ServiceState> {

	private var attachedInvoke: (ServiceState) -> Unit = {}

	fun observe(attachedInvoke: (ServiceState) -> Unit) {
		this.attachedInvoke = attachedInvoke
	}

	override fun onChanged(t: ServiceState?) {
		t?.let(attachedInvoke)
	}
}