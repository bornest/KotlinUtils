package com.github.kotlinutils.core.extensions

/**
 * Created by nbv54 on 30-May-17.
 */
inline val <T : Any> T.classSimpleName : String
    get() = this::class.simpleName ?: ""

inline val <T: kotlin.reflect.KClass<*>> T.simpleNameOrEmptyStr : String
        get() = this.simpleName ?: ""