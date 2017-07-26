package com.github.kotlinutils.concurrent.android

import android.os.Process
import java.util.concurrent.ThreadFactory

/**
 * Simple single thread factory with customizable thread name and Android (Linux) thread priority level
 *
 * @param threadName name of the threads created by this thread factory
 * @param androidThreadPriority a Linux priority level, from -20 for highest scheduling priority to 19 for lowest scheduling priority
 */
class AndroidSingleThreadFactory(
    /**
     * name of the threads created by this thread factory
     */
    @JvmField val threadName: String,
    /**
     * a Linux priority level, from -20 for highest scheduling priority to 19 for lowest scheduling priority
     */
    @JvmField val androidThreadPriority: Int
) : ThreadFactory {
    
    override fun newThread(runnable: Runnable?): Thread {
        return Thread(
            {
                Process.setThreadPriority(androidThreadPriority)
                runnable?.run()
            },
            threadName
        )
    }
}