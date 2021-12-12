package com.rousetime.sample.startup.multiple

import android.content.Context
import com.rousetime.sample.ClassIdAndroidStartup as AndroidStartup
import com.rousetime.android_startup.Startup
import com.rousetime.android_startup.annotation.MultipleProcess

/**
 * Created by idisfkj on 2020/9/15.
 * Email: idisfkj@gmail.com.
 */
@MultipleProcess(":multiple.process.service")
class SampleMultipleSixthStartup : AndroidStartup<String>() {

    override fun create(context: Context): String? {
        return SampleMultipleSixthStartup::class.java.simpleName
    }

    override fun callCreateOnMainThread(): Boolean = false

    override fun waitOnMainThread(): Boolean = true

    override fun dependencies(): List<Class<out Startup<*>>>? {
        return listOf(SampleMultipleFifthStartup::class.java)
    }

}