@file:Suppress("NOTHING_TO_INLINE")

package com.github.kotlinutils.concurrent.java.extensions

import com.github.kotlinutils.concurrent.java.JavaSingleThreadFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created by nbv54 on 16-Jun-17.
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

inline fun singleThreadExecutor(threadName: String,
                                javaThreadPriority: Int
                                = Thread.NORM_PRIORITY
): ExecutorService {
    return Executors.newSingleThreadExecutor(JavaSingleThreadFactory(threadName, javaThreadPriority))
}