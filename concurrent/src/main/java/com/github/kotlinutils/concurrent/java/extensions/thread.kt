@file:Suppress("NOTHING_TO_INLINE")

package com.github.kotlinutils.concurrent.java.extensions

/**
 * Name of current thread in square brackets
 */
inline val <T : Any> T.curThreadNameInBr : String
    get() = "[${Thread.currentThread().name}]"

/**
 * Name and priority of current thread in square brackets
 */
inline val <T : Any> T.curThreadNameAndPriorityInBr : String
    get() = Thread.currentThread().let { "[${it.name} ${it.priority}]" }

