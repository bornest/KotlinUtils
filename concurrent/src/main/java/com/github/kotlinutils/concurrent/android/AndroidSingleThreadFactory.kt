package com.github.kotlinutils.concurrent.android

import android.os.Process
import java.util.concurrent.ThreadFactory

/**
 * Created by nbv54 on 26-May-17.
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