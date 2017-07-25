@file:Suppress("NOTHING_TO_INLINE")
@file:JvmName("HandlerThreadUtils")

package com.github.kotlinutils.concurrent.android.extensions

import android.os.Build
import android.os.HandlerThread
import java.util.*

/**
 * Created by nbv54 on 21-Apr-17.
 */

    inline fun HandlerThread.quitSafelyIfPossible(safetySleepMs : Long = 0L)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            this.quitSafely()
        }
        else
        {
            if (safetySleepMs != 0L)
            {
                Timer().schedule( object : TimerTask() {
                        override fun run() {
                            this@quitSafelyIfPossible.quit()
                    }
                }, safetySleepMs)
            }
            else
            {
                this.quit()
            }
        }
    }