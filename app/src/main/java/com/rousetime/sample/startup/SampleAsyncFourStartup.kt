package com.rousetime.sample.startup

import android.content.Context
import com.rousetime.sample.ClassIdAndroidStartup as AndroidStartup
import com.rousetime.android_startup.Startup

/**
 * Created by idisfkj on 2020/8/18.
 * Email : idisfkj@gmail.com.
 */
class SampleAsyncFourStartup : AndroidStartup<String>() {

    private var mResult: String? = null

    override fun callCreateOnMainThread(): Boolean = false

    override fun create(context: Context): String? {
        Thread.sleep(1000)
        return "$mResult + async four"
    }

    override fun waitOnMainThread(): Boolean = true

    override fun dependencies(): List<Class<out Startup<*>>>? {
        return listOf(SampleAsyncSixStartup::class.java)
    }

    override fun onDependenciesCompleted(startup: Startup<*>, result: Any?) {
        mResult = result as? String?
    }
}