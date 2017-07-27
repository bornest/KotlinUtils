KotlinUtils
===========
[![](https://jitpack.io/v/bornest/KotlinUtils.svg)](https://jitpack.io/#bornest/KotlinUtils)

A modular Android library with useful utility classes & extensions written in
Kotlin.

These are the utilites that I’m continuously working on and using
while developing Android apps.

**Most of the functions and methods are `inline` and don’t add to your app’s method
count.**

Modules
-------

| Module       | Description                                                                                                                                                                                                             | Internal Dependencies               |
|--------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------|
| `all`        | Contains every other module.                                                                                                                                                                                            | `concurrent`, `core`, `id`, `random`, `rx2` |
| `concurrent` | Thread factories, extensions & utility functions for Atomic types, Threads, Executors and Locks.                                                                                                                        | `core`                              |
| `core`       | Extensions & utility functions for Kotlin itself: e.g. reflection extensions.                                                                                                                                           | \-                                  |
| `id`         | Extensions & utility functions for objects and data identifiers: e.g. UUID.                                                                                                                                             | \-                                  |
| `random`     | Extensions & utility functions that simplify the usage of random.                                                                                                                                                       | \-                                  |
| `rx2`        | Everything related to RxJava 2: utility classes for thread-safe lock-free operations on shared state, custom event buses and various Kotlin extension functions for more idiomatic usage of common RxJava 2 constructs. | `concurrent`, `core`, `id`, `random`      |

Setup
--------
To use the latest release add following code to your Gradle dependencies:

`compile 'com.github.bornest.KotlinUtils:{module-name}:v0.1.0'`

To use the latest SNAPSHOT add following code to your Gradle dependencies:

`compile 'com.github.bornest.KotlinUtils:{module-name}:-SNAPSHOT'`

where `{module-name}` is the name of desired module

Usage
-----

See each module's README for quick overview of its functionality:
- [concurrent](https://github.com/bornest/KotlinUtils/blob/master/concurrent/README.md)
- core
- id
- random
- [rx2](https://github.com/bornest/KotlinUtils/blob/master/rx2/README.md)


Important note
--------------

The library is in the early stages of development – there almost certainly will be
some breaking changes in the future.

Test coverage will be increased over time.

License
-------
```
Copyright (c) 2017, Borislav Nesterov

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

```
