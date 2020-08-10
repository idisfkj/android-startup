package com.rousetime.android_startup.run

import android.content.Context
import com.rousetime.android_startup.Startup
import com.rousetime.android_startup.StartupInitializer
import com.rousetime.android_startup.dispatcher.ManagerDispatcher
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
        startup.toWait()
        StartupLogUtils.d("${startup::class.java.simpleName} being create.")

        StartupCostTimesUtils.recordStart(startup::class.java, startup.callCreateOnMainThread())
        val result = startup.create(context)
        StartupCostTimesUtils.recordEnd(startup::class.java)

        // To save result of initialized component.
        StartupInitializer.instance.saveInitializedComponent(startup::class.java, result)
        StartupLogUtils.d("${startup::class.java.simpleName} was completed.")

        dispatcher.notifyChildren(startup, result, sortStore)
    }
}