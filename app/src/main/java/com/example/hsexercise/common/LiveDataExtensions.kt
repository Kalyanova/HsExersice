package com.example.hsexercise.common

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 * Observe only non null values ([action] won't be triggered if [LiveData] will keep null value).
 */
inline fun <D : Any> LiveData<D>.observeNonNull(
    owner: LifecycleOwner,
    crossinline action: (D) -> Unit
) {
    observe(owner, Observer {
        if (it == null) {
            return@Observer
        }
        action(it)
    })
}
