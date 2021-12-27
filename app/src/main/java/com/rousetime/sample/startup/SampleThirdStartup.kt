package com.rousetime.sample.startup

import android.content.Context
import android.util.Log
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.Startup

/**
 * Created by idisfkj on 2020/7/24.
 * Email: idisfkj@gmail.com.
 */
class SampleThirdStartup : AndroidStartup<Long>() {

    override fun callCreateOnMainThread(): Boolean = false

    override fun waitOnMainThread(): Boolean = false

    override fun create(context: Context): Long? {
        Thread.sleep(3000)
        return 10L
    }

    override fun dependenciesByName(): List<String> {
        return listOf(
            "com.rousetime.sample.startup.SampleFirstStartup",
            "com.rousetime.sample.startup.SampleSecondStartup"
        )
    }

    override fun onDependenciesCompleted(startup: Startup<*>, result: Any?) {
        Log.d("SampleThirdStartup", "onDependenciesCompleted: ${startup::class.java.simpleName}, $result")
    }
}