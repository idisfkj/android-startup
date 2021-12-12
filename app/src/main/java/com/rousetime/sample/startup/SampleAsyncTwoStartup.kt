package com.rousetime.sample.startup

import android.content.Context
import com.rousetime.sample.ClassIdAndroidStartup as AndroidStartup

/**
 * Created by idisfkj on 2020/8/17.
 * Email: idisfkj@gmail.com.
 */
class SampleAsyncTwoStartup: AndroidStartup<String>() {

    override fun create(context: Context): String? {
        Thread.sleep(3000)
        return "async two"
    }

    override fun callCreateOnMainThread(): Boolean = false

    override fun waitOnMainThread(): Boolean = false
}