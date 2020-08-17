package com.rousetime.sample.startup

import android.content.Context
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.Startup

/**
 * Created by idisfkj on 2020/8/17.
 * Email: idisfkj@gmail.com.
 */
class SampleSyncTwoStartup: AndroidStartup<String>() {

    private var mResult: String? = null

    override fun create(context: Context): String? {
        return "$mResult + sync two"
    }

    override fun callCreateOnMainThread(): Boolean = true

    override fun waitOnMainThread(): Boolean = false

    override fun dependencies(): List<Class<out Startup<*>>>? {
        return listOf(SampleSyncOneStartup::class.java)
    }

    override fun onDependenciesCompleted(startup: Startup<*>, result: Any?) {
        mResult = result as? String?
    }

}