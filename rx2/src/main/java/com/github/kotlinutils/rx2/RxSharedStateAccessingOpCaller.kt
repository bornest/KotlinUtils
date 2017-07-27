@file:Suppress("NOTHING_TO_INLINE")

package com.github.kotlinutils.rx2

import com.github.kotlinutils.concurrent.java.extensions.curThreadNameInBr
import com.github.kotlinutils.concurrent.java.extensions.singleThreadExecutorWithAutoShutdown
import com.github.kotlinutils.rx2.bus.rxTypedBehaviorBus
import com.github.kotlinutils.rx2.bus.rxTypedPublishBus
import com.github.kotlinutils.rx2.extensions.ForkJoinCommonPoolScheduler
import com.github.kotlinutils.rx2.extensions.createDisposableSubscriber
import com.github.kotlinutils.rx2.extensions.doOnFirst
import com.github.kotlinutils.rx2.extensions.doOnNextIf
import com.github.kotlinutils.rx2.extensions.request
import com.github.kotlinutils.rx2.extensions.scheduler
import com.github.kotlinutils.rx2.extensions.zipWith
import com.github.unitimber.core.loggable.Loggable
import com.github.unitimber.core.loggable.extensions.d
import com.github.unitimber.core.loggable.extensions.i
import com.github.unitimber.core.loggable.extensions.v
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

typealias OperationKey = String

/**
 * *(internal)* [RxSharedStateAccessingOpCaller] request containing information about the operation and action to be performed with it:
 * - if Boolean is true - register
 * - if Boolean is false - unregister
 */
typealias KeyOpRegUnregRequest = Pair<RxSharedStateAccessingOpCaller.OperationInfo, Boolean>

/**
 * Lock-free race-condition-preventing calling queue for operations
 * that work with different parts of shared state (or with the whole shared state)
 * on different threads.
 *
 * Uses String keys to identify the exact part of shared state that going to be accessed.
 */
class RxSharedStateAccessingOpCaller(
    override val logTag: String? = null,
    val keepAliveTime: Long = 1,
    val timeUnit: TimeUnit = TimeUnit.MINUTES,
    override var loggingEnabled: Boolean = true
) : Loggable {
        
    companion object {
        const val COMMON_OP_KEY: OperationKey = "COMMON_OP_KEY"
    }
    
    //region properties
        /**
         * ("in") Bus for processing the queue of 'call*()' calls (i.e. the que of operations)
         */
        private val opInfoQueueBus = rxTypedPublishBus<OperationInfo>()
        /**
         * ("out") Bus for notifying operations that it's their turn to execute
         */
        private val opIdBus = rxTypedPublishBus<String>()
        /**
         * (internal) Bus for notifying queue processor about the keys of currently ongoing operations
         */
        private val ongoingOpKeysBus = rxTypedBehaviorBus<Set<String>>(hashSetOf<String>())
        
        private val regUnregReqBus = rxTypedPublishBus<KeyOpRegUnregRequest>()
    
        private var _ongoingOpKeys = hashSetOf<String>()
        
        private val compositeDisposable = CompositeDisposable()
    
        lateinit var _opInfoQueueProcessingExecutor: ExecutorService //for testing
        private val opInfoQueueProcessingScheduler : Scheduler by lazy {
            singleThreadExecutorWithAutoShutdown(
                "$logTag-opInfoQ",
                keepAliveTime,
                timeUnit
            ).also { _opInfoQueueProcessingExecutor = it }.scheduler
        }
        lateinit var _regUnregReqProcessingExecutor: ExecutorService //for testing
        private val regUnregReqProcessingScheduler : Scheduler by lazy {
            singleThreadExecutorWithAutoShutdown(
                "$logTag-regUnreg",
                keepAliveTime,
                timeUnit
            ).also { _regUnregReqProcessingExecutor = it }.scheduler
        }
        private val callProcessingScheduler : Scheduler by lazy { ForkJoinCommonPoolScheduler() }
    
        private val opCounter = AtomicLong(0L)
    //endregion
    
    init {
        start()
    }
    
    //region Lifecycle
        /**
         * Start processing operation calls one by one
         */
        private inline fun start() {
            if (compositeDisposable.size() == 0) {
                compositeDisposable.addAll(
                    //1. Get opInfo item from opInfoQueueBus
                    opInfoQueueBus.toFlowable()
                        .observeOn(opInfoQueueProcessingScheduler)
                        .concatMap {
                            opInfo ->
                            //2. Start waiting till ongoingOpKeysBus emits ongoingOpKeys suitable for performing this operation
                            v { "$curThreadNameInBr Dequeued '$opInfo', waiting for suitable ongoingOpKeys" }
                            ongoingOpKeysBus.toFlowable()
                                .observeOn(opInfoQueueProcessingScheduler)
                                .filter {
                                    ongoingOpKeys ->
                                    when(opInfo.key) {
                                        COMMON_OP_KEY -> {
                                            ongoingOpKeys.isEmpty()
                                        }
                                        else -> {
                                            !ongoingOpKeys.contains(opInfo.key) && !ongoingOpKeys.contains(COMMON_OP_KEY)
                                        }
                                    }
                                }
                                .take(1)
                                .doOnNextIf(loggingEnabled) {
                                    curOpType ->
                                    v { "$curThreadNameInBr Passing '$opInfo' down 'cos ongoingOpKeys = $curOpType" }
                                }
                                .concatMap {
                                    //5. Start waiting till operation is registered
                                    opIdBus.toFlowable()
                                        .doOnNextIf(loggingEnabled) {
                                            v { "$curThreadNameInBr opInfoQueueBus pre-filter for '$opInfo' received $it" }
                                        }
                                        .observeOn(opInfoQueueProcessingScheduler)
                                        .filter {
                                            opId ->
                                            (opId == opInfo.id).also {
                                                v { "$curThreadNameInBr opInfoQueueBus filter for '$opInfo' received $opId" }
                                            }
                                        }
                                        .take(1)
                                        .also {
                                            v { "$curThreadNameInBr opInfoQueueBus STEP 5 OBSERVABLE CREATED for '$opInfo'" }
                                        }
                                        //3. Send regReq to regUnregReqBus
                                        .zipWith(
                                            Flowable.fromCallable {
                                                v { "$curThreadNameInBr Sending RegReq for '$opInfo' to regUnregReqBus" }
                                                regUnregReqBus.send(opInfo to true) // see step 4 below
                                            }
                                        )
                                }
                                .map {
                                    (opId, _) ->
                                    v { "$curThreadNameInBr Got opId = $opId => '$opInfo' was registered => requesting another operation" }
                                }
                        }
                        .subscribeWith(
                            createDisposableSubscriber<Unit>(
                                onStart = {
                                    request(1)
                                },
                                onNext = {
                                    //6. Get next opInfo item from opInfoQueueBus
                                    request(1)
                                },
                                onComplete = {},
                                onError = {}
                            )
                        ),
                    regUnregReqBus.toFlowable()
                        .observeOn(regUnregReqProcessingScheduler)
                        .subscribeWith(
                            createDisposableSubscriber<KeyOpRegUnregRequest>(
                                onStart = {
                                    request(1)
                                },
                                onNext = {
                                    (opInfo, register) ->
                                    if (register) {
                                        //4. Register operation (see above for steps 1-3 and 5)
                                        v { "$curThreadNameInBr registerOperation() for '$opInfo'" }
                                        registerOperation(opInfo)
                                        opIdBus.send(opInfo.id).also {
                                            v { "$curThreadNameInBr regUnregReqBus Observer sent opId for '$opInfo' to opIdBus" }
                                        }
                                    } else {
                                        v { "$curThreadNameInBr unregisterOperation() for '$opInfo' " }
                                        unregisterOperation(opInfo)
                                    }
                                    request(1)
                                },
                                onComplete = {},
                                onError = {}
                            )
                        )
                )
            }
        }
        
        /**
         * Stop processing operation calls
         */
        private inline fun stop() {
            compositeDisposable.clear()
        }
    //endregion
    
        
    //region Public Methods
        /**
         * Add operation [Single] to the calling queue
         *
         * Operation will be added to the queue when someone subscribes to returned Single
         *
         * @param operation operation [Single] that's added to the queue
         * @param subscribeOnScheduler [Scheduler] to perform the operation on
         * @param operationKey key that corresponds to the part of shared state that's going to be accessed
         * @param operationName name of the operation that's added to the queue (*empty String by default*)
         *
         * @return original operation [Single] wrapped into a calling queue [Single]
         */
        fun <T : Any> callSingle(operation: Single<T>,
                                 subscribeOnScheduler: Scheduler,
                                 operationKey: OperationKey,
                                 operationName: String = ""
        ): Single<T> {
            return prepareCall(operationKey, operationName)
                .firstOrError()
                //4) Perform operation
                .flatMap {
                    opInfo ->
                    d { "$curThreadNameInBr Calling '$opInfo'" }
                    operation
                        //5) Unregister operation
                        .doFinally {
                            d { "$curThreadNameInBr <doFinally> Sending UnRegReq for '$opInfo' to regUnregReqBus" }
                            regUnregReqBus.send(opInfo to false)
                        }
                        .subscribeOn(subscribeOnScheduler)
                }
        }
        
        /**
         * Add operation [Observable] to the calling queue
         *
         * Operation will be added to the queue when someone subscribes to returned Observable
         *
         * @param operation operation [Observable] that's added to the queue
         * @param subscribeOnScheduler [Scheduler] to perform the operation on
         * @param operationKey key that corresponds to the part of shared state that's going to be accessed
         * @param operationName name of the operation that's added to the queue (*empty String by default*)
         *
         * @return original operation [Observable] wrapped into a calling queue [Observable]
         */
        fun <T : Any> callObservable(operation: Observable<T>,
                                     subscribeOnScheduler: Scheduler,
                                     operationKey: OperationKey,
                                     operationName: String = ""
        ): Observable<T> {
            return prepareCall(operationKey, operationName)
                //4) Perform operation
                .concatMap {
                    opInfo ->
                    val unregistered = AtomicBoolean(false)
                    d { "$curThreadNameInBr Calling '$opInfo'" }
                    operation
                        //5) Unregister operation
                        .doOnFirst {
                            if (unregistered.compareAndSet(false, true)) {
                                d { "$curThreadNameInBr <doOnFirst> Sending UnRegReq for '$opInfo' to regUnregReqBus" }
                                regUnregReqBus.send(opInfo to false)
                            }
                        }
                        .doFinally {
                            if (unregistered.compareAndSet(false, true)) {
                                d { "$curThreadNameInBr <doFinally> Sending UnRegReq for '$opInfo' to regUnregReqBus" }
                                regUnregReqBus.send(opInfo to false)
                            }
                        }
                        .subscribeOn(subscribeOnScheduler)
                }
        }
        
        /**
         * Add operation [Flowable] to the calling queue
         *
         * Operation will be added to the queue when someone subscribes to returned Flowable
         *
         * @param operation operation [Flowable] that's added to the queue
         * @param subscribeOnScheduler [Scheduler] to perform the operation on
         * @param operationKey key that corresponds to the part of shared state that's going to be accessed
         * @param operationName name of the operation that's added to the queue (*empty String by default*)
         *
         * @return original operation [Flowable] wrapped into a calling queue [Flowable]
         */
        fun <T : Any> callFlowable(operation: Flowable<T>,
                                   subscribeOnScheduler: Scheduler,
                                   operationKey: OperationKey,
                                   operationName: String = ""
        ): Flowable<T> {
            return prepareCall(operationKey, operationName)
                .toFlowable(BackpressureStrategy.BUFFER)
                //4) Perform operation
                .concatMap {
                    opInfo ->
                    val unregistered = AtomicBoolean(false)
                    d { "$curThreadNameInBr Calling '$opInfo'" }
                    operation
                        //5) Unregister operation
                        .doOnFirst {
                            if (unregistered.compareAndSet(false, true)) {
                                d { "$curThreadNameInBr <doOnFirst> Sending UnRegReq for '$opInfo' to regUnregReqBus" }
                                regUnregReqBus.send(opInfo to false)
                            }
                        }
                        .doFinally {
                            if (unregistered.compareAndSet(false, true)) {
                                d { "$curThreadNameInBr <doFinally> Sending UnRegReq for '$opInfo' to regUnregReqBus" }
                                regUnregReqBus.send(opInfo to false)
                            }
                        }
                        .subscribeOn(subscribeOnScheduler)
                }
        }
    //endregion
    
    
    //region private methods
        private inline fun prepareCall(operationKey: OperationKey,
                                       operationName: String = ""
        ) : Observable<OperationInfo> {
            //1) Generate OperationInfo
            return Observable.just (
                OperationInfo(
                    opCounter.incrementAndGet().toString(),
                    operationName,
                    operationKey
                )
            )
                .subscribeOn(callProcessingScheduler)
                .concatMap {
                    opInfo ->
                    //3) Start waiting for our turn
                    opIdBus.toObservable()
                        .observeOn(callProcessingScheduler)
                        .filter {
                            opId ->
                            (opId == opInfo.id).also {
                                v { "$curThreadNameInBr _prepareCall filter for '$opInfo' received $opId" }
                            }
                        }
                        .take(1)
                        .zipWith(
                            //2) Get in queue
                            Observable.fromCallable {
                                opInfo.also {
                                    d { "$curThreadNameInBr Enqueuing '$opInfo' " }
                                    opInfoQueueBus.send(it)
                                }
                            }
                        ) { _, _ -> opInfo }
                }
                .doOnNextIf(loggingEnabled) {
                    opInfo ->
                    v { "$curThreadNameInBr _prepareCall finished for '$opInfo'" }
                }
        }
    
        private fun registerOperation(opInfo: OperationInfo) {
            val opKey = opInfo.key
            
            when (opKey) {
                COMMON_OP_KEY -> {
                    if (_ongoingOpKeys.isEmpty()) {
                        _ongoingOpKeys.add(opKey)
                    } else {
                        throw RuntimeException("$curThreadNameInBr registerOperation($opInfo) error: " +
                            "Can't register operation with key '$opKey', 'cos _ongoingOpKeys is not empty - $_ongoingOpKeys ")
                    }
                }
                else -> {
                    if (!_ongoingOpKeys.contains(opKey) && !_ongoingOpKeys.contains(COMMON_OP_KEY)) {
                        _ongoingOpKeys.add(opKey)
                    } else {
                        throw RuntimeException("$curThreadNameInBr registerOperation($opInfo) error: " +
                            "Can't register operation with key '$opKey', 'cos _ongoingOpKeys already contains required key(-s) - $_ongoingOpKeys")
                    }
                }
            }
            
            ongoingOpKeysBus.send(_ongoingOpKeys)
            i { "$curThreadNameInBr Registered '$opInfo' => _ongoingOpKeys=$_ongoingOpKeys"}
        }
        
        private fun unregisterOperation(opInfo: OperationInfo) {
            val opKey = opInfo.key
    
            if (_ongoingOpKeys.contains(opKey)){
                _ongoingOpKeys.remove(opKey)
            } else {
                throw RuntimeException("$curThreadNameInBr unregisterOperation($opInfo) error: " +
                    "Can't unregister operation with key '$opKey', 'cos it's not in _ongoingOpKeys - $_ongoingOpKeys")
            }
    
            ongoingOpKeysBus.send(_ongoingOpKeys)
            i { "$curThreadNameInBr Unregistered '$opInfo' => _ongoingOpKeys=$_ongoingOpKeys"}
        }
    //endregion
    
    class OperationInfo(
        @JvmField val id: String,
        @JvmField val name: String,
        @JvmField val key: OperationKey
    ) {
        inline operator fun component1() = id
        inline operator fun component2() = name
        inline operator fun component3() = key
    
        override fun toString(): String
            = "<$key>$name-$id"
    }
}

/**
 * Add this operation [Single] to specified [RxSharedStateAccessingOpCaller] calling queue
 *
 * Operation will be added to the queue when someone subscribes to returned Single
 *
 * @param opCaller [RxSharedStateAccessingOpCaller] calling queue
 * @param subscribeOnScheduler [Scheduler] to perform the operation on
 * @param operationKey key that corresponds to the part of shared state that's going to be accessed
 * @param operationName name of the operation that's added to the queue (*empty String by default*)
 *
 * @return original operation [Single] wrapped into a calling queue [Single]
 */
inline fun <T : Any> Single<T>.withOpCaller(opCaller: RxSharedStateAccessingOpCaller,
                                            subscribeOnScheduler: Scheduler,
                                            operationKey: OperationKey,
                                            operationName: String = ""
): Single<T> {
    return opCaller.callSingle(this, subscribeOnScheduler, operationKey, operationName)
}

/**
 * Add this operation [Observable] to specified [RxSharedStateAccessingOpCaller] calling queue
 *
 * Operation will be added to the queue when someone subscribes to returned Observable
 *
 * @param opCaller [RxSharedStateAccessingOpCaller] calling queue
 * @param subscribeOnScheduler [Scheduler] to perform the operation on
 * @param operationKey key that corresponds to the part of shared state that's going to be accessed
 * @param operationName name of the operation that's added to the queue (*empty String by default*)
 *
 * @return original operation [Observable] wrapped into a calling queue [Observable]
 */
inline fun <T : Any> Observable<T>.withOpCaller(opCaller: RxSharedStateAccessingOpCaller,
                                                subscribeOnScheduler: Scheduler,
                                                operationKey: OperationKey,
                                                operationName: String = ""
): Observable<T> {
    return opCaller.callObservable(this, subscribeOnScheduler, operationKey, operationName)
}

/**
 * Add this operation [Flowable] to specified [RxSharedStateAccessingOpCaller] calling queue
 *
 * Operation will be added to the queue when someone subscribes to returned Flowable
 *
 * @param opCaller [RxSharedStateAccessingOpCaller] calling queue
 * @param subscribeOnScheduler [Scheduler] to perform the operation on
 * @param operationKey key that corresponds to the part of shared state that's going to be accessed
 * @param operationName name of the operation that's added to the queue (*empty String by default*)
 *
 * @return original operation [Flowable] wrapped into a calling queue [Flowable]
 */
inline fun <T : Any> Flowable<T>.withOpCaller(opCaller: RxSharedStateAccessingOpCaller,
                                              subscribeOnScheduler: Scheduler,
                                              operationKey: OperationKey,
                                              operationName: String = ""
): Flowable<T> {
    return opCaller.callFlowable(this, subscribeOnScheduler, operationKey, operationName)
}




























































