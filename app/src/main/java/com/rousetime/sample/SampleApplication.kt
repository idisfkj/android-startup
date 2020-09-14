package com.rousetime.sample

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.rousetime.android_startup.StartupListener
import com.rousetime.android_startup.StartupManager
import com.rousetime.android_startup.manager.StartupCacheManager
import com.rousetime.android_startup.model.CostTimesModel
import com.rousetime.android_startup.model.LoggerLevel
import com.rousetime.android_startup.model.StartupConfig
import com.rousetime.sample.startup.SampleFirstStartup
import com.rousetime.sample.startup.SampleFourthStartup
import com.rousetime.sample.startup.SampleSecondStartup
import com.rousetime.sample.startup.SampleThirdStartup
import com.rousetime.sample.startup.multiple.SampleMultipleFirstStartup
import com.rousetime.sample.startup.multiple.SampleMultipleSecondStartup
import com.rousetime.sample.startup.multiple.SampleMultipleThirdStartup

/**
 * Created by idisfkj on 2020/7/24.
 * Email: idisfkj@gmail.com.
 */
class SampleApplication : Application() {

    companion object {
        const val TAG = "SampleApplication"

        // only in order to test on MainActivity.
        val costTimesLiveData = MutableLiveData<List<CostTimesModel>>()
    }

    override fun onCreate() {
        super.onCreate()

//        val config = StartupConfig.Builder()
//            .setLoggerLevel(LoggerLevel.DEBUG)
//            .setAwaitTimeout(12000L)
//            .setListener(object : StartupListener {
//                override fun onCompleted(totalMainThreadCostTime: Long, costTimesModels: List<CostTimesModel>) {
//                    // can to do cost time statistics.
//                    costTimesLiveData.value = costTimesModels
//                    Log.d("StartupTrack", "onCompleted: ${costTimesModels.size}")
//                }
//            })
//            .build()
//
//        StartupManager.Builder()
//            .setConfig(config)
//            .addStartup(SampleFirstStartup())
//            .addStartup(SampleSecondStartup())
//            .addStartup(SampleThirdStartup())
//            .addStartup(SampleFourthStartup())
//            .addStartup(SampleMultipleFirstStartup())
//            .addStartup(SampleMultipleSecondStartup())
//            .addStartup(SampleMultipleThirdStartup())
//            .build(this)
//            .start()
//            .await()

        if (StartupCacheManager.instance.hadInitialized(SampleSecondStartup::class.java)) {
            Log.d(
                TAG,
                "SampleSecondStartup had initialized, result => ${StartupCacheManager.instance.obtainInitializedResult<Boolean>(SampleSecondStartup::class.java)}"
            )
        }

        if (StartupCacheManager.instance.hadInitialized(SampleFourthStartup::class.java)) {
            Log.d(
                TAG,
                "SampleFourthStartup had initialized, result => ${StartupCacheManager.instance.obtainInitializedResult<Boolean>(SampleFourthStartup::class.java)}"
            )
        } else {
            Log.e(TAG, "SampleFourthStartup not initialized.")
        }

        // call multiple process init.
        val uri = Uri.parse("content://com.rousetime.sample.android_startup.multiple")
        contentResolver.query(uri, null, null, null, null)?.apply { close() }
    }
}