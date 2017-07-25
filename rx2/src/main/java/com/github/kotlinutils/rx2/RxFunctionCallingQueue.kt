package com.github.kotlinutils.rx2

import com.github.kotlinutils.concurrent.java.extensions.curThreadNameInBr
import com.github.kotlinutils.rx2.bus.rxTypedPublishBus
import com.github.kotlinutils.rx2.extensions.isDisposedOrNull
import com.github.unitimber.core.extensions.d
import com.github.unitimber.core.extensions.i
import com.github.unitimber.core.loggable.Loggable
import com.github.unitimber.core.loggable.extensions.d
import com.github.unitimber.core.loggable.extensions.i
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by nbv54 on 24-Mar-17.
 */
@Suppress("NOTHING_TO_INLINE")
/**
 * Created by nbv54 on 09-Feb-17.
 */
class RxFunctionCallingQueue(
    override val logTag: String,
    override var loggingEnabled: Boolean = true
) : Loggable {
    val asyncCallsBus = rxTypedPublishBus<() -> Unit>()
    var asyncCallsDisposable: Disposable? = null

    inline fun setupSubscription(scheduler: Scheduler = Schedulers.io()) {
        if (asyncCallsDisposable.isDisposedOrNull) {
            asyncCallsDisposable = asyncCallsBus.toObservable()
                .observeOn(scheduler)
                .subscribe {
                    it()
                }
        }
    }

    inline fun setupSubscription(scheduler: Scheduler = Schedulers.io(), crossinline doOnEachCall: () -> Unit) {
        if (asyncCallsDisposable.isDisposedOrNull) {
            asyncCallsDisposable = asyncCallsBus.toObservable()
                .observeOn(scheduler)
                .subscribe {
                    doOnEachCall()
                    it.invoke()
                }
        }
    }

    fun clearSubscription() {
        if (!asyncCallsDisposable.isDisposedOrNull) {
            asyncCallsDisposable?.dispose()
            asyncCallsDisposable = null
        }
    }

    inline fun enq(message: String? = null, crossinline action: () -> Unit) {
        message?.let { d { " $curThreadNameInBr callingQueue Enqueue: $it" } }
        val lambda = {
            message?.let { i { " $curThreadNameInBr callingQueue Performing: $message" } }
            action()
        }
        asyncCallsBus.send(lambda)
    }

    inline fun enq(customTag: String, message: String, customLoggingCondition: Boolean = loggingEnabled, crossinline action: () -> Unit) {
        d(customLoggingCondition, customTag) { " $curThreadNameInBr callingQueue Enqueue: $message" }
        val lambda = {
            i(customTag) { " $curThreadNameInBr callingQueue Performing: $message" }
            action()
        }
        asyncCallsBus.send(lambda)
    }

    inline fun callAsyncIfTrueElseCallBlocking(condition: Boolean = true, crossinline action: () -> Unit) {
        if (condition) {
            val lambda = {
                action()
            }
            asyncCallsBus.send(lambda)
        } else {
            action()
        }
    }
}