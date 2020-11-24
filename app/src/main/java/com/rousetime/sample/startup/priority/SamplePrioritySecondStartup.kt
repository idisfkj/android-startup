package com.rousetime.sample.startup.priority

import android.content.Context
import com.rousetime.android_startup.AndroidStartup

/**
 * Created by idisfkj on 2020/9/16.
 * Email: idisfkj@gmail.com.
 */
class SamplePrioritySecondStartup : AndroidStartup<String>() {

    override fun create(context: Context): String? {
        val i = buildString {
            repeat(1000000) {
                append("$it")
            }
        }
        return SamplePrioritySecondStartup::class.java.simpleName
    }

    override fun callCreateOnMainThread(): Boolean = false

    override fun waitOnMainThread(): Boolean = false

}