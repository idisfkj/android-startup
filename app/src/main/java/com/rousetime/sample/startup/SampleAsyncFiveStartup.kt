package com.rousetime.sample.startup

import android.content.Context
import com.rousetime.android_startup.AndroidStartup

/**
 * Created by idisfkj on 2020/8/18.
 * Email : idisfkj@gmail.com.
 */
class SampleAsyncFiveStartup: AndroidStartup<String>() {

    override fun callCreateOnMainThread(): Boolean = false

    override fun create(context: Context): String? {
        Thread.sleep(1000)
        return "async five"
    }

    override fun waitOnMainThread(): Boolean = false
}