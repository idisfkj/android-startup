package com.rousetime.sample.startup.multiple

import android.content.Context
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.annotation.MultipleProcess

/**
 * Created by idisfkj on 2020/9/14.
 * Email: idisfkj@gmail.com.
 */
@MultipleProcess(":multiple.test")
class SampleMultipleThirdStartup : AndroidStartup<String>() {

    override fun create(context: Context): String? {
        return SampleMultipleThirdStartup::class.java.name
    }

    override fun callCreateOnMainThread(): Boolean = false

    override fun waitOnMainThread(): Boolean = false

}