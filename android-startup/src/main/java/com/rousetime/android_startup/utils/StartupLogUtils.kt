package com.rousetime.android_startup.utils

import android.util.Log
import com.rousetime.android_startup.model.LoggerLevel

/**
 * Created by idisfkj on 2020/7/24.
 * Email: idisfkj@gmail.com.
 */
internal object StartupLogUtils {

    private const val TAG = "StartupTrack"
    var level: LoggerLevel = LoggerLevel.NONE

    fun e(message: String) {
        if (level >= LoggerLevel.ERROR) Log.e(TAG, message)
    }

    fun d(message: String) {
        if (level >= LoggerLevel.DEBUG) Log.d(TAG, message)
    }
}