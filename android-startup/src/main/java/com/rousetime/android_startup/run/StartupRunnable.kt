package com.rousetime.android_startup.run

import android.content.Context
import com.rousetime.android_startup.Startup
import com.rousetime.android_startup.dispatcher.ManagerDispatcher
import com.rousetime.android_startup.model.StartupSortStore
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
        val result = startup.create(context)
        StartupLogUtils.d("Startup ${startup::class.java.simpleName} was completed.")

        startup.toNotify()
        dispatcher.notifyChildren(startup, result, sortStore)
    }
}