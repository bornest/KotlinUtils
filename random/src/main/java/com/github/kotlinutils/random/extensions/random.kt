@file:Suppress("NOTHING_TO_INLINE")

package com.bornest.apps.onefamily.util.random.extensions

/**
 * Created by nbv54 on 17-Apr-17.
 */

inline fun sleepRandomTime(min: Long, max: Long) {
    if (max > 0 && min in 0..(max - 1)) {
        var randomDuration = (Math.random() * max.toDouble()).toLong()
        randomDuration = maxOf(randomDuration, min)
        Thread.sleep(randomDuration)
    }
    else {
        throw IllegalArgumentException("Parameters have to satisfy (max > 0 && min in 0..(max - 1)) !")
    }
}

inline fun randomBoolean(probOfTrue: Double): Boolean {
    if (probOfTrue > 0 && probOfTrue < 1) {
        return (Math.random() <= probOfTrue)
    }
    else if (probOfTrue == 0.0) {
        return false
    }
    else if (probOfTrue == 1.0) {
        return true
    }
    else {
        throw IllegalArgumentException("probOfTrue has to satisfy (probOfTrue >= 0 && probOfTrue < 1) !")
    }
}