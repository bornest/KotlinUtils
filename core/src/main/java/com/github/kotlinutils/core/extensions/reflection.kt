package com.github.kotlinutils.core.extensions


/**
 * Simple name of this object's class (or empty string if there's none)
 */
inline val <T : Any> T.classSimpleName : String
    get() = this::class.simpleName ?: ""

/**
 * Simple name of this class (or empty string if there's none)
 */
inline val <T: kotlin.reflect.KClass<*>> T.simpleNameOrEmptyStr : String
        get() = this.simpleName ?: ""