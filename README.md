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

* `callCreateOnMainThread(): Boolean`用来控制`create()`方法调时所在的线程，返回true代表在主线程执行。

* `waitOnMainThread(): Boolean`用来控制当前初始化的组件是否需要在主线程进行等待其完成。如果返回true，将在主线程等待，并且阻塞主线程。

* `create(): T?`组件初始化方法，执行需要处理的初始化逻辑，支持返回一个`T`类型的实例。

* `dependencies(): List<Class<out Startup<*>>>?`返回`Startup<*>`类型的list集合。用来表示当前组件在执行之前需要依赖的组件。

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

## Manifest中自动配置
第一种初始化方法是在Manifest中进行自动配置。

在Android Startup中提供了`StartupProvider`类，它是一个特殊的content provider，提供自动识别在manifest中配置的初始化组件。
为了让其能够自动识别，需要在`StartupProvider`中定义`<meta-data>`标签。其中的`name`为定义的组件类，`value`的值对应为`android.startup`。

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
你不需要将`SampleFirstStartup`、`SampleSecondStartup`与`SampleThirdStartup`添加到`<meta-data>`标签中。这是因为在`SampleFourthStartup`中，它的`dependencies()`中依赖了这些组件。`StartupProvider`会自动识别已经声明的组件中依赖的其它组件。

## Application中手动配置
第二种初始化方法是在Application进行手动配置。

手动初始化需要使用到`StartupManager.Builder()`。

例如，如下代码使用`StartupManager.Builder()`进行初始化配置。

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
完整的代码实例，你可以通过查看[app](https://github.com/idisfkj/android-startup/tree/master/app)获取。

# 更多

## 可选配置

* `LoggerLevel`: 控制Android Startup中的日志输出，可选值包括`LoggerLevel.NONE`, `LoggerLevel.ERROR` and `LoggerLevel.DEBUG`。

* `AwaitTimeout`: 控制Android Startup中主线程的超时等待时间，即阻塞的最长时间。

### Manifest中配置
使用这些配置，你需要定义一个类去实现`StartupProviderConfig`接口，并且实现它的对应方法。

```
class SampleStartupProviderConfig : StartupProviderConfig {

    override fun getConfig(): StartupConfig =
        StartupConfig.Builder()
            .setLoggerLevel(LoggerLevel.DEBUG)
            .setAwaitTimeout(12000L)
            .build()
}
```
与此同时，你还需要在manifest中进行配置`StartupProviderConfig`。

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
经过上面的配置，`StartupProvider`会自动解析`SampleStartupProviderConfig`。

### Application中配置
在Application需要借助`StartupManager.Builder()`进行配置。

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

## 方法

### AndroidStartup

* `createExecutor(): Executor`: 如果定义的组件没有运行在主线程，那么可以通过该方法进行控制运行的子线程。

* `onDependenciesCompleted(startup: Startup<*>, result: Any?)`: 该方法会在每一个依赖执行完毕之后进行回调。

# 实战测试
[AwesomeGithub](https://github.com/idisfkj/AwesomeGithub)中使用了`Android Startup`，优化配置的初始化时间与组件化开发的配置注入时机，使用前与使用后时间对比:

|状态|启动页面|消耗时间|
|---|------| ------|
|使用前|WelcomeActivity|420ms|
|使用后|WelcomeActivity|333ms|

# License
请查看[LICENSE](https://github.com/idisfkj/android-startup/blob/master/LICENSE)。
