package com.rousetime.android_startup.utils

import com.rousetime.android_startup.Startup
import com.rousetime.android_startup.extensions.getUniqueKey
import com.rousetime.android_startup.model.CostTimesModel

/**
 * Created by idisfkj on 2020/8/10.
 * Email: idisfkj@gmail.com.
 */
internal object StartupCostTimesUtils {

    val costTimesMap = mutableMapOf<String, CostTimesModel>()

    private const val ACCURACY = 1000 * 1000L

    var mainThreadTimes = 0L

    fun recordStart(startup: Class<out Startup<*>>, callOnMainThread: Boolean) {
        costTimesMap[startup.getUniqueKey()] = CostTimesModel(startup.simpleName, callOnMainThread, System.nanoTime() / ACCURACY)
    }

    fun recordEnd(startup: Class<out Startup<*>>) {
        costTimesMap[startup.getUniqueKey()]?.let {
            it.endTime = System.nanoTime() / ACCURACY
        }
    }

    fun clear() {
        costTimesMap.clear()
    }

    fun printAll() {
        StartupLogUtils.d(buildString {
            append("startup cost times detail:")
            append("\n")
            append("|=================================================================")
            costTimesMap.values.forEach {
                append("\n")
                append("|      Startup Name       |   ${it.name}")
                append("\n")
                append("| ----------------------- | --------------------------------------")
                append("\n")
                append("|      On Main Thread     |   ${it.callOnMainThread}")
                append("\n")
                append("| ----------------------- | --------------------------------------")
                append("\n")
                append("|       Cost Times        |   ${it.endTime - it.startTime} ms")
                append("\n")
                append("|=================================================================")
            }
            append("\n")
            append("| Total Main Thread Times |   ${mainThreadTimes / ACCURACY} ms")
            append("\n")
            append("|=================================================================")
        })
    }
}