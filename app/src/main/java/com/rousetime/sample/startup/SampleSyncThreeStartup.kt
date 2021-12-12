package com.rousetime.sample.startup

import android.content.Context
import com.rousetime.sample.ClassIdAndroidStartup as AndroidStartup

/**
 * Created by idisfkj on 2020/8/18.
 * Email : idisfkj@gmail.com.
 */
class SampleSyncThreeStartup : AndroidStartup<String>() {
    override fun callCreateOnMainThread(): Boolean = true

    override fun create(context: Context): String? {
        return "sync three"
    }

    override fun waitOnMainThread(): Boolean = false
}