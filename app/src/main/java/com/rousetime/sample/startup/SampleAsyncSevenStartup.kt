package com.rousetime.sample.startup

import android.content.Context
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.Startup

/**
 * Created by idisfkj on 2020/8/18.
 * Email: idisfkj@gmail.com.
 */
class SampleAsyncSevenStartup : AndroidStartup<String>() {

    private var mResult: String? = null

    override fun create(context: Context): String? {
        Thread.sleep(3000)
        return "$mResult + async seven"
    }

    override fun callCreateOnMainThread(): Boolean = false

    override fun waitOnMainThread(): Boolean = false

    override fun dependencies(): List<Class<out Startup<*>>>? {
        return listOf(SampleManualDispatchStartup::class.java)
    }

    override fun onDependenciesCompleted(startup: Startup<*>, result: Any?) {
        mResult = result as? String?
    }
}