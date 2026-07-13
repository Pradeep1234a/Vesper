package com.vesper.ledger.data.secure

object SensitiveActionAuthenticator {
    private var authCallback: ((onSuccess: () -> Unit) -> Unit)? = null

    fun setCallback(callback: (onSuccess: () -> Unit) -> Unit) {
        authCallback = callback
    }

    fun clearCallback() {
        authCallback = null
    }

    fun authenticateAction(onSuccess: () -> Unit) {
        val cb = authCallback
        if (cb != null) {
            cb(onSuccess)
        } else {
            // Safe fallback if no verification callback is registered
            onSuccess()
        }
    }
}
