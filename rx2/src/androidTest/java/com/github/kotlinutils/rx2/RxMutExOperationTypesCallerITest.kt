//package com.github.kotlinutils.rx2
//
//import android.os.HandlerThread
//import android.support.test.runner.AndroidJUnit4
//import com.bornest.apps.onefamily.util.random.extensions.randomBoolean
//import com.bornest.apps.onefamily.util.random.extensions.sleepRandomTime
//import com.github.kotlinutils.concurrent.android.extensions.quitSafelyIfPossible
//import com.github.kotlinutils.concurrent.java.extensions.curThreadNameInBr
//import com.github.kotlinutils.rx2.RxMutExOperationTypesCaller
//import com.github.kotlinutils.rx2.extensions.scheduler
//import com.github.unitimber.core.extensions.v
//import com.github.unitimber.core.loggable.Loggable
//import com.github.unitimber.core.loggable.extensions.d
//import io.reactivex.Observable
//import io.reactivex.Scheduler
//import io.reactivex.Single
//import org.junit.Test
//import org.junit.runner.RunWith
//import java.util.concurrent.CountDownLatch
//import java.util.concurrent.TimeUnit
//
///**
// * Created by nbv54 on 20-Apr-17.
// */
//@RunWith(AndroidJUnit4::class)
//class RxMutExOperationTypesCallerITest : Loggable {
//    override var loggingEnabled = true
//    override val logTag = "OpCallerITest"
//
//    @Test
//    fun simpleHandlerThreadCall() {
//        val handlerThread = HandlerThread("testHandlerThread").also { it.start() }
//        val testObj = TestClassRegular("handlerThreadOpCaller", 20, TimeUnit.SECONDS)
//        val latch = CountDownLatch(4)
//
//        val aOneD = testObj.aOne(handlerThread.scheduler)
//            .take(3)
//            .doOnNext {
//                d { "$curThreadNameInBr Observable Received: $it" }
//                latch.countDown()
//            }
//            .doFinally {
//                latch.countDown()
//            }
//            .test()
//
//        assert(latch.await(10, TimeUnit.SECONDS))
//        aOneD.dispose()
//        handlerThread.quitSafelyIfPossible()
//    }
//
//    //region inner Test Classes
//        abstract class AbstractTestClass(opCallerName: String, keepAliveTime: Long, timeUnit: TimeUnit) {
//            val opCaller = RxMutExOperationTypesCaller(opCallerName, keepAliveTime, timeUnit)
//
//            abstract protected fun waitShort()
//            abstract protected fun waitLong()
//
//            fun aOne(scheduler: Scheduler): Observable<String> {
//                val opName = "aOne"
//                val disposed = VarWithLock(false)
//                return opCaller.callObservable(TestOperationType.A, opName,
//                    Observable.create<String> {
//                        emitter ->
//                        v{"$curThreadNameInBr $opName: performing initial setup"}
//                        waitShort() //imitate initial setup
//
//                        v{"$curThreadNameInBr $opName: sending initial value: 0"}
//                        emitter.onNext("$opName: 0 (initial value)") //send initial value
//
//                        var i = 0
//                        while ( !(disposed{v}) )
//                        {
//                            waitLong() //imitate work/delay
////                            v{"$curThreadNameInBr $opName: sending next value: $i"}
//                            emitter.onNext("$curThreadNameInBr $opName: $i") //send next value
//                            i++
//                        }
//                    }
//                        .doFinally {
//                            disposed {
//                                v = true
//                            }
//                        }
//                        .subscribeOn(scheduler)
//                )
//
//            }
//
//            fun aTwo(scheduler: Scheduler): Single<String> {
//                val opName = "aTwo"
//                return opCaller.callSingle(TestOperationType.A, opName,
//                    Single.create<String> {
//                        emitter ->
//                        v{"$curThreadNameInBr $opName: doing work"}
//
//                        waitLong() //imitate work/delay
//                        if (randomBoolean(0.9)){
//                            v{"$curThreadNameInBr $opName: sending onSuccess"}
//                            emitter.onSuccess("$opName: ")
//                        } else {
//                            v{"$curThreadNameInBr $opName: sending onError"}
//                            emitter.onError(Throwable("$opName: "))
//                        }
//                    }
//                        .subscribeOn(scheduler)
//                )
//            }
//
//            fun bOne(scheduler: Scheduler): Observable<String> {
//                val opName = "bOne"
//                val disposed = VarWithLock(false)
//                return opCaller.callObservable(TestOperationType.B, opName,
//                    Observable.create<String> {
//                        emitter ->
//                        v{"$curThreadNameInBr $opName: performing initial setup"}
//                        waitShort() //imitate initial setup
//
//                        v{"$curThreadNameInBr $opName: sending initial value: 0"}
//                        emitter.onNext("$opName: 0 (initial value)") //send initial value
//
//                        var i = 0
//                        while ( !(disposed{v}) )
//                        {
//                            waitLong() //imitate work/delay
////                            v{"$curThreadNameInBr $opName: sending next value: $i"}
//                            emitter.onNext("$curThreadNameInBr $opName: $i") //send next value
//                            i++
//                        }
//                    }
//                        .doFinally {
//                            disposed {
//                                v = true
//                            }
//                        }
//                        .subscribeOn(scheduler)
//                )
//            }
//
//            fun bTwo(scheduler: Scheduler): Single<String> {
//                val opName = "bTwo"
//                return opCaller.callSingle(TestOperationType.B, opName,
//                    Single.create<String> {
//                        emitter ->
//                        v{"$curThreadNameInBr $opName: doing work"}
//                        waitLong() //imitate work/delay
//                        if (randomBoolean(0.9)){
//                            v{"$curThreadNameInBr $opName: sending onSuccess"}
//                            emitter.onSuccess("$opName: ")
//                        } else {
//                            v{"$curThreadNameInBr $opName: sending onError"}
//                            emitter.onError(Throwable("$opName: "))
//                        }
//                    }
//                        .subscribeOn(scheduler)
//                )
//            }
//        }
//
//        class TestClassRandom(opCallerName: String, keepAliveTime: Long, timeUnit: TimeUnit) : AbstractTestClass(opCallerName, keepAliveTime, timeUnit){
//            override fun waitShort() {
//                sleepRandomTime(10, 300)
//            }
//
//            override fun waitLong() {
//                sleepRandomTime(10, 1000)
//            }
//        }
//
//        class TestClassRegular(opCallerName: String, keepAliveTime: Long, timeUnit: TimeUnit) : AbstractTestClass(opCallerName, keepAliveTime, timeUnit){
//            override fun waitShort() {
//                Thread.sleep(150)
//            }
//
//            override fun waitLong() {
//                Thread.sleep(500)
//            }
//        }
//
//        class TestOperationType(name: String) : RxMutExOperationTypesCaller.OperationType(name)
//        {
//            companion object {
//                val A = TestOperationType("A")
//                val B = TestOperationType("B")
//            }
//        }
//    //endregion
//}