package com.rousetime.sample

import android.app.Application
import com.rousetime.android_startup.StartupManager
import com.rousetime.android_startup.model.LoggerLevel
import com.rousetime.android_startup.model.StartupConfig
import com.rousetime.sample.startup.SampleFirstStartup
import com.rousetime.sample.startup.SampleFourthStartup
import com.rousetime.sample.startup.SampleSecondStartup
import com.rousetime.sample.startup.SampleThirdStartup

/**
 * Created by idisfkj on 2020/7/24.
 * Email: idisfkj@gmail.com.
 */
class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

//        val config = StartupConfig.Builder()
//            .setLoggerLevel(LoggerLevel.DEBUG)
//            .setAwaitTimeout(12000L)
//            .build()
//
//        StartupManager.Builder()
//            .setConfig(config)
//            .addStartup(SampleFirstStartup())
//            .addStartup(SampleSecondStartup())
//            .addStartup(SampleThirdStartup())
//            .addStartup(SampleFourthStartup())
//            .build(this)
//            .start()
//            .await()
    }
}