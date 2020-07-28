package com.rousetime.sample

import android.app.Application
import com.rousetime.android_startup.StartupManager
import com.rousetime.android_startup.model.LoggerLevel
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
        StartupManager.Builder()
            .setAwaitTimeout(12000)
            .setLoggerLevel(LoggerLevel.DEBUG)
            .addStartup(SampleFirstStartup())
            .addStartup(SampleSecondStartup())
            .addStartup(SampleThirdStartup())
            .addStartup(SampleFourthStartup())
            .build(this)
            .start()
            .await()
    }
}