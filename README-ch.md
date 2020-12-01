中文｜[English](README.md)

# android-startup
[![Author](https://img.shields.io/badge/Author-idisfkj-orange.svg)](https://idisfkj.github.io/archives/)
[![Platform](https://img.shields.io/badge/platform-android-lightgrey.svg)](https://www.android.com/)
[![API](https://img.shields.io/badge/API-15%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=15)
[![Language](https://img.shields.io/badge/language-kotlin-ff4081.svg)](https://kotlinlang.org/)
[![Release](https://img.shields.io/github/v/release/idisfkj/android-startup)](https://github.com/idisfkj/android-startup/releases)
[![Code Size](https://img.shields.io/github/languages/code-size/idisfkj/android-startup?color=%23CDDC39)]()
[![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)

`android-startup`提供一种在应用启动时能够更加简单、高效的方式来初始化组件。开发人员可以使用`android-startup`来简化启动序列，并显式地设置初始化顺序与组件之间的依赖关系。
与此同时`android-startup`支持**同步与异步等待**，并通过有向无环图[拓扑排序](https://github.com/idisfkj/android-startup/blob/master/android-startup/src/main/java/com/rousetime/android_startup/sort/TopologySort.kt)的方式来保证内部依赖组件的初始化顺序。

下面是一张与google的[App Startup](https://developer.android.com/topic/libraries/app-startup)功能对比的表格。

|指标|App Startup|Android Startup|
|:---:|:------:| :------:|
|手动配置| ✅ | ✅ |
|自动配置| ✅ | ✅ |
|依赖支持| ✅ | ✅ |
|闭环处理| ✅ | ✅ |
|线程控制| ❌ | ✅ |
|异步等待| ❌ | ✅ |
|依赖回调| ❌ | ✅ |
|手动通知| ❌ | ✅ |
|拓扑优化| ❌ | ✅ |
|耗时统计| ❌ | ✅ |
|线程优先级| ❌ | ✅ |
|多进程| ❌ | ✅ |

> 开源不易，希望朋友小手一抖，右上角来个star，感谢🙏

## 相关文章

[我为何弃用Jetpack的App Startup?](https://juejin.im/post/6859500445669752846)

[Android Startup实现分析](https://juejin.im/post/6871006041262260237)

[Android Startup最新进展](https://mp.weixin.qq.com/s?__biz=MzIzNTc5NDY4Nw==&mid=2247484784&idx=1&sn=435833fade53cfbe62bf5b3e98e60d59&chksm=e8e0fce0df9775f65748debdfc1b8f5013970d92c7e76ea2c7436240438c278fb62bfbbc6cf3&token=630713225&lang=zh_CN#rd)

## 添加依赖
将下面的依赖添加到`build.gradle`文件中:

```
dependencies {
    implementation 'com.rousetime.android:android-startup:1.0.6'
}
```

> 依赖版本的更新信息: [Release](https://github.com/idisfkj/android-startup/releases)

## 快速使用

![](./images/android_startup_diagram.png)

android-startup提供了两种使用方式，在使用之前需要先定义初始化的组件。

### 定义初始化的组件
每一个初始化的组件都需要实现[AndroidStartup<T>](https://github.com/idisfkj/android-startup/blob/master/android-startup/src/main/java/com/rousetime/android_startup/AndroidStartup.kt)抽象类，它实现了`Startup<T>`接口，它主要有以下四个抽象方法：

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

### Manifest中自动配置
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

### Application中手动配置
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
完整的示例代码，你可以通过查看[app](https://github.com/idisfkj/android-startup/tree/master/app)获取。

运行示例代码，控制台将会产生如下日志:

1. 排序优化之后的初始化顺序

```
*****/com.rousetime.sample D/StartupTrack: TopologySort result:
    |================================================================
    |         order          |    [1]
    |----------------------------------------------------------------
    |        Startup         |    SampleFirstStartup
    |----------------------------------------------------------------
    |   Dependencies size    |    0
    |----------------------------------------------------------------
    | callCreateOnMainThread |    true
    |----------------------------------------------------------------
    |    waitOnMainThread    |    false
    |================================================================
    |         order          |    [2]
    |----------------------------------------------------------------
    |        Startup         |    SampleSecondStartup
    |----------------------------------------------------------------
    |   Dependencies size    |    1
    |----------------------------------------------------------------
    | callCreateOnMainThread |    false
    |----------------------------------------------------------------
    |    waitOnMainThread    |    true
    |================================================================
    |         order          |    [3]
    |----------------------------------------------------------------
    |        Startup         |    SampleThirdStartup
    |----------------------------------------------------------------
    |   Dependencies size    |    2
    |----------------------------------------------------------------
    | callCreateOnMainThread |    false
    |----------------------------------------------------------------
    |    waitOnMainThread    |    false
    |================================================================
    |         order          |    [4]
    |----------------------------------------------------------------
    |        Startup         |    SampleFourthStartup
    |----------------------------------------------------------------
    |   Dependencies size    |    3
    |----------------------------------------------------------------
    | callCreateOnMainThread |    false
    |----------------------------------------------------------------
    |    waitOnMainThread    |    false
    |================================================================
```

2. 各组件初始化所消耗的时间

```
*****/com.rousetime.sample D/StartupTrack: startup cost times detail:
    |=================================================================
    |      Startup Name       |   SampleFirstStartup
    | ----------------------- | --------------------------------------
    |   Call On Main Thread   |   true
    | ----------------------- | --------------------------------------
    |   Wait On Main Thread   |   false
    | ----------------------- | --------------------------------------
    |       Cost Times        |   0 ms
    |=================================================================
    |      Startup Name       |   SampleSecondStartup
    | ----------------------- | --------------------------------------
    |   Call On Main Thread   |   false
    | ----------------------- | --------------------------------------
    |   Wait On Main Thread   |   true
    | ----------------------- | --------------------------------------
    |       Cost Times        |   5001 ms
    |=================================================================
    |      Startup Name       |   SampleThirdStartup
    | ----------------------- | --------------------------------------
    |   Call On Main Thread   |   false
    | ----------------------- | --------------------------------------
    |   Wait On Main Thread   |   false
    | ----------------------- | --------------------------------------
    |       Cost Times        |   3007 ms
    |=================================================================
    |      Startup Name       |   SampleFourthStartup
    | ----------------------- | --------------------------------------
    |   Call On Main Thread   |   false
    | ----------------------- | --------------------------------------
    |   Wait On Main Thread   |   false
    | ----------------------- | --------------------------------------
    |       Cost Times        |   102 ms
    |=================================================================
    | Total Main Thread Times |   5008 ms
    |=================================================================
```

## 更多

### 可选配置

* [LoggerLevel](https://github.com/idisfkj/android-startup/blob/master/android-startup/src/main/java/com/rousetime/android_startup/model/LoggerLevel.kt): 控制Android Startup中的日志输出，可选值包括`LoggerLevel.NONE`, `LoggerLevel.ERROR` and `LoggerLevel.DEBUG`。

* [AwaitTimeout](https://github.com/idisfkj/android-startup/blob/master/android-startup/src/main/java/com/rousetime/android_startup/model/StartupConfig.kt): 控制Android Startup中主线程的超时等待时间，即阻塞的最长时间。

* [StartupListener](https://github.com/idisfkj/android-startup/blob/master/android-startup/src/main/java/com/rousetime/android_startup/StartupListener.kt): Android Startup监听器，所有组件初始化完成之后该监听器会被调用。

#### Manifest中配置
使用这些配置，你需要定义一个类去实现`StartupProviderConfig`接口，并且实现它的对应方法。

```
class SampleStartupProviderConfig : StartupProviderConfig {

    override fun getConfig(): StartupConfig =
        StartupConfig.Builder()
            .setLoggerLevel(LoggerLevel.DEBUG)
            .setAwaitTimeout(12000L)
            .setListener(object : StartupListener {
                override fun onCompleted(totalMainThreadCostTime: Long, costTimesModels: List<CostTimesModel>) {
                    // can to do cost time statistics.
                }
            })
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

#### Application中配置
在Application需要借助`StartupManager.Builder()`进行配置。

```
override fun onCreate() {
    super.onCreate()

    val config = StartupConfig.Builder()
        .setLoggerLevel(LoggerLevel.DEBUG)
        .setAwaitTimeout(12000L)
        .setListener(object : StartupListener {
            override fun onCompleted(totalMainThreadCostTime: Long, costTimesModels: List<CostTimesModel>) {
                // can to do cost time statistics.
            }
        })
        .build()

    StartupManager.Builder()
        .setConfig(config)
        ...
        .build(this)
        .start()
        .await()
}
```

### [AndroidStartup](https://github.com/idisfkj/android-startup/blob/master/android-startup/src/main/java/com/rousetime/android_startup/AndroidStartup.kt)

* `createExecutor(): Executor`: 如果定义的组件没有运行在主线程，那么可以通过该方法进行控制运行的子线程。

* `onDependenciesCompleted(startup: Startup<*>, result: Any?)`: 该方法会在每一个依赖执行完毕之后进行回调。

* `manualDispatch(): Boolean`: 返回`true`时，代表需要手动去通知依赖自身的子组件; 需要配合`onDispatch()`来使用。

* `onDispatch()`: 配合`manualDispatch()`使用，通知依赖自身的子组件，开始执行子组件的初始化逻辑。

### [StartupCacheManager](https://github.com/idisfkj/android-startup/blob/master/android-startup/src/main/java/com/rousetime/android_startup/manager/StartupCacheManager.kt)

* `hadInitialized(zClass: Class<out Startup<*>>)`: 检验对应的组件是否已经初始化完成。

* `obtainInitializedResult(zClass: Class<out Startup<*>>): T?`: 获取对应已经初始化的组件所返回的结果。

* `remove(zClass: Class<out Startup<*>>)`: 清除对应组件的初始化缓存结果。

* `clear()`: 清除所有组件初始化的缓存结果。

### [Annotation](https://github.com/idisfkj/android-startup/tree/master/android-startup/src/main/java/com/rousetime/android_startup/annotation)

* ThreadPriority: 设置`Startup`初始化的线程优先级。

* MultipleProcess: 设置`Startup`初始化时所在的进程。

## 示例

* [Sync And Sync](https://github.com/idisfkj/android-startup/blob/master/app/src/main/java/com/rousetime/sample/SampleCommonActivity.kt): 同步与同步依赖的场景

* [Sync And Async](https://github.com/idisfkj/android-startup/blob/master/app/src/main/java/com/rousetime/sample/SampleCommonActivity.kt): 同步与异步依赖的场景

* [Async And Sync](https://github.com/idisfkj/android-startup/blob/master/app/src/main/java/com/rousetime/sample/SampleCommonActivity.kt): 异步与同步依赖的场景

* [Async And Async](https://github.com/idisfkj/android-startup/blob/master/app/src/main/java/com/rousetime/sample/SampleCommonActivity.kt): 异步与异步依赖的场景

* [Async And Async Await Main Thread](https://github.com/idisfkj/android-startup/blob/master/app/src/main/java/com/rousetime/sample/SampleCommonActivity.kt): 异步与异步依赖在主线程等候的场景

* [Manual Dispatch](https://github.com/idisfkj/android-startup/blob/master/app/src/main/java/com/rousetime/sample/SampleCommonActivity.kt): 手动通知依赖完成的场景

* [Thread Priority](https://github.com/idisfkj/android-startup/blob/master/app/src/main/java/com/rousetime/sample/SampleCommonActivity.kt): 改变线程优先级的场景

* [Multiple Processes](https://github.com/idisfkj/android-startup/blob/master/app/src/main/java/com/rousetime/sample/SampleCommonActivity.kt): 多进程初始化的场景

## 实战测试
[AwesomeGithub](https://github.com/idisfkj/AwesomeGithub)中使用了`Android Startup`，优化配置的初始化时间与组件化开发的配置注入时机，使用前与使用后时间对比:

|状态|启动页面|消耗时间|
|---|------| ------|
|使用前|WelcomeActivity|420ms|
|使用后|WelcomeActivity|333ms|

## 联系我
微信搜索公众号【Android补给站】或者扫描下方二维码

![](./images/wx.jpg)

QQ交流群

<img src="./images/qq.png" width = "258" height = "353" alt="" align=center />

## License
请查看[LICENSE](https://github.com/idisfkj/android-startup/blob/master/LICENSE)。
