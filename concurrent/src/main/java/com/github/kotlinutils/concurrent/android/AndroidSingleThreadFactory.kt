package com.github.kotlinutils.concurrent.android

import android.os.Process
import java.util.concurrent.ThreadFactory

/**
 * Simple single thread factory with customizable thread name and Android (Linux) thread priority level
 *
 * @property threadName name of the threads created by this thread factory
 * @property androidThreadPriority a Linux priority level, from -20 for highest scheduling priority to 19 for lowest scheduling priority
 */
class AndroidSingleThreadFactory(
    @JvmField val threadName: String,
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