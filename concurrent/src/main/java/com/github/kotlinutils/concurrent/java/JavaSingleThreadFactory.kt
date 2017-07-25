package com.github.kotlinutils.concurrent.java

import java.util.concurrent.ThreadFactory

/**
 * Simple single thread factory with customizable thread name and Java Thread priority level
 *
 * @param threadName name of the threads created by this thread factory
 * @param javaThreadPriority Java Thread priority level, from 10 for highest scheduling priority to 1 for lowest scheduling priority
 */
class JavaSingleThreadFactory(
    @JvmField val threadName: String,
    @JvmField val javaThreadPriority: Int
) : ThreadFactory {
    
    override fun newThread(runnable: Runnable?): Thread {
        return Thread(
            {
                runnable?.run()
            },
            threadName
        ).apply { priority = javaThreadPriority }
    }
}