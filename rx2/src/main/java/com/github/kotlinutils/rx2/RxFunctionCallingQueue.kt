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

@Suppress("NOTHING_TO_INLINE")
/**
 * Queue of function objects that are called in a serialized manner on specified [Scheduler]
 */
class RxFunctionCallingQueue(
    override val logTag: String,
    override var loggingEnabled: Boolean = true
) : Loggable {
    val asyncCallsBus = rxTypedPublishBus<() -> Unit>()
    var asyncCallsDisposable: Disposable? = null
    
    /**
     * Start processing the queue
     *
     * @param scheduler [Scheduler] to call functions on (**Schedulers.io() by default**)
     */
    inline fun setupSubscription(scheduler: Scheduler = Schedulers.io()) {
        if (asyncCallsDisposable.isDisposedOrNull) {
            asyncCallsDisposable = asyncCallsBus.toObservable()
                .observeOn(scheduler)
                .subscribe {
                    it()
                }
        }
    }
    
    /**
     * Start processing the queue
     *
     * @param scheduler [Scheduler] to call functions on (**Schedulers.io() by default**)
     * @param doOnEachCall additional action to perform before each call
     */
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
    
    /**
     * Stop processing the queue
     */
    fun clearSubscription() {
        if (!asyncCallsDisposable.isDisposedOrNull) {
            asyncCallsDisposable?.dispose()
            asyncCallsDisposable = null
        }
    }
    
    /**
     * Add function to queue
     *
     * Adds the function to the queue. Logs provided message on enqueue and on execution of the function.
     *
     * @param message comment about the function that's being enqueued (usually its name)
     * @param action function to enqueue
     */
    inline fun enq(message: String? = null, crossinline action: () -> Unit) {
        message?.let { d { " $curThreadNameInBr callingQueue Enqueue: $it" } }
        val lambda = {
            message?.let { i { " $curThreadNameInBr callingQueue Performing: $message" } }
            action()
        }
        asyncCallsBus.send(lambda)
    }
    
    /**
     * Add function to queue
     *
     * Adds the function to the queue. Logs provided message on enqueue and on execution of the function.
     *
     * @param customTag custom log tag to use while logging on enqueue and on execution of the function
     * @param message comment about the function that's being enqueued (usually its name)
     * @param customLoggingCondition custom logging condition (*is equal to the value of loggingEnabled by default*)
     * @param action function to enqueue
     */
    inline fun enq(customTag: String, message: String, customLoggingCondition: Boolean = loggingEnabled, crossinline action: () -> Unit) {
        d(customLoggingCondition, customTag) { " $curThreadNameInBr callingQueue Enqueue: $message" }
        val lambda = {
            i(customTag) { " $curThreadNameInBr callingQueue Performing: $message" }
            action()
        }
        asyncCallsBus.send(lambda)
    }
}