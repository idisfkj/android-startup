package com.rousetime.sample

import android.app.Application
import android.util.Log
import com.rousetime.android_startup.StartupInitializer
import com.rousetime.sample.startup.SampleFourthStartup
import com.rousetime.sample.startup.SampleSecondStartup

/**
 * Created by idisfkj on 2020/7/24.
 * Email: idisfkj@gmail.com.
 */
class SampleApplication : Application() {

    companion object {
        const val TAG = "SampleApplication"
    }

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

        if (StartupInitializer.instance.hadInitialized(SampleSecondStartup::class.java)) {
            Log.d(
                TAG,
                "SampleSecondStartup had initialized, result => ${StartupInitializer.instance.obtainInitializedResult<Boolean>(SampleSecondStartup::class.java)}"
            )
        }

        if (StartupInitializer.instance.hadInitialized(SampleFourthStartup::class.java)) {
            Log.d(
                TAG,
                "SampleFourthStartup had initialized, result => ${StartupInitializer.instance.obtainInitializedResult<Boolean>(SampleFourthStartup::class.java)}"
            )
        } else {
            Log.e(TAG, "SampleFourthStartup not initialized.")
        }
    }
}