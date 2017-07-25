package com.github.kotlinutils.rx2

import com.github.unitimber.test.junit.UniTimberTestRule
import org.junit.Rule

/**
 * Created by nbv54 on 19-Jun-17.
 */
class RxSharedStateAccessingOpCallerTest {
    @Rule @JvmField val uniTimberTestRule = UniTimberTestRule()
    /*
    @Test
    fun run_A_while_A_is_running() {
        val latch = CountDownLatch(2)
        val testTag = "AwA"
        
        val opCaller = RxSharedStateAccessingOpCaller(testTag)
        val aKey = "A"
        
        val aItem = Item()
        val aItemSpy = spy(aItem)
        
        val inOrder = inOrder(aItemSpy)
                
        val aOneName = "aOne"
        val aOne = Single.fromCallable {
            v("$testTag-$aOneName") { "$curThreadNameInBr Start work" }
            aItemSpy.append(aOneName)
            Thread.sleep(300)
            v("$testTag-$aOneName") { "$curThreadNameInBr Finish work" }
        }
            .doFinally {
                latch.countDown()
            }
            .withOpCaller(opCaller, Schedulers.io(), aKey, aOneName)
            .doOnSuccess {
                d("$testTag-$aOneName-observer") { "$curThreadNameInBr Received onSuccess" }
            }
    
        val aTwoName = "aTwo"
        val aTwo = opCaller.callSingle(Single.fromCallable {
            v("$testTag-$aTwoName") { "$curThreadNameInBr Start work" }
            aItemSpy.append(aTwoName)
            Thread.sleep(300)
            v("$testTag-$aTwoName") { "$curThreadNameInBr Finish work" }
        }
            .doFinally {
                latch.countDown()
            }
            .subscribeOn(Schedulers.io()), Schedulers.io(),
            aKey, aTwoName
        )
            .doOnSuccess {
                d("$testTag-$aTwoName-observer") { "$curThreadNameInBr Received onSuccess" }
            }
        
        aOne.test()
        Thread.sleep(100)
        aTwo.test()
                
        assertTrue { latch.await(2, TimeUnit.SECONDS) }
        
        inOrder.verify(aItemSpy).append(aOneName)
        inOrder.verify(aItemSpy).append(aTwoName)
    }
    
    @Test
    fun run_A_while_B_is_running() {
        val latch = CountDownLatch(2)
        val testTag = "AwB"
    
        val opCaller = RxSharedStateAccessingOpCaller(testTag)
        val aKey = "A"
        val bKey = "B"
        
        val aItem = Item()
        val aItemSpy = spy(aItem)
        val bItem = Item()
        val bItemSpy = spy(bItem)
        
        val inOrder = inOrder(aItemSpy, bItemSpy)
    
        val bOneName = "bOne"
        val bOne = opCaller.callSingle(Single.fromCallable {
            v("$testTag-$bOneName") { "$curThreadNameInBr Start work" }
            bItemSpy.startWork(bOneName)
            Thread.sleep(300)
            bItemSpy.finishWork(bOneName)
            v("$testTag-$bOneName") { "$curThreadNameInBr Finish work" }
        }
            .doFinally {
                latch.countDown()
            }
            .subscribeOn(Schedulers.io()),,
            bKey, bOneName
        )
    
        val aOneName = "aOne"
        val aOne = opCaller.callSingle(Single.fromCallable {
            v("$testTag-$aOneName") { "$curThreadNameInBr Start work" }
            aItemSpy.startWork(aOneName)
            Thread.sleep(300)
            aItemSpy.finishWork(aOneName)
            v("$testTag-$aOneName") { "$curThreadNameInBr Finish work" }
        }
            .doFinally {
                latch.countDown()
            }
            .subscribeOn(Schedulers.io()),,
            aKey, aOneName
        )
    
    
        bOne.test()
        Thread.sleep(100)
        aOne.test()
        
        assertTrue { latch.await(2, TimeUnit.SECONDS) }
        
        inOrder.verify(bItemSpy).startWork(bOneName)
        inOrder.verify(aItemSpy).startWork(aOneName)
        inOrder.verify(bItemSpy).finishWork(bOneName)
        inOrder.verify(aItemSpy).finishWork(aOneName)
    }
    
    
    @Test
    fun run_A_while_ALL_is_running() {
        val latch = CountDownLatch(2)
        val testTag = "AwALL"
    
        val opCaller = RxSharedStateAccessingOpCaller(testTag)
        val aKey = "A"
        
        val aItem = Item()
        val aItemSpy = spy(aItem)
        val bItem = Item()
        val bItemSpy = spy(bItem)
        
        val inOrder = inOrder(aItemSpy, bItemSpy)
        
        val allOneName = "allOneName"
        val allOne = opCaller.callSingle(Single.fromCallable {
            v("$testTag-$allOneName") { "$curThreadNameInBr Start work" }
            aItemSpy.startWork(allOneName)
            bItemSpy.startWork(allOneName)
            Thread.sleep(300)
            aItemSpy.finishWork(allOneName)
            bItemSpy.finishWork(allOneName)
            v("$testTag-$allOneName") { "$curThreadNameInBr Finish work" }
        }
            .doFinally {
                latch.countDown()
            }
            .subscribeOn(Schedulers.io()),,
            COMMON_OP_KEY, allOneName
        )
    
        val aOneName = "aOne"
        val aOne = opCaller.callSingle(Single.fromCallable {
            v("$testTag-$aOneName") { "$curThreadNameInBr Start work" }
            aItemSpy.startWork(aOneName)
            Thread.sleep(300)
            aItemSpy.finishWork(aOneName)
            v("$testTag-$aOneName") { "$curThreadNameInBr Finish work" }
        }
            .doFinally {
                latch.countDown()
            }
            .subscribeOn(Schedulers.io()),,
            aKey, aOneName
        )
        
        allOne.test()
        Thread.sleep(100)
        aOne.test()
                
        latch.await(2, TimeUnit.SECONDS)
        
        inOrder.verify(aItemSpy).startWork(allOneName)
        inOrder.verify(bItemSpy).startWork(allOneName)
        
        inOrder.verify(aItemSpy).finishWork(allOneName)
        inOrder.verify(bItemSpy).finishWork(allOneName)
        
        inOrder.verify(aItemSpy).startWork(aOneName)
        inOrder.verify(aItemSpy).finishWork(aOneName)
    }
    
    
    @Test
    fun run_ALL_while_A_is_running() {
        val latch = CountDownLatch(2)
        val testTag = "ALLwA"
    
        val opCaller = RxSharedStateAccessingOpCaller(testTag)
        val aKey = "A"
        
        val aItem = Item()
        val aItemSpy = spy(aItem)
        val bItem = Item()
        val bItemSpy = spy(bItem)
        
        val inOrder = inOrder(aItemSpy, bItemSpy)
            
        val allOneName = "allOneName"
        val allOne = opCaller.callSingle(Single.fromCallable {
            v("$testTag-$allOneName") { "$curThreadNameInBr Start work" }
            aItemSpy.startWork(allOneName)
            bItemSpy.startWork(allOneName)
            Thread.sleep(300)
            aItemSpy.finishWork(allOneName)
            bItemSpy.finishWork(allOneName)
            v("$testTag-$allOneName") { "$curThreadNameInBr Finish work" }
        }
            .doFinally {
                latch.countDown()
            }
            .subscribeOn(Schedulers.io()),,
            COMMON_OP_KEY, allOneName
        )
        
            
        val aOneName = "aOne"
        val aOne = opCaller.callSingle(Single.fromCallable {
            v("$testTag-$aOneName") { "$curThreadNameInBr Start work" }
            aItemSpy.startWork(aOneName)
            Thread.sleep(300)
            aItemSpy.finishWork(aOneName)
            v("$testTag-$aOneName") { "$curThreadNameInBr Finish work" }
        }
            .doFinally {
                latch.countDown()
            }
            .subscribeOn(Schedulers.io()),,
            aKey, aOneName
        )
        
        aOne.test()
        Thread.sleep(100)
        allOne.test()
        
        latch.await(2, TimeUnit.SECONDS)
        
        inOrder.verify(aItemSpy).startWork(aOneName)
        inOrder.verify(aItemSpy).finishWork(aOneName)
        
        inOrder.verify(aItemSpy).startWork(allOneName)
        inOrder.verify(bItemSpy).startWork(allOneName)
        
        inOrder.verify(aItemSpy).finishWork(allOneName)
        inOrder.verify(bItemSpy).finishWork(allOneName)
    }
    
    
    @Test
    fun run_ALL_while_A_and_B_are_running() {
        val latch = CountDownLatch(3)
        val testTag = "ALLwAnB"
    
        val opCaller = RxSharedStateAccessingOpCaller(testTag)
        val aKey = "A"
        val bKey = "B"
        
        val aItem = Item()
        val aItemSpy = spy(aItem)
        val bItem = Item()
        val bItemSpy = spy(bItem)
        
        val inOrder = inOrder(aItemSpy, bItemSpy)
        
        var tAStartTime : Long = 0
        var tBStartTime : Long = 0
        var tAllStartTime : Long = 0
            
        val aOneName = "aOne"
        val aOne = opCaller.callSingle(Single.fromCallable {
            tAStartTime = System.currentTimeMillis()
            v("$testTag-$aOneName") { "$curThreadNameInBr Start work" }
            aItemSpy.startWork(aOneName)
            Thread.sleep(100)
            aItemSpy.finishWork(aOneName)
            v("$testTag-$aOneName") { "$curThreadNameInBr Finish work" }
        }
            .doFinally {
                latch.countDown()
            }
            .subscribeOn(Schedulers.io()),,
            aKey, aOneName
        )
        
        val bOneName = "bOne"
        val bOne = opCaller.callSingle(Single.fromCallable {
            tBStartTime = System.currentTimeMillis()
            v("$testTag-$bOneName") { "$curThreadNameInBr Start work" }
            bItemSpy.startWork(bOneName)
            Thread.sleep(100)
            bItemSpy.finishWork(bOneName)
            v("$testTag-$bOneName") { "$curThreadNameInBr Finish work" }
        }
            .doFinally {
                latch.countDown()
            }
            .subscribeOn(Schedulers.io()),,
            bKey, bOneName
        )
        
        val allOneName = "allOneName"
        val allOne = opCaller.callSingle(Single.fromCallable {
            tAllStartTime = System.currentTimeMillis()
            v("$testTag-$allOneName") { "$curThreadNameInBr Start work" }
            aItemSpy.startWork(allOneName)
            bItemSpy.startWork(allOneName)
            Thread.sleep(100)
            aItemSpy.finishWork(allOneName)
            bItemSpy.finishWork(allOneName)
            v("$testTag-$allOneName") { "$curThreadNameInBr Finish work" }
        }
            .doFinally {
                latch.countDown()
            }
            .subscribeOn(Schedulers.io()),,
            COMMON_OP_KEY, allOneName
        )
        
        
        aOne.test()
        Thread.sleep(50)
        bOne.test()
        Thread.sleep(50)
        allOne.test()
        
        latch.await(2, TimeUnit.SECONDS)
        
        inOrder.verify(aItemSpy).startWork(aOneName)
        inOrder.verify(bItemSpy).startWork(bOneName)
        
        inOrder.verify(aItemSpy).finishWork(aOneName)
        inOrder.verify(bItemSpy).finishWork(bOneName)
        
        inOrder.verify(aItemSpy).startWork(allOneName)
        inOrder.verify(bItemSpy).startWork(allOneName)
        inOrder.verify(aItemSpy).finishWork(allOneName)
        inOrder.verify(bItemSpy).finishWork(allOneName)
        
        assertTrue { tBStartTime - tAStartTime < 100 }
        assertTrue { tAllStartTime - tAStartTime >= 100 }
    }
    
    @Test
    fun apply_scheduler_to_OpCallerSingle() {
        val latch = CountDownLatch(1)
        val testTag = "AwA"
    
        val opCaller = RxSharedStateAccessingOpCaller(testTag)
        val aKey = "A"
    
        val aItem = Item()
        val aItemSpy = spy(aItem)
    
        val inOrder = inOrder(aItemSpy)
        
        val aOneName = "aOne"
        val aOne = Single.fromCallable {
            v("$testTag-$aOneName") { "$curThreadNameInBr Start work" }
            aItemSpy.append(aOneName)
            v("$testTag-$aOneName") { "$curThreadNameInBr Finish work" }
            aItemSpy.data
        }
            .doFinally {
                latch.countDown()
            }
            .toOpCallerSingle(opCaller, aKey, aOneName)
            .subscribeOn(Schedulers.io())
            .doOnSuccess {
                d("$testTag-$aOneName-observer") { "$curThreadNameInBr Received onSuccess: '$it'" }
            }
        
        aOne.test()
    
//        assertTrue { latch.await(2, TimeUnit.SECONDS) }
        latch.await()
        
    }
    */
    
    /*
    inline fun threadForSingleWork(threadName: String,
                                   compLock: ThreadIndependentCompositeOperationLock,
                                   singleKey: String,
                                   latch: CountDownLatch,
                                   crossinline work: () -> Unit
    ): Thread {
        return Thread({
            d { "$curThreadNameInBr Deq" }
            
            compLock.lock(singleKey)
            
            v { "$curThreadNameInBr Start work" }
            
            work()
            
            v { "$curThreadNameInBr Finish work" }
            
            compLock.unlock(singleKey)
            
            latch.countDown()
        }, threadName)
    }
    
    inline fun threadForAllWork(threadName: String,
                                compLock: ThreadIndependentCompositeOperationLock,
                                latch: CountDownLatch,
                                crossinline work: () -> Unit
    ): Thread {
        return Thread({
            d { "$curThreadNameInBr Deq" }
            
            compLock.lockAll()
            
            v { "$curThreadNameInBr Start work" }
            
            work()
            
            v { "$curThreadNameInBr Finish work" }
            
            compLock.unlockAll()
            
            latch.countDown()
        }, threadName)
    }
    */
    class Item {
        var data = ""
        fun startWork(str: String){}
        fun finishWork(str: String){}
        fun append(str: String) = data.plus(str)
    }
}