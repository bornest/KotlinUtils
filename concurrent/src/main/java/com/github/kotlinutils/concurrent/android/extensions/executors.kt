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
 * Created by nbv54 on 26-May-17.
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

inline fun singleThreadExecutor(threadName: String,
                                androidThreadPriority: Int
                                = Process.THREAD_PRIORITY_BACKGROUND
): ExecutorService {
    return Executors.newSingleThreadExecutor(AndroidSingleThreadFactory(threadName, androidThreadPriority))
}