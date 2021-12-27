package com.rousetime.android_startup

import android.content.Context
import com.rousetime.android_startup.dispatcher.Dispatcher
import com.rousetime.android_startup.executor.StartupExecutor

/**
 * Created by idisfkj on 2020/7/23.
 * Email: idisfkj@gmail.com.
 */
interface Startup<T> : Dispatcher, StartupExecutor {

    /**
     * Contains all of the necessary operations to initialize the component.
     * and returns an instance of `T`
     *
     * @param [context]
     */
    fun create(context: Context): T?

    /**
     * Returns a list of the other [Startup] objects that the initializer depends on.
     */
    @Deprecated("Used dependenciesByName instead", ReplaceWith("dependenciesByName()"))
    fun dependencies(): List<Class<out Startup<*>>>?

    /**
     * Returns a list of the other [Startup] Class Name that the initializer depends on.
     */
    fun dependenciesByName(): List<String>?

    /**
     * Returns size of depends on.
     */
    fun getDependenciesCount(): Int

    /**
     * Called whenever there is a dependency completion.
     *
     * @param [startup] dependencies [startup].
     * @param [result] of dependencies startup.
     */
    fun onDependenciesCompleted(startup: Startup<*>, result: Any?)

    /**
     * Returns true that manual to dispatch. but must be call [onDispatch], in order to notify children that dependencies startup completed.
     */
    fun manualDispatch(): Boolean

    /**
     * Register dispatcher when [manualDispatch] return true.
     */
    fun registerDispatcher(dispatcher: Dispatcher)

    /**
     * Start to dispatch when [manualDispatch] return true.
     */
    fun onDispatch()

}