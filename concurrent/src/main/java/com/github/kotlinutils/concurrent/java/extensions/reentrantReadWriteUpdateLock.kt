@file:Suppress("NOTHING_TO_INLINE")

package com.github.kotlinutils.concurrent.java.extensions

import com.googlecode.concurentlocks.ReentrantReadWriteUpdateLock

/**
 * Perform action under readLock
 *
 * 1. Acquires readLock
 * 2. Tries to perform specified action
 * 3. Releases readLock
 *
 * @param T return type of action to perform
 * @param action action to be performed
 *
 * @return return value of performed action
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

/**
 * Perform action under updateLock
 *
 * 1. Acquires updateLock
 * 2. Tries to perform specified action
 * 3. Releases updateLock
 *
 * @param T return type of action to perform
 * @param action action to be performed
 *
 * @return return value of performed action
 */
inline fun <T> ReentrantReadWriteUpdateLock.update(action: () -> T) : T {
    val ul = this.updateLock()
    ul.lock()
    try {
        return action()
    } finally {
        ul.unlock()
    }
}

/**
 * Perform action under writeLock
 *
 * 1. Acquires writeLock
 * 2. Tries to perform specified action
 * 3. Releases writeLock
 *
 * @param T return type of action to perform
 * @param action action to be performed
 *
 * @return return value of performed action
 */
inline fun <T> ReentrantReadWriteUpdateLock.write(action: () -> T) : T {
    val wl = this.writeLock()
    wl.lock()
    try {
        return action()
    } finally {
        wl.unlock()
    }
}



