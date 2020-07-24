package com.rousetime.android_startup.utils

import android.util.Log
import com.rousetime.android_startup.model.LoggerLevel

/**
 * Created by idisfkj on 2020/7/24.
 * Email: idisfkj@gmail.com.
 */
object StartupLogUtils {

    private const val TAG = "StartupTrack"
    var level: LoggerLevel = LoggerLevel.NONE

    fun e(message: String) {
        if (level >= LoggerLevel.ERROR) Log.d(TAG, message)
    }

    fun d(message: String) {
        if (level >= LoggerLevel.DEBUG) Log.v(TAG, message)
    }
}