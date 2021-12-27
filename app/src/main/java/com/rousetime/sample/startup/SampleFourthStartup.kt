package com.rousetime.sample.startup

import android.content.Context
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.Startup

/**
 * Created by idisfkj on 2020/7/24.
 * Email: idisfkj@gmail.com.
 */
class SampleFourthStartup : AndroidStartup<Any>() {

    override fun callCreateOnMainThread(): Boolean = false

    override fun waitOnMainThread(): Boolean = false

    override fun create(context: Context): Any? {
        Thread.sleep(100)
        return null
    }

    override fun dependenciesByName(): List<String> {
        return listOf(
            "com.rousetime.sample.startup.SampleFirstStartup",
            "com.rousetime.sample.startup.SampleSecondStartup",
            "com.rousetime.sample.startup.SampleThirdStartup"
        )
    }
}