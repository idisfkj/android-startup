package com.rousetime.sample.startup

import android.content.Context
import android.util.Log
import com.rousetime.sample.ClassIdAndroidStartup as AndroidStartup

/**
 * Created by idisfkj on 2020/8/17.
 * Email: idisfkj@gmail.com.
 */
class SampleSyncOneStartup: AndroidStartup<String>() {

    override fun create(context: Context): String? {
        return "sync one"
    }

    override fun callCreateOnMainThread(): Boolean = true

    override fun waitOnMainThread(): Boolean = false
}