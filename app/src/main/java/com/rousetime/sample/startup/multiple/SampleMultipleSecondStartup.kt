package com.rousetime.sample.startup.multiple

import android.content.Context
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.Startup
import com.rousetime.android_startup.annotation.MultipleProcess

/**
 * Created by idisfkj on 2020/9/14.
 * Email: idisfkj@gmail.com.
 */
@MultipleProcess(":multiple.provider")
class SampleMultipleSecondStartup : AndroidStartup<Boolean>() {

    override fun create(context: Context): Boolean? {
        return true
    }

    override fun callCreateOnMainThread(): Boolean = false

    override fun waitOnMainThread(): Boolean = false

    override fun dependencies(): List<Class<out Startup<*>>>? {
        return arrayListOf(SampleMultipleFirstStartup::class.java)
    }

}