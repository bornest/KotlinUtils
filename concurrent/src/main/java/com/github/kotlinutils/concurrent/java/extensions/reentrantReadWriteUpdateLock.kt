@file:Suppress("NOTHING_TO_INLINE")

package com.github.kotlinutils.concurrent.java.extensions

import com.googlecode.concurentlocks.ReentrantReadWriteUpdateLock

/**
 * Created by nbv54 on 09-Jun-17.
 */

inline fun <T> ReentrantReadWriteUpdateLock.read(action: () -> T) : T {
    val rl = this.readLock()
    rl.lock()
    try {
        return action()
    } finally {
        rl.unlock()
    }
}

inline fun <T> ReentrantReadWriteUpdateLock.update(action: () -> T) : T {
    val ul = this.updateLock()
    ul.lock()
    try {
        return action()
    } finally {
        ul.unlock()
    }
}

inline fun <T> ReentrantReadWriteUpdateLock.write(action: () -> T) : T {
    val wl = this.writeLock()
    wl.lock()
    try {
        return action()
    } finally {
        wl.unlock()
    }
}



