package com.rousetime.sample.startup.multiple

import android.content.Context
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.annotation.MultipleProcess

/**
 * Created by idisfkj on 2020/9/14.
 * Email: idisfkj@gmail.com.
 */
@MultipleProcess(":multiple.provider")
class SampleMultipleFirstStartup : AndroidStartup<String>() {

    override fun create(context: Context): String? {
        return SampleMultipleFirstStartup::class.java.simpleName
    }

    override fun callCreateOnMainThread(): Boolean = false

    override fun waitOnMainThread(): Boolean = false

}