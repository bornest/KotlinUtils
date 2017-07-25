package com.github.kotlinutils.concurrent.java.extensions

import java.util.concurrent.atomic.AtomicReference

/**
 * Created by nbv54 on 30-May-17.
 */
var <T : Any?> AtomicReference<T>.v: T
    inline get() = this.get()
    set(value) = this.set(value)