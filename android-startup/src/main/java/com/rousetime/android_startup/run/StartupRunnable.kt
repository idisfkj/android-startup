package com.rousetime.android_startup.run

import android.content.Context
import android.os.Process
import androidx.core.os.TraceCompat
import com.rousetime.android_startup.Startup
import com.rousetime.android_startup.annotation.ThreadPriority
import com.rousetime.android_startup.dispatcher.ManagerDispatcher
import com.rousetime.android_startup.manager.StartupCacheManager
import com.rousetime.android_startup.model.ResultModel
import com.rousetime.android_startup.model.StartupSortStore
import com.rousetime.android_startup.utils.StartupCostTimesUtils
import com.rousetime.android_startup.utils.StartupLogUtils

/**
 * Created by idisfkj on 2020/7/27.
 * Email: idisfkj@gmail.com.
 */
internal class StartupRunnable(
    private val context: Context,
    private val startup: Startup<*>,
    private val sortStore: StartupSortStore,
    private val dispatcher: ManagerDispatcher
) : Runnable {

    override fun run() {
        Process.setThreadPriority(startup::class.java.getAnnotation(ThreadPriority::class.java)?.priority ?: Process.THREAD_PRIORITY_DEFAULT)
        startup.toWait()
        StartupLogUtils.d("${startup::class.java.simpleName} being create.")

        TraceCompat.beginSection(startup::class.java.simpleName)
        StartupCostTimesUtils.recordStart(startup, startup.callCreateOnMainThread(), startup.waitOnMainThread())
        val result = startup.create(context)
        StartupCostTimesUtils.recordEnd(startup)
        TraceCompat.endSection()

        // To save result of initialized component.
        StartupCacheManager.instance.saveInitializedComponent(startup::class.java, ResultModel(result))
        StartupLogUtils.d("${startup::class.java.simpleName} was completed.")

        dispatcher.notifyChildren(startup, result, sortStore)
    }
}