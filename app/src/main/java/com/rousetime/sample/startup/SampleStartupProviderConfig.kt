package com.rousetime.sample.startup

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.rousetime.android_startup.StartupListener
import com.rousetime.android_startup.model.CostTimesModel
import com.rousetime.android_startup.model.LoggerLevel
import com.rousetime.android_startup.model.StartupConfig
import com.rousetime.android_startup.provider.StartupProviderConfig

/**
 * Created by idisfkj on 2020/7/28.
 * Email: idisfkj@gmail.com.
 */
class SampleStartupProviderConfig : StartupProviderConfig {

    companion object {
        // only in order to test on MainActivity.
        val costTimesLiveData = MutableLiveData<List<CostTimesModel>>()
    }

    override fun getConfig(): StartupConfig =
        StartupConfig.Builder()
            .setLoggerLevel(LoggerLevel.DEBUG)
            .setAwaitTimeout(12000L)
            .setListener(object : StartupListener {
                override fun onCompleted(totalMainThreadCostTime: Long, costTimesModels: List<CostTimesModel>) {
                    // can to do cost time statistics.
                    costTimesLiveData.value = costTimesModels
                    Log.d("StartupTrack", "onCompleted: ${costTimesModels.size}")
                }
            })
            .build()
}