@file:Suppress("NOTHING_TO_INLINE")

package com.github.kotlinutils.concurrent.java.extensions

/**
 * Created by nbv54 on 21-Apr-17.
 */
inline val <T : Any> T.curThreadNameInBr : String
            get() = "[${Thread.currentThread().name}]"
//            get() = Thread.currentThread().let { "[${it.name} ${it.priority}]" }

