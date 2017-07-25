@file:Suppress("NOTHING_TO_INLINE")

package com.github.kotlinutils.rx2.extensions

import android.os.Build
import android.os.HandlerThread
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import com.twitter.jsr166e.ForkJoinPool as ForkJoinPoolBackport

/**
 * Created by nbv54 on 11-Mar-17.
 */

inline fun HandlerThread.toScheduler() : Scheduler
    = AndroidSchedulers.from(this.looper)

inline val HandlerThread.scheduler : Scheduler
    get() = AndroidSchedulers.from(this.looper)

inline val Executor.scheduler : Scheduler
    get() = Schedulers.from(this)

object ForkJoinCommonPoolScheduler {
    val _scheduler : Scheduler = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Schedulers.from(ForkJoinPool.commonPool())
        else
            Schedulers.from(ForkJoinPoolBackport.commonPool())
    inline operator fun invoke(): Scheduler = _scheduler
}