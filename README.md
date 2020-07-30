# android-startup
The Android Startup library provides a straightforward, performant way to initialize components at application startup. Both library developers and app developers can use Android Startup to streamline startup sequences and explicitly set the order of initialization.

# Setup
Add the following dependency to your `build.gradle` file:

```
dependencies {
    implementation 'com.rousetime.android:android-startup:1.0.0'
}
```

# Quick Usage
There are tow ways of using android-startup in your project.

Need to be initialized before using android-startup.

## Initialize components at android startup
Apps and libraries often rely on having components initialized right away when the app starts up. Android Startup provides a more performant way to initialize components at app startup and explicitly define their dependencies.

To use Android Startup to initialize components at startup, you must define a component initializer for each component that the app needs to initialize.

### Implementation component android startup
You define each component initializer by creating a class that implements the `AndroidStartup<T>` abstract. 
This abstract implements the `Startup<T>` interface. And this abstract defines four important methods:

* The `callCreateOnMainThread()`method,which control the `create()`method is in the main thread calls.Othrewise in the other thread.

* The `waitOnMainThread()`method,which control the current component should call waiting in the main thread.If returns true, will block the main thread.

* The `create()`method,which contains all of the necessary operations to initialize the component and returns an instance of `T`

* The `dependencies()`method,which returns a list of the other `Startup<T>` objects that the initializer depends on. You can use this method to control the order in which the app runs the initializers at startup.

For example, Define a `SampleFirstStartup` class that implements `AndroidStartup<String>`:

```
class SampleFirstStartup : AndroidStartup<String>() {

    override fun callCreateOnMainThread(): Boolean = true

    override fun waitOnMainThread(): Boolean = false

    override fun create(context: Context): String? {
        return this.javaClass.simpleName
    }

    override fun dependencies(): List<Class<out Startup<*>>>? {
        return null
    }

}
```
The `dependencies()` method returns an null list because `SampleFirstStartup ` does not depend on any other libraries.

Suppose that your app also depends on a library called `SampleSecondStartup`, which in turn depends on `SampleFirstStartup`. This dependency means that you need to make sure that Android Startup initializes `SampleFirstStartup ` first. Define an `SampleSecondStartup ` class that implements `AndroidStartup<Boolean>`:

```
class SampleSecondStartup : AndroidStartup<Boolean>() {

    override fun callCreateOnMainThread(): Boolean = false

    override fun waitOnMainThread(): Boolean = true

    override fun create(context: Context): Boolean {
        Thread.sleep(5000)
        return true
    }

    override fun dependencies(): List<Class<out Startup<*>>>? {
        return listOf(SampleFirstStartup::class.java)
    }

}
```
Because you include `SampleFirstStartup` in the `dependencies()` method, Android Startup initializes `SampleFirstStartup` before `SampleSecondStartup`.

# To be continued...
