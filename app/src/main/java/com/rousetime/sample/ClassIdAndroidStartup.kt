package com.rousetime.sample

import android.util.Log
import androidx.annotation.CallSuper
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.Startup

abstract class ClassIdAndroidStartup<T> : AndroidStartup<T>() {
    override val id: String by lazy {
        this::class.java.name
    }

    override fun dependencyIds(): List<String>? {
        return dependencies()?.map { it.name }
    }

    open fun dependencies(): List<Class<out Startup<*>>>? {
        return null
    }

    @CallSuper
    override fun onAllCompleted() {
        Log.e("===do onAllCompleted===", "in ${javaClass.name}")
    }
}