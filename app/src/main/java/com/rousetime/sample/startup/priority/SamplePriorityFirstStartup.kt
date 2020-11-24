package com.rousetime.sample.startup.priority

import android.content.Context
import android.os.Process
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.annotation.ThreadPriority

/**
 * Created by idisfkj on 2020/9/16.
 * Email: idisfkj@gmail.com.
 */
@ThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
class SamplePriorityFirstStartup : AndroidStartup<String>() {

    override fun create(context: Context): String? {
        val i = buildString {
            repeat(1000000) {
                append("$it")
            }
        }
        return SamplePriorityFirstStartup::class.java.simpleName
    }

    override fun callCreateOnMainThread(): Boolean = false

    override fun waitOnMainThread(): Boolean = false

}