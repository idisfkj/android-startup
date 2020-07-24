package com.rousetime.android_startup

import android.app.Application
import com.rousetime.android_startup.model.LoggerLevel
import com.rousetime.android_startup.startup.SampleFirstStartup
import com.rousetime.android_startup.startup.SampleFourthStartup
import com.rousetime.android_startup.startup.SampleSecondStartup
import com.rousetime.android_startup.startup.SampleThirdStartup

/**
 * Created by idisfkj on 2020/7/24.
 * Email: idisfkj@gmail.com.
 */
class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        StartupManager.Companion.Builder()
            .setLoggerLevel(LoggerLevel.DEBUG)
            .addStartup(SampleFirstStartup())
            .addStartup(SampleSecondStartup())
            .addStartup(SampleThirdStartup())
            .addStartup(SampleFourthStartup())
            .build()
            .start()
            .await()
    }
}