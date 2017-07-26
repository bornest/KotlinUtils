@file:Suppress("NOTHING_TO_INLINE")
@file:JvmName("HandlerThreadUtils")

package com.github.kotlinutils.concurrent.android.extensions

import android.os.Build
import android.os.HandlerThread
import java.util.*

/**
 * Try to quit this HandlerThread safely
 *
 * Calls [HandlerThread.quitSafely] if SDK version >= 18
 *
 * If SDK version < 18 calls [HandlerThread.quit] after specified timeout
 *
 * @param timeoutMs [Long]: timeout in milliseconds after [HandlerThread.quit] is called (**0ms by default**)
 */
inline fun HandlerThread.tryToQuitSafely(timeoutMs: Long = 0L) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
        this.quitSafely()
    } else {
        if (timeoutMs != 0L) {
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    this@tryToQuitSafely.quit()
                }
            }, timeoutMs)
        } else {
            this.quit()
        }
    }
}