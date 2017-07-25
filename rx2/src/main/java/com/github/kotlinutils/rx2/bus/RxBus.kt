@file:Suppress("NOTHING_TO_INLINE")

package com.github.kotlinutils.rx2.bus

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import io.reactivex.subjects.Subject
import java.util.concurrent.TimeUnit

/**
 * [RxBus] based on [PublishSubject] of type [Any]
 */
typealias RxPublishBus = RxBus<PublishSubject<Any>, Any>

/**
 * [RxBus] based on [PublishSubject] of type T
 */
typealias RxTypedPublishBus<T> = RxBus<PublishSubject<T>, T>

/**
 * [RxBus] based on [BehaviorSubject] of type [Any]
 */
typealias RxBehaviorBus = RxBus<BehaviorSubject<Any>, Any>

/**
 * [RxBus] based on [BehaviorSubject] of type T
 */
typealias RxTypedBehaviorBus<T> = RxBus<BehaviorSubject<T>, T>

/**
 * [RxBus] based on [ReplaySubject] of type [Any]
 */
typealias RxReplayBus = RxBus<ReplaySubject<Any>, Any>

/**
 * [RxBus] based on [ReplaySubject] of type T
 */
typealias RxTypedReplayBus<T> = RxBus<ReplaySubject<T>, T>

/**
 * A wrapper for [Subject] which represents an Event Bus.
 * Abstracts the functionality of underlying Subject by providing smaller interface relevant to this particular use case.
 *
 * @param S type of wrapped [Subject<D>]
 * @param D type of events to be passed through Bus
 * @param subject [Subject<D>] to wrap
 * @param serialized [Boolean] that determines whether to serialize the underlying Subject (**true by default**)
 *
 * @property dataType [Class] that corresponds to type of events to be passed through Bus (**null by default**)
 */
class RxBus<out S, D>(
    subject: S,
    val dataType: Class<D>? = null,
    serialized: Boolean = true
)
where S : Subject<D>, D : Any {
    private val _bus = subject.also { if (serialized) it.toSerialized() }
    /**
     * A Java Class of underlying [Subject]
     */
    val busType = subject::class.java

    /**
     * Send an item through the Bus
     *
     * @param item item to send
     */
    fun send(item: D) = _bus.onNext(item)

    /**
     * Returns true if the subject has any Observers.
     * This method is thread-safe.
     *
     * @return true if the subject has any Observers
     */
    fun hasObservers() = _bus.hasObservers()

    /**
     * Convert Bus into an Observable that emits events sent through this Bus.
     *
     * @return [Observable] which you can use to subscribe to events sent through this Bus
     */
    fun toObservable(): Observable<D>
        = _bus.hide()
    
    /**
     * Convert Bus into an Observable that emits events sent through this Bus only if they are of specified type.
     *
     * @param T the type to filter the items emitted by resulting Observable
     *
     * @return [Observable] which you can use to subscribe to events type  that are sent through this Bus
     */
    inline fun <reified T : D> toObservableOfType(): Observable<T>
        = toObservableOfType(T::class.java)
    
    /**
     * Convert Bus into a Observable that emits events sent through this Bus only if they are of specified type.
     *
     * @param eventType the [Class<T>] type to filter the items emitted by resulting Observable
     *
     * @return [Observable] which you can use to subscribe to events type  that are sent through this Bus
     */
    inline fun <T : D> toObservableOfType(eventType: Class<T>): Observable<T>
        = toObservable().ofType(eventType)
    
    /**
     * Convert Bus into a Flowable that emits events sent through this Bus.
     *
     * @return [Flowable] with [BackpressureStrategy.BUFFER] which you can use to subscribe to events sent through this Bus
     */
    inline fun toFlowable() = toFlowable(BackpressureStrategy.BUFFER)
    
    /**
     * Convert Bus into a Flowable that emits events sent through this Bus
     *
     * @param backpressureStrategy [BackpressureStrategy] you want to use
     *
     * @return [Flowable] with specified backpressure strategy which you can use to subscribe to events sent through this Bus
     */
    fun toFlowable(backpressureStrategy: BackpressureStrategy) : Flowable<D>
        = _bus.toFlowable(backpressureStrategy)
    
    /**
     * Convert Bus into a Flowable that emits events sent through this Bus only if they are of specified type.
     *
     * @param T the type to filter the items emitted by resulting Flowable
     * @param backpressureStrategy [BackpressureStrategy] you want to use (**[BackpressureStrategy.BUFFER] by default**)
     *
     * @return [Flowable] which you can use to subscribe to events type  that are sent through this Bus
     */
    inline fun <reified T : D> toFlowableOfType(backpressureStrategy: BackpressureStrategy = BackpressureStrategy.BUFFER) : Flowable<T>
        = toFlowableOfType(T::class.java, backpressureStrategy)
    
    /**
     * Convert Bus into a Flowable that emits events sent through this Bus only if they are of specified type.
     *
     * @param eventType the [Class<T>] type to filter the items emitted by resulting Flowable
     * @param backpressureStrategy [BackpressureStrategy] you want to use (**[BackpressureStrategy.BUFFER] by default**)
     *
     * @return [Flowable] which you can use to subscribe to events type  that are sent through this Bus
     */
    inline fun <T : D> toFlowableOfType(eventType: Class<T>, backpressureStrategy: BackpressureStrategy = BackpressureStrategy.BUFFER) : Flowable<T>
        = toFlowable(backpressureStrategy).ofType(eventType)
}


//region PublishBus
    /**
     * Create an [RxPublishBus]
     *
     * @return new [RxPublishBus]
     */
    inline fun rxPublishBus(): RxPublishBus {
        return RxPublishBus(
            PublishSubject.create<Any>(),
            Any::class.java
        )
    }

    /**
     * Create an [RxTypedPublishBus]
     *
     * @param T type of items to be sent through the bus
     *
     * @return new [RxTypedPublishBus] of specified type T
     */
    inline fun <reified T : Any> rxTypedPublishBus(): RxTypedPublishBus<T> {
        return RxTypedPublishBus<T>(
            PublishSubject.create<T>(),
            T::class.java
        )
    }
//endregion


//region BehaviorBus
    /**
     * Create an [RxBehaviorBus] with an optional default item
     *
     * @param defaultValue the item that will be emitted first to any Observer as long as no items have been sent through the bus yet (**none by default**)
     *
     * @return new [RxBehaviorBus] with specified default item
     */
    inline fun rxBehaviorBus(defaultValue: Any? = null): RxBehaviorBus {
        return RxBehaviorBus(
            if (defaultValue != null)
                BehaviorSubject.createDefault<Any>(defaultValue)
            else
                BehaviorSubject.create<Any>(),
            Any::class.java
        )
    }

    /**
     * Create an [RxTypedBehaviorBus] with an optional default item
     *
     * @param T type of items to be sent through the bus
     * @param defaultValue the item that will be emitted first to any Observer as long as no items have been sent through the bus yet (**none by default**)
     *
     * @return new [RxTypedBehaviorBus] of specified type T with specified default item
     */
    inline fun <reified T : Any> rxTypedBehaviorBus(defaultValue: T? = null): RxTypedBehaviorBus<T> {
        return RxTypedBehaviorBus<T>(
            if (defaultValue != null)
                BehaviorSubject.createDefault<T>(defaultValue)
            else
                BehaviorSubject.create<T>(),
            T::class.java
        )
    }
//endregion


//region ReplayBus

    /**
     * Create an unbounded [RxReplayBus] with an optional initial buffer capacity
     *
     * @param capacityHint the initial buffer capacity of underlying ReplaySubject (**not set by default**)
     *
     * @return new [RxReplayBus] with specified initial buffer capacity
     */
    inline fun rxReplayBus(capacityHint: Int? = null): RxReplayBus {
        return RxReplayBus(
            if (capacityHint != null)
                ReplaySubject.create<Any>(capacityHint)
            else
                ReplaySubject.create<Any>(),
            Any::class.java
        )
    }

    /**
     * Create a size-bounded [RxReplayBus]
     *
     * @param maxSize the maximum number of buffered items
     *
     * @return new [RxReplayBus] with specified maximum number of buffered items
     */
    inline fun rxReplayBusWithSize(maxSize: Int): RxReplayBus {
        return RxReplayBus(
            ReplaySubject.createWithSize<Any>(maxSize),
            Any::class.java
        )
    }

    /**
     * Create a time-bounded [RxReplayBus]
     *
     * @param maxAge the maximum age of the contained items
     * @param unit the unit of time
     * @param scheduler the [Scheduler] that provides the current time
     *
     * @return new [RxReplayBus] with specified time bounds
     */
    inline fun rxReplayBusWithTime(maxAge: Long, unit: TimeUnit, scheduler: Scheduler): RxReplayBus {
        return RxReplayBus(
            ReplaySubject.createWithTime<Any>(maxAge, unit, scheduler),
            Any::class.java
        )
    }

    /**
     * Create a time- and size-bounded [RxReplayBus]
     *
     * @param maxAge the maximum age of the contained items
     * @param unit the unit of time
     * @param scheduler the [Scheduler] that provides the current time
     * @param maxSize the maximum number of buffered items
     *
     * @return new [RxReplayBus] with specified time and size bounds
     */
    inline fun rxReplayBusWithTimeAndSize(maxAge: Long, unit: TimeUnit, scheduler: Scheduler, maxSize: Int): RxReplayBus {
        return RxReplayBus(
            ReplaySubject.createWithTimeAndSize<Any>(maxAge, unit, scheduler, maxSize),
            Any::class.java
        )
    }

    /**
     * Create an unbounded [RxTypedReplayBus] with an optional initial buffer capacity
     *
     * @param T type of items to be sent through the bus
     * @param capacityHint the initial buffer capacity of underlying ReplaySubject (**not set by default**)
     *
     * @return new [RxTypedReplayBus] of specified type T with specified initial buffer capacity
     */
    inline fun <reified T : Any> rxTypedReplayBus(capacityHint: Int? = null): RxTypedReplayBus<T> {
        return RxTypedReplayBus<T>(
            if (capacityHint != null)
                ReplaySubject.create<T>(capacityHint)
            else
                ReplaySubject.create<T>(),
            T::class.java
        )
    }

    /**
     * Create a size-bounded [RxTypedReplayBus]
     *
     * @param T type of items to be sent through the bus
     * @param maxSize the maximum number of buffered items
     *
     * @return new [RxTypedReplayBus] of specified type T with specified maximum number of buffered items
     */
    inline fun <reified T : Any> rxTypedReplayBusWithSize(maxSize: Int): RxTypedReplayBus<T> {
        return RxTypedReplayBus<T>(
            ReplaySubject.createWithSize<T>(maxSize),
            T::class.java
        )
    }

    /**
     * Create a time-bounded [RxTypedReplayBus]
     *
     * @param T type of items to be sent through the bus
     * @param maxAge the maximum age of the contained items
     * @param unit the unit of time
     * @param scheduler the [Scheduler] that provides the current time
     *
     * @return new [RxTypedReplayBus] of specified type T with specified time bounds
     */
    inline fun <reified T : Any> rxTypedReplayBusWithTime(maxAge: Long, unit: TimeUnit, scheduler: Scheduler): RxTypedReplayBus<T> {
        return RxTypedReplayBus<T>(
            ReplaySubject.createWithTime<T>(maxAge, unit, scheduler),
            T::class.java
        )
    }

    /**
     * Create a time- and size-bounded [RxTypedReplayBus]
     *
     * @param T type of items to be sent through the bus
     * @param maxAge the maximum age of the contained items
     * @param unit the unit of time
     * @param scheduler the [Scheduler] that provides the current time
     * @param maxSize the maximum number of buffered items
     *
     * @return new [RxTypedReplayBus] of specified type T with specified time and size bounds
     */
    inline fun <reified T : Any> rxTypedReplayBusWithTimeAndSize(maxAge: Long, unit: TimeUnit, scheduler: Scheduler, maxSize: Int): RxTypedReplayBus<T> {
        return RxTypedReplayBus<T>(
            ReplaySubject.createWithTimeAndSize<T>(maxAge, unit, scheduler, maxSize),
            T::class.java
        )
    }
//endregion
