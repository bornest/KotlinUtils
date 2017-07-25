@file:Suppress("NOTHING_TO_INLINE")

package com.github.kotlinutils.id.extensions

import java.util.*

/**
 * Created by nbv54 on 30-May-17.
 */
inline val <T : Any?> T.identHashCode : Int
    get() = System.identityHashCode(this)

inline fun randomStrUUID() = UUID.randomUUID().toString()