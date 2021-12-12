package com.rousetime.sample.startup

import android.content.Context
import com.rousetime.sample.ClassIdAndroidStartup as AndroidStartup
import com.rousetime.android_startup.Startup

/**
 * Created by idisfkj on 2020/8/17.
 * Email: idisfkj@gmail.com.
 */
class SampleAsyncOneStartup : AndroidStartup<String>() {

    private var mResult: String? = null

    override fun create(context: Context): String? {
        Thread.sleep(2000)
        return "$mResult + async one"
    }

    override fun callCreateOnMainThread(): Boolean = false

    override fun waitOnMainThread(): Boolean = false

    override fun dependencies(): List<Class<out Startup<*>>>? {
        return listOf(SampleSyncThreeStartup::class.java)
    }

    override fun onDependenciesCompleted(startup: Startup<*>, result: Any?) {
        mResult = result as? String?
    }
}