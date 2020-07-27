package com.rousetime.sample.startup

import android.content.Context
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.Startup
import com.rousetime.android_startup.utils.StartupLogUtils

/**
 * Created by idisfkj on 2020/7/24.
 * Email: idisfkj@gmail.com.
 */
class SampleSecondStartup : AndroidStartup<Boolean>() {

    override fun isOnMainThread(): Boolean = false

    override fun isNeedWait(): Boolean = false

    override fun create(context: Context): Boolean {
        Thread.sleep(5000)
        return true
    }

    override fun dependencies(): List<Class<out Startup<*>>>? {
        return listOf(SampleFirstStartup::class.java)
    }
}