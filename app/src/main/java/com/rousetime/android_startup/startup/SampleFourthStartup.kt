package com.rousetime.android_startup.startup

import android.content.Context
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.Startup

/**
 * Created by idisfkj on 2020/7/24.
 * Email: idisfkj@gmail.com.
 */
class SampleFourthStartup : AndroidStartup<Any>() {

    override fun isOnMainThread(): Boolean = false

    override fun isNeedWait(): Boolean = true

    override fun create(context: Context): Any {
        Thread.sleep(100)
        return Any()
    }

    override fun dependencies(): List<Class<out Startup<*>>>? {
        return listOf(SampleFirstStartup::class.java, SampleSecondStartup::class.java, SampleThirdStartup::class.java)
    }
}