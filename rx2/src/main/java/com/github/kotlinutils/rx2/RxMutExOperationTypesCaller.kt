@file:Suppress("NOTHING_TO_INLINE")

package com.github.kotlinutils.rx2

import com.github.kotlinutils.concurrent.java.extensions.curThreadNameInBr
import com.github.kotlinutils.concurrent.java.extensions.singleThreadExecutorWithAutoShutdown
import com.github.kotlinutils.core.extensions.classSimpleName
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
import com.github.unitimber.core.loggable.extensions.w
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


/**
 * *(internal)* Object that describe the state of [RxMutExOperationTypesCaller]:
 * - type of operations being performed ([RxMutExOperationTypesCaller.OperationType])
 * - number of ongoing operations
 */
typealias OpTypeState = Pair<RxMutExOperationTypesCaller.OperationType, Int>
/**
 * *(internal)* [RxMutExOperationTypesCaller] request containing information about the operation and action to be performed with it:
 * - if Boolean is true - register
 * - if Boolean is false - unregister
 */
typealias TypedOpRegUnregRequest = Pair<RxMutExOperationTypesCaller.OperationInfo, Boolean>

/**
 * Lock-free race-condition-preventing calling queue
 * for operations of mutually exclusive types
 * that work with shared state
 * on different threads.
 */
class RxMutExOperationTypesCaller(
    override val logTag: String? = null,
    val keepAliveTime: Long = 1,
    val timeUnit: TimeUnit = TimeUnit.MINUTES,
    override var loggingEnabled: Boolean = true,
    @JvmField val testing: Boolean = false
) : Loggable {
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
         * (internal) Bus for notifying queue processor about the type of currently ongoing operations
         */
        private val curOpTypeBus = rxTypedBehaviorBus(OperationType.NOTHING)
        
        private val regUnregReqBus = rxTypedPublishBus<TypedOpRegUnregRequest>()
    
        private var _curOpTypeState: OpTypeState = OperationType.NOTHING to 0
        
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
                            //2. Start waiting till curOpTypeBus emits OperationType suitable for performing this operation
                            v { "$curThreadNameInBr Dequeued '${if (testing) {opInfo.name} else {opInfo.shortStr}}', waiting for suitable curOpType" }
                            curOpTypeBus.toFlowable()
                                .observeOn(opInfoQueueProcessingScheduler)
                                .filter { it == OperationType.NOTHING || it == opInfo.type }
                                .take(1)
                                .doOnNextIf(loggingEnabled) {
                                    curOpType ->
                                    v { "$curThreadNameInBr Passing '${if (testing) {opInfo.name} else {opInfo.shortStr}}' down 'cos curOpType = $curOpType" }
                                }
                                .concatMap {
                                    //5. Start waiting till operation is registered
                                    opIdBus.toFlowable()
                                        .doOnNextIf(loggingEnabled) {
                                            v { "$curThreadNameInBr opInfoQueueBus pre-filter for '${if (testing) {opInfo.name} else {opInfo.shortStr}}' received $it" }
                                        }
                                        .observeOn(opInfoQueueProcessingScheduler)
                                        .filter {
                                            opId ->
                                            (opId == opInfo.id).also {
                                                v { "$curThreadNameInBr opInfoQueueBus filter for '${if (testing) {opInfo.name} else {opInfo.shortStr}}' received $opId" }
                                            }
                                        }
                                        .take(1)
                                        .also {
                                            v { "$curThreadNameInBr opInfoQueueBus STEP 5 OBSERVABLE CREATED for '${if (testing) {opInfo.name} else {opInfo.shortStr}}'" }
                                        }
                                        //3. Send regReq to regUnregReqBus
                                        .zipWith(
                                            Flowable.fromCallable {
                                                v { "$curThreadNameInBr Sending RegReq for '${if (testing) {opInfo.name} else {opInfo.shortStr}}' to regUnregReqBus" }
                                                regUnregReqBus.send(opInfo to true) // see step 4 below
                                            }
                                        )
                                }
                                .map {
                                    (opId, _) ->
                                    v { "$curThreadNameInBr Got opId = $opId => '${if (testing) {opInfo.name} else {opInfo.shortStr}}' was registered => requesting another operation" }
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
                            createDisposableSubscriber<TypedOpRegUnregRequest>(
                                onStart = {
                                    request(1)
                                },
                                onNext = {
                                    (opInfo, register) ->
                                    if (register) {
                                        //4. Register operation (see above for steps 1-3 and 5)
                                        v { "$curThreadNameInBr registerOperation() for '${if (testing) {opInfo.name} else {opInfo.shortStr}}' " }
                                        registerOperation(opInfo)
                                        opIdBus.send(opInfo.id).also {
                                            v { "$curThreadNameInBr regUnregReqBus Observer sent opId for '${if (testing) {opInfo.name} else {opInfo.shortStr}}' to opIdBus" }
                                        }
                                    } else {
                                        v { "$curThreadNameInBr unregisterOperation() for '${if (testing) {opInfo.name} else {opInfo.shortStr}}' " }
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
         * @param operationType type of the operation that's added to the queue
         * @param operationName name of the operation that's added to the queue (*empty String by default*)
         * @param operation operation [Single] that's added to the queue
         *
         * @return original operation [Single] wrapped into a calling queue [Single]
         */
        fun <T : Any> callSingle(operationType: OperationType,
                                 operationName: String = "",
                                 operation: Single<T>
        ): Single<T> {
            return prepareCall(operationType, operationName)
                .firstOrError()
                //4) Perform operation
                .flatMap {
                    opInfo ->
                    d { "$curThreadNameInBr Calling '${ if (testing) {opInfo.name} else {opInfo.shortStr} }' " }
                    operation
                        //5) Unregister operation
                        .doFinally {
                            d { "$curThreadNameInBr <doFinally> Sending UnRegReq for '${if (testing) {opInfo.name} else {opInfo.shortStr}}' to regUnregReqBus" }
                            regUnregReqBus.send(opInfo to false)
                        }
                }
        }
    
        /**
         * Add operation [Observable] to the calling queue
         *
         * Operation will be added to the queue when someone subscribes to returned Observable
         *
         * @param operationType type of the operation that's added to the queue
         * @param operationName name of the operation that's added to the queue (*empty String by default*)
         * @param operation operation [Observable] that's added to the queue
         *
         * @return original operation [Observable] wrapped into a calling queue [Observable]
         */
        fun <T : Any> callObservable(operationType: OperationType,
                                     operationName: String = "",
                                     operation: Observable<T>
        ): Observable<T> {
            return prepareCall(operationType, operationName)
                //4) Perform operation
                .concatMap {
                    opInfo ->
                    val unregistered = AtomicBoolean(false)
                    d { "$curThreadNameInBr Calling '${ if (testing) {opInfo.name} else {opInfo.shortStr} }' " }
                    operation
                        //5) Unregister operation
                        .doOnFirst {
                            if (unregistered.compareAndSet(false, true)){
                                d { "$curThreadNameInBr <doOnFirst> Sending UnRegReq for '${if (testing) {opInfo.name} else {opInfo.shortStr}}' to regUnregReqBus" }
                                regUnregReqBus.send(opInfo to false)
                            }
                        }
                        .doFinally {
                            if (unregistered.compareAndSet(false, true)){
                                d { "$curThreadNameInBr <doFinally> Sending UnRegReq for '${if (testing) {opInfo.name} else {opInfo.shortStr}}' to regUnregReqBus" }
                                regUnregReqBus.send(opInfo to false)
                            }
                        }
                }
        }
    
        /**
         * Add operation [Flowable] to the calling queue
         *
         * Operation will be added to the queue when someone subscribes to returned Flowable
         *
         * @param operationType type of the operation that's added to the queue
         * @param operationName name of the operation that's added to the queue (*empty String by default*)
         * @param operation operation [Flowable] that's added to the queue
         *
         * @return original operation [Flowable] wrapped into a calling queue [Flowable]
         */
        fun <T : Any> callFlowable(operationType: OperationType,
                                     operationName: String = "",
                                     operation: Flowable<T>
        ): Flowable<T> {
            return prepareCall(operationType, operationName)
                .toFlowable(BackpressureStrategy.BUFFER)
                //4) Perform operation
                .concatMap {
                    opInfo ->
                    val unregistered = AtomicBoolean(false)
                    d { "$curThreadNameInBr Calling '${ if (testing) {opInfo.name} else {opInfo.shortStr} }' " }
                    operation
                        //5) Unregister operation
                        .doOnFirst {
                            if (unregistered.compareAndSet(false, true)){
                                d { "$curThreadNameInBr <doOnFirst> Sending UnRegReq for '${if (testing) {opInfo.name} else {opInfo.shortStr}}' to regUnregReqBus" }
                                regUnregReqBus.send(opInfo to false)
                            }
                        }
                        .doFinally {
                            if (unregistered.compareAndSet(false, true)){
                                d { "$curThreadNameInBr <doFinally> Sending UnRegReq for '${if (testing) {opInfo.name} else {opInfo.shortStr}}' to regUnregReqBus" }
                                regUnregReqBus.send(opInfo to false)
                            }
                        }
                }
        }
    //endregion
    
    
    //region private methods
        private inline fun prepareCall(operationType: OperationType,
                                       operationName: String = ""
        ) : Observable<OperationInfo> {
            //1) Generate OperationInfo
            return Observable.just (
                OperationInfo(operationName, operationType,
                    if (testing) {
                        operationName
                    } else {
                        opCounter.incrementAndGet().toString()
                    }
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
                                w { "$curThreadNameInBr _prepareCall filter for '${if (testing) { opInfo.name } else { opInfo.shortStr }}' received $opId" }
                            }
                        }
                        .take(1)
                        .zipWith(
                            //2) Get in queue
                            Observable.fromCallable {
                                opInfo.also {
                                    d { "$curThreadNameInBr Enqueuing '${if (testing) { it.name } else { it.shortStr }}' " }
                                    opInfoQueueBus.send(it)
                                }
                            }
                        ) { _, _ -> opInfo }
                }
                .doOnNextIf(loggingEnabled) {
                    w { "$curThreadNameInBr _prepareCall finished for '${if (testing) { it.name } else { it.shortStr }}'" }
                }
        }
    
        private fun registerOperation(operationInfo: OperationInfo) {
            val opTypeToReg = operationInfo.type
            _curOpTypeState.let {
                (curOpType, counter) ->
                when (curOpType) {
                    OperationType.NOTHING -> {
                        _curOpTypeState = opTypeToReg to 1
                        curOpTypeBus.send(opTypeToReg)
                    }
                    else -> {
                        if (opTypeToReg == curOpType) {
                            _curOpTypeState = opTypeToReg to (counter + 1)
                        } else {
                            throw RuntimeException("$curThreadNameInBr registerOperation() for " +
                                "'${ if (testing) {operationInfo.name} else {operationInfo.shortStr} }' error: " +
                                "Can't register operation of type $opTypeToReg while curOpType is $curOpType!")
                        }
                    }
                }
            }
            i { "$curThreadNameInBr Registered '${ if (testing) {operationInfo.name} else {operationInfo.shortStr} }' => _curOpTypeState = $_curOpTypeState"}
        }
        
        private fun unregisterOperation(operationInfo: OperationInfo) {
            val opTypeToUnreg = operationInfo.type
            _curOpTypeState.let {
                (curOpType, counter) ->
                when (curOpType) {
                    OperationType.NOTHING -> {
                        throw RuntimeException("$curThreadNameInBr unregisterOperation() for " +
                            "'${ if (testing) {operationInfo.name} else {operationInfo.shortStr} }' error: " +
                            "Can't unregister operation of type $opTypeToUnreg - curOpType is already ${OperationType.NOTHING}!")
                    }
                    opTypeToUnreg -> {
                        when {
                            counter > 1 -> {
                                _curOpTypeState = opTypeToUnreg to (counter - 1)
                            }
                            counter == 1 -> {
                                _curOpTypeState = OperationType.NOTHING to 0
                                curOpTypeBus.send(OperationType.NOTHING)
                            }
                            else -> {
                                throw RuntimeException("$curThreadNameInBr unregisterOperation() for " +
                                    "'${ if (testing) {operationInfo.name} else {operationInfo.shortStr} }' error: " +
                                    "Can't unregister operation of type $opTypeToUnreg - counter is already < 1!")
                            }
                        }
                    }
                    else -> {
                        throw RuntimeException("unregisterOperation() for " +
                            "'${ if (testing) {operationInfo.name} else {operationInfo.shortStr} }' error: " +
                            "Can't unregister operation of type $opTypeToUnreg while curOpType is $curOpType!")
                    }
                }
                i { "$curThreadNameInBr Unregistered '${ if (testing) {operationInfo.name} else {operationInfo.shortStr} }' => _curOpTypeState = $_curOpTypeState" }
            }
        }
    //endregion
    
    
    //region inner classes
        open class OperationType protected constructor(
            @JvmField
            val name: String
        ) {
            companion object {
                @JvmField
                val NOTHING = OperationType("NOTHING")
            }
            
            override fun toString()
                = "${this.classSimpleName}.${this.name}"
        }
        
        class OperationInfo(
            @JvmField val name: String,
            @JvmField val type: OperationType,
            @JvmField val id: String
        ) {
            inline operator fun component1() = name
            inline operator fun component2() = type
            inline operator fun component3() = id
            inline val shortStr: String
                get() = "<${type.name}>$name-$id"
            
            override fun toString(): String
                = "OperationInfo{name: $name, type: $type, id: $id}"
        }
    //endregion
}































































