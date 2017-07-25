@file:Suppress("NOTHING_TO_INLINE")

package com.github.kotlinutils.rx2.extensions

import com.github.unitimber.core.extensions.e
import com.github.unitimber.core.extensions.v
import com.github.unitimber.core.loggable.Loggable
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3

/**
 * Created by nbv54 on 10-Mar-17.
 */


//region Creation
    inline fun <T : Any> T.toSingle(): Single<T> = Single.just(this)
//endregion


//region logging Emitter
    inline fun SingleEmitter<*>.loggingOnError(loggingEnabled: Boolean = true,
                                               errorMessage: () -> String
    ) {
        e(loggingEnabled, null, errorMessage)
        this.onError(Throwable(errorMessage()))
    }

    inline fun Loggable.loggingOnError(singleEmitter: SingleEmitter<*>, errorMessage: () -> String) {
        e(errorMessage)
        singleEmitter.onError(Throwable(errorMessage()))
    }
    
    inline fun Loggable.loggingOnError(singleEmitter: SingleEmitter<*>, throwable: Throwable) {
        e(throwable)
        singleEmitter.onError(throwable)
    }

    inline fun Loggable.loggingOnError(singleEmitter: SingleEmitter<*>, throwable: Throwable, errorMessage: () -> String) {
        e(throwable, errorMessage)
        singleEmitter.onError(throwable)
    }

    inline fun SingleEmitter<String>.loggingOnSuccess(loggingEnabled: Boolean = true,
                                                      successMessage: () -> String
    ) {
        v(loggingEnabled, null, successMessage)
        this.onSuccess(successMessage())
    }

    inline fun <T : Any> SingleEmitter<T>.loggingOnSuccess(item: T,
                                                           loggingEnabled: Boolean = true,
                                                           successMessage: () -> String
    ) {
        v(loggingEnabled, null, successMessage)
        this.onSuccess(item)
    }

    inline fun Loggable.loggingOnSuccess(singleEmitter: SingleEmitter<String>,
                                         successMessage: () -> String
    ) {
        v(successMessage)
        singleEmitter.onSuccess(successMessage())
    }

    inline fun <T : Any> Loggable.loggingOnSuccess(singleEmitter: SingleEmitter<T>,
                                                   item: T,
                                                   successMessage: () -> String
    ) {
        v(successMessage)
        singleEmitter.onSuccess(item)
    }
//endregion


//region zip
    inline fun <reified T1 : Any, T2: Any, R : Any> singleZip(s1: Single<T1>, s2: Single<T2>, noinline zipFunction: (T1, T2) -> R) : Single<R>
    {
        return Single.zip(
            s1,
            s2,
            BiFunction<T1, T2, R>(zipFunction)
        )
    }

    inline fun <reified T1 : Any, T2: Any> singleZip(s1: Single<T1>, s2: Single<T2>): Single<Pair<T1, T2>>
    {
        return singleZip(s1, s2)
        {
            t1, t2 -> t1 to t2
        }
    }

    inline fun <reified T1 : Any, T2: Any, T3: Any, R : Any> singleZip(s1: Single<T1>, s2: Single<T2>, s3: Single<T3>, noinline zipFunction: (T1, T2, T3) -> R) : Single<R>
    {
        return Single.zip(
            s1,
            s2,
            s3,
            Function3<T1, T2, T3, R>(zipFunction)
        )
    }

    inline fun <reified T1 : Any, T2: Any, T3: Any> singleZip(s1: Single<T1>, s2: Single<T2>, s3: Single<T3>): Single<Triple<T1, T2, T3>>
    {
        return singleZip(s1, s2, s3)
        {
            t1, t2, t3 -> Triple(t1, t2,  t3)
        }
    }
//endregion


//region zipWith
    inline fun <reified T1 : Any, T2: Any, R : Any> Single<T1>.zipWith(s2: Single<T2>, noinline zipFunction: (T1, T2) -> R) : Single<R>
    {
        return singleZip(this, s2, zipFunction)
    }

    inline fun <reified T1 : Any, T2: Any> Single<T1>.zipWith(s2: Single<T2>): Single<Pair<T1, T2>>
    {
        return singleZip(this, s2)
    }

    inline fun <reified T1 : Any, T2: Any, T3: Any, R : Any> Single<T1>.zipWith(s2: Single<T2>, s3: Single<T3>, noinline zipFunction: (T1, T2, T3) -> R) : Single<R>
    {
        return singleZip(this, s2, s3, zipFunction)
    }

    inline fun <reified T1 : Any, T2: Any, T3: Any> Single<T1>.zipWith(s2: Single<T2>, s3: Single<T3>): Single<Triple<T1, T2, T3>>
    {
        return singleZip(this, s2, s3)
    }
//endregion


//region doOn*
    inline fun <T : Any> Single<T>.doOnSuccessIf(condition: Boolean, crossinline action: (T) -> Unit): Single<T> {
        return if (condition) this.doOnSuccess { action(it) } else this
    }
//endregion