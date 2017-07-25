@file:Suppress("NOTHING_TO_INLINE")

package com.github.kotlinutils.rx2.extensions

import com.github.unitimber.core.extensions.e
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.FlowableTransformer
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import org.reactivestreams.Publisher

//region Creation
    /**
     * Returns a Flowable that emits the object this method was called on and then completes
     */
    inline fun <T : Any> T.toSingletonFlowable(): Flowable<T> = Flowable.just(this)

    /**
     * Create a [Flowable]
     *
     * This method is equivalent to Flowable.create() but allows more idiomatic usage in Kotlin (by leaving lambda parameter outside of the brackets)
     *
     * @param T type of items to be emitted by this Flowable
     * @param backpressureStrategy the backpressure mode to apply if the downstream Subscriber doesn't request (fast) enough
     * @param source lambda with [FlowableEmitter] parameter that is called when a Subscriber subscribes to the returned Flowable
     *
     * @see Flowable.create
     */
    inline fun <T : Any> createFlowable(backpressureStrategy: BackpressureStrategy = BackpressureStrategy.BUFFER,
                                        crossinline source: (FlowableEmitter<T>) -> Unit
    ): Flowable<T>
    {
        return Flowable.create<T>(
            {
                emitter -> source(emitter)
            },
            backpressureStrategy
        )
    }
//endregion


//region logging Emitter
    
    inline fun FlowableEmitter<*>.loggingOnError(loggingEnabled: Boolean = true,
                                                 errorMessage: () -> String
    ) {
        e(loggingEnabled, null, errorMessage)
        this.onError(Throwable(errorMessage()))
    }
//endregion


//region combineLatest
    inline fun <reified T1 : Any, T2 : Any, R : Any> flowableCombineLatest(f1: Flowable<T1>,
                                                                           f2: Flowable<T2>,
                                                                           noinline combineFunction: (T1, T2) -> R
    ): Flowable<R> {
        return Flowable.combineLatest(
            f1,
            f2,
            BiFunction<T1, T2, R>(combineFunction)
        )
    }

    inline fun <reified T1 : Any, T2 : Any> flowableCombineLatest(f1: Flowable<T1>,
                                                                  f2: Flowable<T2>
    ): Flowable<Pair<T1, T2>> {
        return flowableCombineLatest(f1, f2)
        {
            t1, t2 ->
            t1 to t2
        }
    }

    inline fun <reified T1 : Any, T2 : Any, T3 : Any, R : Any> flowableCombineLatest(f1: Flowable<T1>,
                                                                                     f2: Flowable<T2>,
                                                                                     f3: Flowable<T3>,
                                                                                     noinline combineFunction: (T1, T2, T3) -> R
    ): Flowable<R> {
        return Flowable.combineLatest(
            f1,
            f2,
            f3,
            Function3<T1, T2, T3, R>(combineFunction)
        )
    }

    inline fun <reified T1 : Any, T2 : Any, T3 : Any> flowableCombineLatest(f1: Flowable<T1>,
                                                                            f2: Flowable<T2>,
                                                                            f3: Flowable<T3>
    ): Flowable<Triple<T1, T2, T3>> {
        return flowableCombineLatest(f1, f2, f3)
        {
            t1, t2, t3 ->
            Triple(t1, t2, t3)
        }
    }
//endregion

//region combineLatestWith
    inline fun <reified T1 : Any, T2 : Any, R : Any> Flowable<T1>.combineLatestWith(f2: Flowable<T2>,
                                                                                    noinline combineFunction: (T1, T2) -> R
    ): Flowable<R> {
        return flowableCombineLatest(this, f2, combineFunction)
    }

    inline fun <reified T1 : Any, T2 : Any> Flowable<T1>.combineLatestWith(f2: Flowable<T2>): Flowable<Pair<T1, T2>> {
        return flowableCombineLatest(this, f2)
        {
            t1, t2 ->
            t1 to t2
        }
    }

    inline fun <reified T1 : Any, T2 : Any, T3 : Any, R : Any> Flowable<T1>.combineLatestWith(f2: Flowable<T2>,
                                                                                              f3: Flowable<T3>,
                                                                                              noinline combineFunction: (T1, T2, T3) -> R
    ): Flowable<R> {
        return flowableCombineLatest(this, f2, f3, combineFunction)
    }

    inline fun <reified T1 : Any, T2 : Any, T3 : Any> Flowable<T1>.combineLatestWith(f2: Flowable<T2>,
                                                                                     f3: Flowable<T3>
    ): Flowable<Triple<T1, T2, T3>> {
        return flowableCombineLatest(this, f2, f3)
        {
            t1, t2, t3 ->
            Triple(t1, t2, t3)
        }
    }
//endregion


//region zip
    inline fun <reified T1 : Any, T2 : Any, R : Any> flowableZip(f1: Flowable<T1>,
                                                                 f2: Flowable<T2>,
                                                                 noinline zipFunction: (T1, T2) -> R
    ): Flowable<R> {
        return Flowable.zip(
            f1,
            f2,
            BiFunction<T1, T2, R>(zipFunction)
        )
    }

    inline fun <reified T1 : Any, T2 : Any> flowableZip(f1: Flowable<T1>,
                                                        f2: Flowable<T2>
    ): Flowable<Pair<T1, T2>> {
        return flowableZip(f1, f2)
        {
            t1, t2 ->
            t1 to t2
        }
    }

    inline fun <reified T1 : Any, T2 : Any, T3 : Any, R : Any> flowableZip(f1: Flowable<T1>,
                                                                           f2: Flowable<T2>,
                                                                           f3: Flowable<T3>,
                                                                           noinline zipFunction: (T1, T2, T3) -> R
    ): Flowable<R> {
        return Flowable.zip(
            f1,
            f2,
            f3,
            Function3<T1, T2, T3, R>(zipFunction)
        )
    }

    inline fun <reified T1 : Any, T2 : Any, T3 : Any> flowableZip(f1: Flowable<T1>,
                                                                  f2: Flowable<T2>,
                                                                  f3: Flowable<T3>
    ): Flowable<Triple<T1, T2, T3>> {
        return flowableZip(f1, f2, f3)
        {
            t1, t2, t3 ->
            Triple(t1, t2, t3)
        }
    }
//endregion

//region zipWith
    inline fun <reified T1 : Any, T2 : Any, R : Any> Flowable<T1>.zipWith(f2: Flowable<T2>,
                                                                          noinline zipFunction: (T1, T2) -> R
    ): Flowable<R> {
        return flowableZip(this, f2, zipFunction)
    }

    inline fun <reified T1 : Any, T2 : Any> Flowable<T1>.zipWith(f2: Flowable<T2>): Flowable<Pair<T1, T2>> {
        return flowableZip(this, f2)
        {
            t1, t2 ->
            t1 to t2
        }
    }

    inline fun <reified T1 : Any, T2 : Any, T3 : Any, R : Any> Flowable<T1>.zipWith(f2: Flowable<T2>,
                                                                                    f3: Flowable<T3>,
                                                                                    noinline zipFunction: (T1, T2, T3) -> R
    ): Flowable<R> {
        return flowableZip(this, f2, f3, zipFunction)
    }

    inline fun <reified T1 : Any, T2 : Any, T3 : Any> Flowable<T1>.zipWith(f2: Flowable<T2>,
                                                                           f3: Flowable<T3>
    ): Flowable<Triple<T1, T2, T3>> {
        return flowableZip(this, f2, f3)
        {
            t1, t2, t3 ->
            Triple(t1, t2, t3)
        }
    }
//endregion

//region doOn*
    inline fun <T : Any, R : Any> Flowable<T>.doOnNth(n: Int, crossinline action: (T) -> R): Flowable<T> {
        return this.compose(object : FlowableTransformer<T, T> {
            var count = 0
            override fun apply(sourceObs: Flowable<T>): Publisher<T> {
                return sourceObs.doOnNext {
                    count++
                    if (count == n) {
                        action(it)
                    }
                }
            }
        })
    }
    
    inline fun <T : Any, R : Any> Flowable<T>.doOnFirst(crossinline action: (T) -> R): Flowable<T> {
        return this.compose(object : FlowableTransformer<T, T> {
            var done = false
            override fun apply(sourceObs: Flowable<T>): Publisher<T> {
                return sourceObs.doOnNext {
                    if (done) {
                        action(it)
                        done = true
                    }
                }
            }
        })
    }
    
    inline fun <T : Any, R : Any> Flowable<T>.doOnNextIf(condition: Boolean,
                                                         crossinline action: (T) -> R
    ): Flowable<T> {
        return if (condition) {
            this.doOnNext { action(it) }
        } else {
            this
        }
    }
//endregion