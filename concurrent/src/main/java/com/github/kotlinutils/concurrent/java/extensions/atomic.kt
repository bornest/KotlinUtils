package com.github.kotlinutils.concurrent.java.extensions

import java.util.concurrent.atomic.AtomicReference

/**
 * Reference to object stored in this AtomicReference:
 *
 * Reading from this property is equivalent to calling [AtomicReference.get]
 *
 * Writing to this property is equivalent to calling [AtomicReference.set]
 */
var <T : Any?> AtomicReference<T>.v: T
    inline get() = this.get()
    set(value) = this.set(value)