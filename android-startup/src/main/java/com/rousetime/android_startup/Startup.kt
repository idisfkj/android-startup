package com.rousetime.android_startup

import android.content.Context
import com.rousetime.android_startup.dispatcher.Dispatcher
import com.rousetime.android_startup.executor.StartupExecutor
import com.rousetime.android_startup.utils.StartupLogUtils

/**
 * Created by idisfkj on 2020/7/23.
 * Email: idisfkj@gmail.com.
 */
interface Startup<T> : Dispatcher, StartupExecutor {

    fun create(context: Context): T

    fun dependencies(): List<Class<out Startup<*>>>?

}