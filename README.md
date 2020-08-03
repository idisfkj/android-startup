中文｜[English](README-en.md)

# android-startup
[![Author](https://img.shields.io/badge/Author-idisfkj-orange.svg)](https://idisfkj.github.io/archives/)
[![Platform](https://img.shields.io/badge/platform-android-lightgrey.svg)](https://www.android.com/)
[![API](https://img.shields.io/badge/API-15%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=15)
[![Language](https://img.shields.io/badge/language-kotlin-ff4081.svg)](https://kotlinlang.org/)
[![Release](https://img.shields.io/github/v/release/idisfkj/android-startup)](https://github.com/idisfkj/android-startup/releases)
[![Code Size](https://img.shields.io/github/languages/code-size/idisfkj/android-startup?color=%23CDDC39)]()
[![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)

Android Startup提供一种在应用启动时能够更加简单、高效的方式来初始化组件。开发人员可以使用Android Startup来简化启动序列，并显式地设置初始化顺序与组件之间的依赖关系。
与此同时，Android Startup支持**同步与异步等待**，并通过有向无环图[拓扑排序](https://github.com/idisfkj/android-startup/blob/master/android-startup/src/main/java/com/rousetime/android_startup/sort/TopologySort.kt)的方式来保证内部依赖组件的初始化顺序。

# 添加依赖
将下面的依赖添加到`build.gradle`文件中:

```
dependencies {
    implementation 'com.rousetime.android:android-startup:1.0.1'
}
```

> 依赖版本的更新信息: [Release](https://github.com/idisfkj/android-startup/releases)

# 快速使用
android-startup提供了两种使用方式，在使用之前需要先定义初始化的组件。

## 定义初始化的组件
每一个初始化的组件都需要实现`AndroidStartup<T>`抽象类，它实现了`Startup<T>`接口，它主要有以下四个抽象方法：

* `callCreateOnMainThread()`用来控制`create()`方法调时所在的线程，返回true代表在主线程执行。

* `waitOnMainThread()`用来控制当前初始化的组件是否需要在主线程进行等待其完成。如果返回true，将在主线程等待，并且阻塞主线程。

* `create()`组件初始化方法，执行需要处理的初始化逻辑，支持返回一个`T`类型的实例。

* `dependencies()`返回`Startup<T>`类型的list集合。用来表示当前组件在执行之前需要依赖的组件。

例如，下面定义一个`SampleFirstStartup`类来实现`AndroidStartup<String>`抽象类:

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
因为`SampleFirstStartup`在执行之前不需要依赖其它组件，所以它的`dependencies()`方法可以返回空，同时它会在主线程中执行。

> 注意：️虽然`waitOnMainThread()`返回了`false`，但由于它是在主线程中执行，而主线程默认是阻塞的，所以`callCreateOnMainThread()`返回`true`时，该方法设置将失效。

假设你还需要定义`SampleSecondStartup`，它依赖于`SampleFirstStartup`。这意味着在执行`SampleSecondStartup`之前`SampleFirstStartup`必须先执行完毕。

```
class SampleSecondStartup : AndroidStartup<Boolean>() {

    override fun callCreateOnMainThread(): Boolean = false

    override fun waitOnMainThread(): Boolean = true

    override fun create(context: Context): Boolean {
        // 模仿执行耗时
        Thread.sleep(5000)
        return true
    }

    override fun dependencies(): List<Class<out Startup<*>>>? {
        return listOf(SampleFirstStartup::class.java)
    }

}
```
在`dependencies()`方法中返回了`SampleFirstStartup`，所以它能保证`SampleFirstStartup`优先执行完毕。
它会在子线程中执行，但由于`waitOnMainThread()`返回了`true`，所以主线程会阻塞等待直到它执行完毕。

例如，你还定义了[SampleThirdStartup](https://github.com/idisfkj/android-startup/blob/master/app/src/main/java/com/rousetime/sample/startup/SampleThirdStartup.kt)与[SampleFourthStartup](https://github.com/idisfkj/android-startup/blob/master/app/src/main/java/com/rousetime/sample/startup/SampleFourthStartup.kt)

## 使用Manifest实现自动配置
第一种初始化方法是在Manifest中进行自动配置。

在Android Startup中提供了`StartupProvider`类，它是一个特殊的content provider，提供自动识别在Manifest中配置的初始化组件。

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
