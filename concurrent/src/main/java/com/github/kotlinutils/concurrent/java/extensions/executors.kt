@file:Suppress("NOTHING_TO_INLINE")

package com.github.kotlinutils.concurrent.java.extensions

import android.os.Process
import com.github.kotlinutils.concurrent.java.JavaSingleThreadFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Create Single-Thread Executor with thread that auto-terminates on timeout if it's idle
 *
 * @param threadName [String]: name of the thread
 * @param keepAliveTime [Long]: timeout after which the idle thread will auto-terminate
 * @param timeUnit [TimeUnit]: the time unit for the keepAliveTime argument
 * @param javaThreadPriority [Int]: Java Thread priority of the thread, from 10 for highest priority to 1 for lowest priority ([Thread.NORM_PRIORITY] by default)
 *
 * @return new Single-Threaded [ThreadPoolExecutor] with a thread that has specified name & priority and auto-terminates on specified timeout if it's idle
 */
inline fun singleThreadExecutorWithAutoShutdown(threadName: String,
                                                keepAliveTime: Long,
                                                timeUnit: TimeUnit,
                                                javaThreadPriority: Int
                                                = Thread.NORM_PRIORITY
): ThreadPoolExecutor {
    return ThreadPoolExecutor(1, 1,
        keepAliveTime, timeUnit,
        LinkedBlockingQueue<Runnable>(),
        JavaSingleThreadFactory(threadName, javaThreadPriority)
    ).apply { allowCoreThreadTimeOut(true) }
}

/**
 * Create Single-Thread Executor with thread that auto-terminates on timeout if it's idle
 *
 * @param threadName [String]: name of the thread
 * @param javaThreadPriority [Int]: Java Thread priority of the thread, from 10 for highest priority to 1 for lowest priority ([Thread.NORM_PRIORITY] by default)
 *
 * @return new Single-Threaded [ThreadPoolExecutor] with a thread that has specified name & priority and auto-terminates on specified timeout if it's idle
 */
inline fun singleThreadExecutor(threadName: String,
                                javaThreadPriority: Int
                                = Thread.NORM_PRIORITY
): ExecutorService {
    return Executors.newSingleThreadExecutor(JavaSingleThreadFactory(threadName, javaThreadPriority))
}