package com.bornest.apps.onefamily.util.rx2

import com.bornest.apps.onefamily.util.random.extensions.randomBoolean
import com.bornest.apps.onefamily.util.random.extensions.sleepRandomTime
import com.github.kotlinutils.concurrent.java.extensions.curThreadNameInBr
import com.github.kotlinutils.rx2.RxMutExOperationTypesCaller
import com.github.unitimber.core.JavaLogger
import com.github.unitimber.core.UniTimber
import com.github.unitimber.core.extensions.uniTimberPlantDebugTree
import com.github.unitimber.core.extensions.v
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertTrue

/**
 * Created by nbv54 on 12-Apr-17.
 */

class RxMutExOperationTypesCallerTest {

    @Before
    fun setup() {
        uniTimberPlantDebugTree { JavaLogger() }
    }

    @After
    fun tearDown() {
        UniTimber.uprootAll()
    }
        
    @Test
    fun regularDelayCalls() {
        val latchCount = 6
        val latchTime = 1L
        val timeUnit = TimeUnit.MINUTES
        
        val testObj = TestClassRegular("regularDelayOpCaller", latchTime, timeUnit)
        
        runTest(testObj, latchCount, latchTime, timeUnit) {
            latch ->
            val t1 = Thread {
                //1
                testObj.aOne(Schedulers.newThread())
                    .take(3)
                    .doFinally {
                        latch.countDown()
                    }
                    .test()
        
        
                Thread.sleep(600)
        
                //4
                testObj.bTwo(Schedulers.io())
                    .doFinally {
                        latch.countDown()
                    }
                    .test()
        
                Thread.sleep(300)
        
                //5
                testObj.aTwo(Schedulers.io())
                    .doFinally {
                        latch.countDown()
                    }
                    .test()
            }
    
            val t2 = Thread {
                Thread.sleep(200)
        
                //2
                testObj.bTwo(Schedulers.io())
                    .doFinally {
                        latch.countDown()
                    }
                    .test()
        
                Thread.sleep(200)
        
                //3
                testObj.aTwo(Schedulers.io())
                    .doFinally {
                        latch.countDown()
                    }
                    .test()
        
                Thread.sleep(400)
        
                //6
                testObj.bOne(Schedulers.newThread())
                    .take(3)
                    .doFinally {
                        latch.countDown()
                    }
                    .test()
            }
    
            t1.start()
            t2.start()
        }
    }
    
    
    @Test
    fun randomDelayCalls(){
        val latchCount = 6
        val latchTime = 1L
        val timeUnit = TimeUnit.MINUTES
    
        val testObj = TestClassRandom("randomDelayOpCaller", latchTime, timeUnit)
        
        runTest(testObj, latchCount, latchTime, timeUnit) {
            latch ->
            val t1 = Thread {
                //1
                testObj.aOne(Schedulers.newThread())
                    .take(3)
                    .doFinally {
                        latch.countDown()
                    }
                    .test()
        
                sleepRandomTime(10, 600)
        
                //4
                testObj.bTwo(Schedulers.io())
                    .doFinally {
                        latch.countDown()
                    }
                    .test()
        
                sleepRandomTime(10, 300)
        
                //5
                testObj.aTwo(Schedulers.io())
                    .doFinally {
                        latch.countDown()
                    }
                    .test()
            }
    
            val t2 = Thread {
                sleepRandomTime(10, 200)
        
                //2
                testObj.bTwo(Schedulers.io())
                    .doFinally {
                        latch.countDown()
                    }
                    .test()
        
                sleepRandomTime(10, 200)
        
                //3
                testObj.aTwo(Schedulers.io())
                    .doFinally {
                        latch.countDown()
                    }
                    .test()
        
                sleepRandomTime(10, 400)
        
                //6
                testObj.bOne(Schedulers.newThread())
                    .take(3)
                    .doFinally {
                        latch.countDown()
                    }
                    .test()
            }
    
            t1.start()
            t2.start()
        }
    }
    
//    @Ignore
    @Test
    fun zeroDelayCalls() {
        val latchCount = 6
        val latchTime = 1L
        val timeUnit = TimeUnit.MINUTES
        
        val testObj = TestClassRegular("zeroDelayOpCaller", latchTime, timeUnit)
        
        runTest(testObj, latchCount, latchTime, timeUnit) {
            latch ->
            val t1 = Thread {
                testObj.aOne(Schedulers.newThread())
                    .take(3)
                    .doFinally {
                        latch.countDown()
                    }
                    .test()
        
                testObj.bTwo(Schedulers.io())
                    .doFinally {
                        latch.countDown()
                    }
                    .test()
        
                testObj.aTwo(Schedulers.io())
                    .doFinally {
                        latch.countDown()
                    }
                    .test()
            }
    
            val t2 = Thread {
                testObj.bTwo(Schedulers.io())
                    .doFinally {
                        latch.countDown()
                    }
                    .test()
        
                testObj.aTwo(Schedulers.io())
                    .doFinally {
                        latch.countDown()
                    }
                    .test()
        
                testObj.bOne(Schedulers.newThread())
                    .take(3)
                    .doFinally {
                        latch.countDown()
                    }
                    .test()
            }
    
            t1.start()
            t2.start()
        }
    }
    
    private inline fun runTest(testObj: AbstractTestClass,
                               latchCount: Int,
                               latchTime: Long,
                               timeUnit: TimeUnit,
                               testBody: (CountDownLatch) -> Unit
    ) {
        val opInfoQBusExec = testObj.opCaller._opInfoQueueProcessingExecutor
        val regUnregBusExec = testObj.opCaller._regUnregReqProcessingExecutor
    
        val latch = CountDownLatch(latchCount)
        
        testBody(latch)
    
        val latchWasOnTime = latch.await(latchTime, timeUnit)
        opInfoQBusExec.shutdown()
        regUnregBusExec.shutdown()
    
        assertTrue { latchWasOnTime }
    }
        
    //region inner Test Classes
        abstract class AbstractTestClass(opCallerName: String, keepAliveTime: Long, timeUnit: TimeUnit) {
            val opCaller = RxMutExOperationTypesCaller(opCallerName, keepAliveTime, timeUnit)

            abstract protected fun waitShort()
            abstract protected fun waitLong()
        
            fun aOne(scheduler: Scheduler): Observable<String> {
                val opName = "aOne"
                val disposed = AtomicBoolean(false)
                return opCaller.callObservable(TestOperationType.A, opName,
                    Observable.create<String> {
                        emitter ->
                        v{"$curThreadNameInBr $opName: performing initial setup"}
                        waitShort() //imitate initial setup

                        v{"$curThreadNameInBr $opName: sending initial value: 0"}
                        emitter.onNext("$opName: 0 (initial value)") //send initial value

                        var i = 0
                        while ( !disposed.get() )
                        {
                            waitLong() //imitate work/delay
//                            v{"$curThreadNameInBr $opName: sending next value: $i"}
                            emitter.onNext("$curThreadNameInBr $opName: $i") //send next value
                            i++
                        }
                    }
                        .doFinally {
                            disposed.set(true)
                        }
                        .subscribeOn(scheduler)
                )
            }

            fun aTwo(scheduler: Scheduler): Single<String> {
                val opName = "aTwo"
                return opCaller.callSingle(TestOperationType.A, opName,
                    Single.create<String> {
                        emitter ->
                        v{"$curThreadNameInBr $opName: doing work"}
                        
                        waitLong() //imitate work/delay
                        if (randomBoolean(0.9)){
                            v{"$curThreadNameInBr $opName: sending onSuccess"}
                            emitter.onSuccess("$opName: ")
                        } else {
                            v{"$curThreadNameInBr $opName: sending onError"}
                            emitter.onError(Throwable("$opName: "))
                        }
                    }
                        .subscribeOn(scheduler)
                )
            }

            fun bOne(scheduler: Scheduler): Observable<String> {
                val opName = "bOne"
                val disposed = AtomicBoolean(false)
                return opCaller.callObservable(TestOperationType.A, opName,
                    Observable.create<String> {
                        emitter ->
                        v{"$curThreadNameInBr $opName: performing initial setup"}
                        waitShort() //imitate initial setup
            
                        v{"$curThreadNameInBr $opName: sending initial value: 0"}
                        emitter.onNext("$opName: 0 (initial value)") //send initial value
            
                        var i = 0
                        while ( !disposed.get() )
                        {
                            waitLong() //imitate work/delay
//                            v{"$curThreadNameInBr $opName: sending next value: $i"}
                            emitter.onNext("$curThreadNameInBr $opName: $i") //send next value
                            i++
                        }
                    }
                        .doFinally {
                            disposed.set(true)
                        }
                        .subscribeOn(scheduler)
                )
            }

            fun bTwo(scheduler: Scheduler): Single<String> {
                val opName = "bTwo"
                return opCaller.callSingle(TestOperationType.B, opName,
                    Single.create<String> {
                        emitter ->
                        v{"$curThreadNameInBr $opName: doing work"}
                        waitLong() //imitate work/delay
                        if (randomBoolean(0.9)){
                            v{"$curThreadNameInBr $opName: sending onSuccess"}
                            emitter.onSuccess("$opName: ")
                        } else {
                            v{"$curThreadNameInBr $opName: sending onError"}
                            emitter.onError(Throwable("$opName: "))
                        }
                    }
                        .subscribeOn(scheduler)
                )
            }
        }

        class TestClassRandom(opCallerName: String, keepAliveTime: Long, timeUnit: TimeUnit) : AbstractTestClass(opCallerName, keepAliveTime, timeUnit){
            override fun waitShort() {
                sleepRandomTime(10, 300)
            }

            override fun waitLong() {
                sleepRandomTime(10, 1000)
            }
        }

        class TestClassRegular(opCallerName: String, keepAliveTime: Long, timeUnit: TimeUnit) : AbstractTestClass(opCallerName, keepAliveTime, timeUnit){
            override fun waitShort() {
                Thread.sleep(150)
            }

            override fun waitLong() {
                Thread.sleep(500)
            }
        }

        class TestOperationType(name: String) : RxMutExOperationTypesCaller.OperationType(name)
        {
            companion object {
                val A = TestOperationType("A")
                val B = TestOperationType("B")
            }
        }
    //endregion
}