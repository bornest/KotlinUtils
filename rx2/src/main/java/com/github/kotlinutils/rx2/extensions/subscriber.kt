@file:Suppress("NOTHING_TO_INLINE")

package com.github.kotlinutils.rx2.extensions

import io.reactivex.subscribers.DisposableSubscriber

/**
 * Created by nbv54 on 10-Apr-17.
 */

typealias RequestFun = (Long) -> Unit
inline fun RequestFun.request(n: Long) = this.invoke(n)

inline fun <T : Any> createDisposableSubscriber(crossinline onNext: RequestFun.(T) -> Unit,
                                                crossinline onComplete: () -> Unit,
                                                crossinline onError: (Throwable?) -> Unit,
                                                crossinline onStart: RequestFun.() -> Unit
) : DisposableSubscriber<T> {
    return object : DisposableSubscriber<T>(){
        override fun onNext(item: T) {
            onNext.invoke(this::request, item)
        }

        override fun onComplete() {
            onComplete.invoke()
        }

        override fun onError(t: Throwable?) {
            onError.invoke(t)
        }

        override fun onStart() {
            onStart.invoke(this::request)
        }
    }
}

inline fun <T : Any> createDisposableSubscriber(crossinline onNext: RequestFun.(T) -> Unit,
                                                crossinline onComplete: () -> Unit,
                                                crossinline onError: (Throwable?) -> Unit
) : DisposableSubscriber<T>
{
    return object : DisposableSubscriber<T>(){
        override fun onNext(item: T) {
            onNext.invoke(this::request, item)
        }

        override fun onComplete() {
            onComplete.invoke()
        }

        override fun onError(t: Throwable?) {
            onError.invoke(t)
        }
    }
}

