# KotlinUtils-rx2

Android library built on top of RxJava 2 with utility classes:
- thread-safe lock-free operations on shared state (`RxFunctionCallingQueue`, `RxMutExOperationTypesCaller`, `RxSharedStateAccessingOpCaller`)
- easy communication between modules (`RxBus` varieties)
- various Kotlin extension functions for more idiomatic usage of common RxJava 2 constructs


## RxBus

A wrapper for RxJava 2 Subject which represents an Event Bus.

Abstracts the functionality of underlying Subject by providing smaller interface relevant to this particular use case.


There are different varieties of RxBus:
- `RxPublishBus` - `RxBus` based on `PublishSubject` of type `Any`
- `RxTypedPublishBus<T>`- `RxBus` based on `PublishSubject` of type `T`
- `RxBehaviorBus` - `RxBus` based on `BehaviorSubject` of type `Any`
- `RxTypedBehaviorBus<T>`- `RxBus` based on `BehaviorSubject` of type `T`
- `RxReplayBus` - `RxBus` based on `ReplaySubject` of type `Any`
- `RxTypedReplayBus<T>`- `RxBus` based on `ReplaySubject` of type `T`

## RxFunctionCallingQueue
Queue of function objects that are called in a serialized manner on specified Scheduler.

_Example_:
```Kotlin
//operations will be performed on a thread from Schedulers.io() thread pool
//operation_2 will be called after operation_1 completes

val callingQ = RxFunctionCallingQueue("exampleQ").apply { setupSubscription(Schedulers.io()) }

callingQ.enq("operation_1") {
  //do something
}

callingQ.enq("operation_2") {
  //do something else
}

```

## RxMutExOperationTypesCaller
Lock-free race-condition-preventing calling queue for operations of mutually exclusive types that work with shared state on different threads.

_Example_:

Allowing simultaneous operations of type A, simultaneous operations of type B, but not A and B.


## RxSharedStateAccessingOpCaller
Lock-free race-condition-preventing calling queue for operations
that work with different parts of shared state (or with the whole shared state) on different threads.

Uses String keys to identify the exact part of shared state that going to be accessed.

_Example_:

If we have a shared list of users,
we can allow simultaneous operations on different users,
but not on the same user.
