@file:Suppress("NOTHING_TO_INLINE")

package com.github.kotlinutils.rx2.extensions

import com.github.unitimber.core.extensions.e
import com.github.unitimber.core.loggable.Loggable
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3

/**
 * Created by nbv54 on 08-Mar-17.
 */

typealias Lambda0<T> = () -> T

//region Creation
    inline fun <T : Any> T.toSingletonObservable(): Observable<T> = Observable.just(this)

    inline fun <T : Any> Lambda0<T>.observableFromCallable() : Observable<T> = Observable.fromCallable { this() }

//endregion


//region logging Emitter
    inline fun ObservableEmitter<*>.loggingOnError(loggingEnabled: Boolean = true, errorMessage: () -> String)
    {
        e(loggingEnabled, null, errorMessage)
        this.onError(Throwable(errorMessage()))
    }

    inline fun Loggable.loggingOnError(observableEmitter: ObservableEmitter<*>, errorMessage: () -> String) {
        e(errorMessage)
        observableEmitter.onError(Throwable(errorMessage()))
    }

    inline fun Loggable.loggingOnError(observableEmitter: ObservableEmitter<*>, throwable: Throwable) {
        e(throwable)
        observableEmitter.onError(throwable)
    }

    inline fun Loggable.loggingOnError(observableEmitter: ObservableEmitter<*>, throwable: Throwable, errorMessage: () -> String) {
        e(throwable, errorMessage)
        observableEmitter.onError(throwable)
    }
//endregion


//region combineLatest
    inline fun <reified T1 : Any, T2: Any, R : Any> observableCombineLatest(o1: Observable<T1>, o2: Observable<T2>, noinline combineFunction: (T1, T2) -> R) : Observable<R>
    {
        return Observable.combineLatest(
            o1,
            o2,
            BiFunction<T1, T2, R>(combineFunction)
        )
    }

    inline fun <reified T1 : Any, T2: Any> observableCombineLatest(o1: Observable<T1>, o2: Observable<T2>): Observable<Pair<T1, T2>>
    {
        return observableCombineLatest(o1, o2)
        {
            t1, t2 -> t1 to t2
        }
    }

    inline fun <reified T1 : Any, T2: Any, T3: Any, R : Any> observableCombineLatest(o1: Observable<T1>, o2: Observable<T2>, o3: Observable<T3>, noinline combineFunction: (T1, T2, T3) -> R) : Observable<R>
    {
        return Observable.combineLatest(
            o1,
            o2,
            o3,
            Function3<T1, T2, T3, R>(combineFunction)
        )
    }

    inline fun <reified T1 : Any, T2: Any, T3: Any> observableCombineLatest(o1: Observable<T1>, o2: Observable<T2>, o3: Observable<T3>): Observable<Triple<T1, T2, T3>>
    {
        return observableCombineLatest(o1, o2, o3)
        {
            t1, t2, t3 -> Triple(t1, t2, t3)
        }
    }
//endregion

//region combineLatestWith
    inline fun <reified T1 : Any, T2: Any, R : Any> Observable<T1>.combineLatestWith(o2: Observable<T2>, noinline combineFunction: (T1, T2) -> R) : Observable<R>
    {
        return observableCombineLatest(this, o2, combineFunction)
    }

    inline fun <reified T1 : Any, T2: Any> Observable<T1>.combineLatestWith(o2: Observable<T2>): Observable<Pair<T1, T2>>
    {
        return observableCombineLatest(this, o2)
        {
            t1, t2 -> t1 to t2
        }
    }

    inline fun <reified T1 : Any, T2: Any, T3: Any, R : Any> Observable<T1>.combineLatestWith(o2: Observable<T2>, o3: Observable<T3>, noinline combineFunction: (T1, T2, T3) -> R) : Observable<R>
    {
        return observableCombineLatest(this, o2, o3, combineFunction)
    }

    inline fun <reified T1 : Any, T2: Any, T3: Any> Observable<T1>.combineLatestWith(o2: Observable<T2>, o3: Observable<T3>): Observable<Triple<T1, T2, T3>>
    {
        return observableCombineLatest(this, o2, o3)
        {
            t1, t2, t3 -> Triple(t1, t2, t3)
        }
    }
//endregion


//region zip
    inline fun <reified T1 : Any, T2: Any, R : Any> observableZip(o1: Observable<T1>, o2: Observable<T2>, noinline zipFunction: (T1, T2) -> R) : Observable<R>
    {
        return Observable.zip(
            o1,
            o2,
            BiFunction<T1, T2, R>(zipFunction)
        )
    }

    inline fun <reified T1 : Any, T2: Any> observableZip(o1: Observable<T1>, o2: Observable<T2>): Observable<Pair<T1, T2>>
    {
        return observableZip(o1, o2)
        {
            t1, t2 -> t1 to t2
        }
    }

    inline fun <reified T1 : Any, T2: Any, T3: Any, R : Any> observableZip(o1: Observable<T1>, o2: Observable<T2>, o3: Observable<T3>, noinline zipFunction: (T1, T2, T3) -> R) : Observable<R>
    {
        return Observable.zip(
            o1,
            o2,
            o3,
            Function3<T1, T2, T3, R>(zipFunction)
        )
    }

    inline fun <reified T1 : Any, T2: Any, T3: Any> observableZip(o1: Observable<T1>, o2: Observable<T2>, o3: Observable<T3>): Observable<Triple<T1, T2, T3>>
    {
        return observableZip(o1, o2, o3)
        {
            t1, t2, t3 -> Triple(t1, t2, t3)
        }
    }
//endregion

//region zipWith
    inline fun <reified T1 : Any, T2: Any, R : Any> Observable<T1>.zipWith(o2: Observable<T2>, noinline zipFunction: (T1, T2) -> R) : Observable<R>
    {
        return observableZip(this, o2, zipFunction)
    }

    inline fun <reified T1 : Any, T2: Any> Observable<T1>.zipWith(o2: Observable<T2>): Observable<Pair<T1, T2>>
    {
        return observableZip(this, o2)
        {
            t1, t2 -> t1 to t2
        }
    }

    inline fun <reified T1 : Any, T2: Any, T3: Any, R : Any> Observable<T1>.zipWith(o2: Observable<T2>, o3: Observable<T3>, noinline zipFunction: (T1, T2, T3) -> R) : Observable<R>
    {
        return observableZip(this, o2, o3, zipFunction)
    }

    inline fun <reified T1 : Any, T2: Any, T3: Any> Observable<T1>.zipWith(o2: Observable<T2>, o3: Observable<T3>): Observable<Triple<T1, T2, T3>>
    {
        return observableZip(this, o2, o3)
        {
            t1, t2, t3 -> Triple(t1, t2, t3)
        }
    }
//endregion

//region doOn*
    inline fun <T : Any, R : Any> Observable<T>.doOnNth(n: Int, crossinline action: (T) -> R): Observable<T> {
        return this.compose(object : ObservableTransformer<T, T> {
            var count = 0
            override fun apply(sourceObs: Observable<T>): ObservableSource<T> {
                return sourceObs.doOnNext {
                    count++
                    if (count == n) {
                        action(it)
                    }
                }
            }
        })
    }
    
    inline fun <T : Any, R : Any> Observable<T>.doOnFirst(crossinline action: (T) -> R): Observable<T> {
        return this.compose(object : ObservableTransformer<T, T> {
            var done = false
            override fun apply(sourceObs: Observable<T>): ObservableSource<T> {
                return sourceObs.doOnNext {
                    if (!done) {
                        action(it)
                        done = true
                    }
                }
            }
        })
    }

    inline fun <T : Any, R : Any> Observable<T>.doOnNextIf(condition: Boolean,
                                                           crossinline action: (T) -> R
    ): Observable<T> {
        return if (condition) this.doOnNext { action(it) } else this
    }

    inline fun <T : Any, R : Any> Observable<T>.doOnFirstIf(condition: Boolean,
                                                           crossinline action: (T) -> R
    ): Observable<T> {
        return if (condition) this.doOnFirst { action(it) } else this
    }
//endregion