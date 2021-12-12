package com.rousetime.android_startup.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Process

/**
 * Created by idisfkj on 2020/9/14.
 * Email: idisfkj@gmail.com.
 */
internal object ProcessUtils {

    private lateinit var curProcessName: String

    private fun getProcessName(context: Context): String {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val myPid = Process.myPid()
        am.runningAppProcesses.forEach {
            if (it.pid == myPid) {
                return it.processName
            }
        }
        return ""
    }

    fun isMainProcess(context: Context): Boolean = getProcessName(context) == context.packageName

    fun isMultipleProcess(context: Context, processName: Array<out String>): Boolean {
        if (!this::curProcessName.isInitialized) {
            curProcessName = getProcessName(context)
        }

        processName.forEach {
            if (curProcessName == "${context.packageName}$it") {
                return true
            }
        }
        return false
    }
}