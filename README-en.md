English｜[中文](README.md)

# android-startup
[![Author](https://img.shields.io/badge/Author-idisfkj-orange.svg)](https://idisfkj.github.io/archives/)
[![Platform](https://img.shields.io/badge/platform-android-lightgrey.svg)](https://www.android.com/)
[![API](https://img.shields.io/badge/API-15%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=15)
[![Language](https://img.shields.io/badge/language-kotlin-ff4081.svg)](https://kotlinlang.org/)
[![Release](https://img.shields.io/github/v/release/idisfkj/android-startup)](https://github.com/idisfkj/android-startup/releases)
[![Code Size](https://img.shields.io/github/languages/code-size/idisfkj/android-startup?color=%23CDDC39)]()
[![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)

The Android Startup library provides a straightforward, performant way to initialize components at application startup. Both library developers and app developers can use Android Startup to streamline startup sequences and explicitly set the order of initialization.

At the same time, the Android Startup support **async await and sync await**. And [topological ordering](https://github.com/idisfkj/android-startup/blob/master/android-startup/src/main/java/com/rousetime/android_startup/sort/TopologySort.kt) is used to ensure the initialization order of dependent components.

# Setup
Add the following dependency to your `build.gradle` file:

```
dependencies {
    implementation 'com.rousetime.android:android-startup:1.0.1'
}
```

> Versions update information: [Release](https://github.com/idisfkj/android-startup/releases)

# Quick Usage
There are tow ways of using android-startup in your project,need to be initialized before using android-startup.

## Define Initialize components
You define each component initializer by creating a class that implements the `AndroidStartup<T>` abstract.
This abstract implements the `Startup<T>` interface. And this abstract defines four important methods:

* The `callCreateOnMainThread()`method,which control the `create()`method is in the main thread calls.Othrewise in the other thread.

* The `waitOnMainThread()`method,which control the current component should call waiting in the main thread.If returns true, will block the main thread.

* The `create()`method,which contains all of the necessary operations to initialize the component and returns an instance of `T`

* The `dependencies()`method,which returns a list of the other `Startup<T>` objects that the initializer depends on.

For example, Define a `SampleFirstStartup` class that implements `AndroidStartup<String>`:

```
class SampleFirstStartup : AndroidStartup<String>() {

    override fun callCreateOnMainThread(): Boolean = true

    override fun waitOnMainThread(): Boolean = false

    override fun create(context: Context): String? {
        // todo something
        return this.javaClass.simpleName
    }

    override fun dependencies(): List<Class<out Startup<*>>>? {
        return null
    }

}
```
The `dependencies()` method returns an null list because `SampleFirstStartup ` does not depend on any other libraries.

Suppose that your app also depends on a library called `SampleSecondStartup`, which in turn depends on `SampleFirstStartup`. This dependency means that you need to make sure that Android Startup initializes `SampleFirstStartup ` first.

```
class SampleSecondStartup : AndroidStartup<Boolean>() {

    override fun callCreateOnMainThread(): Boolean = false

    override fun waitOnMainThread(): Boolean = true

    override fun create(context: Context): Boolean {
        // Simulation execution time.
        Thread.sleep(5000)
        return true
    }

    override fun dependencies(): List<Class<out Startup<*>>>? {
        return listOf(SampleFirstStartup::class.java)
    }

}
```
Because you include `SampleFirstStartup` in the `dependencies()` method, Android Startup initializes `SampleFirstStartup` before `SampleSecondStartup`.

For example, you also define a [SampleThirdStartup](https://github.com/idisfkj/android-startup/blob/master/app/src/main/java/com/rousetime/sample/startup/SampleThirdStartup.kt) and a [SampleFourthStartup](https://github.com/idisfkj/android-startup/blob/master/app/src/main/java/com/rousetime/sample/startup/SampleFourthStartup.kt)

## Automatic initialization in manifest
The first one is automatic initializes startup in manifest.

Android Startup includes a special content provider called `StartupProvider` that it uses to discover and call your component startup.
Android Startup discovers component startup by first checking for a `<meta-data>` entry under the `StartupProvider` manifest entry. Then, Android Startup calls the `dependencies()` methods for any startup that it has already discovered.

This means that in order for a component startup to be discoverable by Android Startup, one of the following conditions must be met:

* The component startup has a corresponding `<meta-data>` entry under the `StartupProvider` manifest entry.

* The component startup is listed in the `dependencies()` method from an startup that is already discoverable.

Consider again the example,to make sure Android Startup can discover these startup, add the following to the manifest file:

```
<provider
    android:name="com.rousetime.android_startup.provider.StartupProvider"
    android:authorities="${applicationId}.android_startup"
    android:exported="false">

    <meta-data
        android:name="com.rousetime.sample.startup.SampleFourthStartup"
        android:value="android.startup" />

</provider>
```
You don't need to add a `<meta-data>` entry for `SampleFirstStartup`, `SampleSecondStartup` and `SampleThirdStartup`, because them are a dependency of `SampleFourthStartup`. This means that if `SampleFourthStartup` is discoverable, then are also.

## Manually initialization in application
The second one is manually initializes startup in application.

Consider again the example,to make sure Android Startup can initializes,you can use `StartupManager.Builder()` directly in order to manually initialize components.

For example, the following code calls `StartupManager.Builder()` and manually initializes them:

```
class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        StartupManager.Builder()
            .addStartup(SampleFirstStartup())
            .addStartup(SampleSecondStartup())
            .addStartup(SampleThirdStartup())
            .addStartup(SampleFourthStartup())
            .build(this)
            .start()
            .await()
    }
}
```
You can check out the sample [app](https://github.com/idisfkj/android-startup/tree/master/app) for more code information.

# More

## Config

* `LoggerLevel`: control Android Startup log level, include `LoggerLevel.NONE`, `LoggerLevel.ERROR` and `LoggerLevel.DEBUG`.

* `AwaitTimeout`: control Android Startup timeout of await on main thread.

### config in manifest
To use these config, you must define a class than implements the `StartupProviderConfig` interface:

```
class SampleStartupProviderConfig : StartupProviderConfig {

    override fun getConfig(): StartupConfig =
        StartupConfig.Builder()
            .setLoggerLevel(LoggerLevel.DEBUG)
            .setAwaitTimeout(12000L)
            .build()
}
```
At the same time, you need add `StartupProviderConfig` to manifest file:

```
<provider
    android:name="com.rousetime.android_startup.provider.StartupProvider"
    android:authorities="${applicationId}.android_startup"
    android:exported="false">

    <meta-data
        android:name="com.rousetime.sample.startup.SampleStartupProviderConfig"
        android:value="android.startup.provider.config" />

</provider>
```
`StartupProvider` that it uses to discover and call `SampleStartupProviderConfig`.

### config in application
To use these config,you need use `StartupManager.Builder()` in application.

```
override fun onCreate() {
    super.onCreate()

    val config = StartupConfig.Builder()
        .setLoggerLevel(LoggerLevel.DEBUG)
        .setAwaitTimeout(12000L)
        .build()

    StartupManager.Builder()
        .setConfig(config)
        ...
        .build(this)
        .start()
        .await()
}
```

## Method

### AndroidStartup

* `createExecutor(): Executor`: If the startup not create on main thread, them the startup will run in the executor.

* `onDependenciesCompleted(startup: Startup<*>, result: Any?)`: This method is called whenever there is a dependency completion.

# License
Please see [LICENSE](https://github.com/idisfkj/android-startup/blob/master/LICENSE)
