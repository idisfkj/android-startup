package com.rousetime.sample.startup.multiple

import android.content.Context
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.annotation.MultipleProcess

/**
 * Created by idisfkj on 2020/9/15.
 * Email: idisfkj@gmail.com.
 */
@MultipleProcess(":multiple.process.service", ":multiple.test")
class SampleMultipleFourthStartup : AndroidStartup<String>() {

    override fun create(context: Context): String? {
        Thread.sleep(1000)
        return SampleMultipleFourthStartup::class.java.simpleName
    }

    override fun callCreateOnMainThread(): Boolean = false

    override fun waitOnMainThread(): Boolean = false
}