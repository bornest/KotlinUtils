@file:Suppress("NOTHING_TO_INLINE")

package com.github.kotlinutils.concurrent.android.extensions

import android.os.Process
import com.github.kotlinutils.concurrent.android.AndroidSingleThreadFactory
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
 * @param androidThreadPriority [Int]: a Linux priority level of the thread, from -20 for highest priority to 19 for lowest priority ([Process.THREAD_PRIORITY_BACKGROUND] by default)
 *
 * @return new Single-Threaded [ThreadPoolExecutor] with a thread that has specified name & priority and auto-terminates on specified timeout if it's idle
 */
inline fun singleThreadExecutorWithAutoShutdown(threadName: String,
                                                keepAliveTime: Long,
                                                timeUnit: TimeUnit,
                                                androidThreadPriority: Int
                                                = Process.THREAD_PRIORITY_BACKGROUND
): ThreadPoolExecutor {
    return ThreadPoolExecutor(1, 1,
        keepAliveTime, timeUnit,
        LinkedBlockingQueue<Runnable>(),
        AndroidSingleThreadFactory(threadName, androidThreadPriority)
    ).apply { allowCoreThreadTimeOut(true) }
}

/**
 * Create Single-Thread Executor
 *
 * @param threadName [String]: name of the thread
 * @param androidThreadPriority [Int]: a Linux priority level of the thread, from -20 for highest priority to 19 for lowest priority ([Process.THREAD_PRIORITY_BACKGROUND] by default)
 *
 * @return new Single-Threaded [ThreadPoolExecutor] with a thread that has specified name & priority
 */
inline fun singleThreadExecutor(threadName: String,
                                androidThreadPriority: Int
                                = Process.THREAD_PRIORITY_BACKGROUND
): ExecutorService {
    return Executors.newSingleThreadExecutor(AndroidSingleThreadFactory(threadName, androidThreadPriority))
}