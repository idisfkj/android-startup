package com.rousetime.sample

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.StartupListener
import com.rousetime.android_startup.StartupManager
import com.rousetime.android_startup.manager.StartupCacheManager
import com.rousetime.android_startup.model.CostTimesModel
import com.rousetime.android_startup.model.LoggerLevel
import com.rousetime.android_startup.model.StartupConfig
import com.rousetime.sample.startup.multiple.SampleMultipleFifthStartup
import com.rousetime.sample.startup.multiple.SampleMultipleFourthStartup
import com.rousetime.sample.startup.multiple.SampleMultipleSixthStartup
import com.rousetime.sample.startup.multiple.SampleMultipleThirdStartup

/**
 * Created by idisfkj on 2020/9/15.
 * Email: idisfkj@gmail.com.
 */
class MultipleProcessService : Service() {

    private var mServiceListener: IServiceListenerInterface? = null

    private var mBinder = object : IMultipleProcessServiceInterface.Stub() {

        override fun clear() {
            StartupCacheManager.instance.clear()
        }

        override fun initStartup() {
            // must be start main thread.
            Handler(Looper.getMainLooper()).post {
                val list = mutableListOf<AndroidStartup<*>>()
                list.add(SampleMultipleThirdStartup())
                list.add(SampleMultipleFourthStartup())
                list.add(SampleMultipleFifthStartup())
                list.add(SampleMultipleSixthStartup())
                val config = StartupConfig.Builder()
                    .setLoggerLevel(LoggerLevel.DEBUG)
                    .setAwaitTimeout(12000L)
                    .setListener(object : StartupListener {
                        override fun onCompleted(totalMainThreadCostTime: Long, costTimesModels: List<CostTimesModel>) {
                            // can to do cost time statistics.
                            mServiceListener?.onCompleted(buildString {
                                append("Startup Completed: ")
                                append("\n")
                                append("\n")
                                costTimesModels.forEach {
                                    append("\n")
                                    append("Startup Name: ${it.name}")
                                    append("\n")
                                    append("CallOnMainThread: ${it.callOnMainThread}")
                                    append("\n")
                                    append("WaitOnMainThread: ${it.waitOnMainThread}")
                                    append("\n")
                                    append("Cost times: ${it.endTime - it.startTime} ms")
                                    append("\n")
                                }
                                if (costTimesModels.isEmpty()) {
                                    append("result form cache.")
                                    append("\n")
                                    list.forEach {
                                        append("\n")
                                        append("${it::class.java.simpleName}: ${StartupCacheManager.instance.obtainInitializedResult<String>(it::class.java)}")
                                        append("\n")
                                    }
                                }
                            }, totalMainThreadCostTime)
                            Log.d("StartupTrack", "onCompleted: ${costTimesModels.size}")
                        }
                    })
                    .build()

                StartupManager.Builder()
                    .setConfig(config)
                    .addAllStartup(list)
                    .build(this@MultipleProcessService)
                    .start()
                    .await()
            }
        }

        override fun addServiceListener(serviceListener: IServiceListenerInterface?) {
            mServiceListener = serviceListener
        }

    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }
}