package com.app.service.service.servicestate

import androidx.lifecycle.Observer

class StateObserver(private val attachedInvoke: (ServiceState) -> Unit) : Observer<ServiceState> {

	override fun onChanged(t: ServiceState?) {
		t?.let(attachedInvoke)
	}
}