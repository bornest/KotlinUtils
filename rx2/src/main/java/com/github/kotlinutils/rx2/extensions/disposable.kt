package com.github.kotlinutils.rx2.extensions

import io.reactivex.disposables.Disposable

/**
 * Created by nbv54 on 24-Mar-17.
 */

/**
 * true if this disposable hasn't been disposed of
 */
inline val Disposable.isNotDisposed : Boolean
    get() = !this.isDisposed

/**
 * true if this disposable is null or has been disposed of
 */
inline val Disposable?.isDisposedOrNull : Boolean
    get() = this?.isDisposed ?: true
