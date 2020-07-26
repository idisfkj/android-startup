package com.rousetime.sample.startup

import android.content.Context
import com.rousetime.android_startup.AndroidStartup

/**
 * Created by idisfkj on 2020/7/24.
 * Email: idisfkj@gmail.com.
 */
class SampleFirstStartup : AndroidStartup<String>() {

    override fun isOnMainThread(): Boolean = true

    override fun isNeedWait(): Boolean = false

    override fun create(context: Context): String {
        Thread.sleep(2000)
        return this.javaClass.simpleName
    }

}