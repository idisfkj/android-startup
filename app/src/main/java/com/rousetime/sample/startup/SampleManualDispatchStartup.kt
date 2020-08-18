package com.rousetime.sample.startup

import android.content.Context
import com.rousetime.android_startup.AndroidStartup

/**
 * Created by idisfkj on 2020/8/18.
 * Email: idisfkj@gmail.com.
 */
class SampleManualDispatchStartup : AndroidStartup<String>() {

    override fun create(context: Context): String? {
        Thread {
            Thread.sleep(2000)
            // manual dispatch
            onDispatch()
        }.start()
        return "manual dispatch"
    }

    override fun callCreateOnMainThread(): Boolean = true

    override fun waitOnMainThread(): Boolean = false

    override fun manualDispatch(): Boolean = true

}