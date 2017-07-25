package com.github.kotlinutils.concurrent.java

import java.util.concurrent.ThreadFactory

/**
 * Created by nbv54 on 16-Jun-17.
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