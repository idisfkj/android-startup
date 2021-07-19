package com.rousetime.android_startup.utils

import com.rousetime.android_startup.Startup
import com.rousetime.android_startup.extensions.getUniqueKey
import com.rousetime.android_startup.manager.StartupCacheManager
import com.rousetime.android_startup.model.CostTimesModel
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by idisfkj on 2020/8/10.
 * Email: idisfkj@gmail.com.
 */
internal object StartupCostTimesUtils {

    val costTimesMap = ConcurrentHashMap<String, CostTimesModel>()

    private const val ACCURACY = 1000 * 1000L

    var startTime = 0L
    var endTime: Long? = null

    val mainThreadTimes
        get() = (endTime ?: System.nanoTime()) - startTime

    fun recordStart(startup: Class<out Startup<*>>, callOnMainThread: Boolean, waitOnMainThread: Boolean) {
        if (checkOpenStatistics()) {
            costTimesMap[startup.getUniqueKey()] = CostTimesModel(
                startup.simpleName,
                callOnMainThread,
                waitOnMainThread,
                System.nanoTime() / ACCURACY
            )
        }
    }

    fun recordEnd(startup: Class<out Startup<*>>) {
        if (checkOpenStatistics()) {
            costTimesMap[startup.getUniqueKey()]?.let {
                it.endTime = System.nanoTime() / ACCURACY
            }
        }
    }

    fun clear() {
        if (checkOpenStatistics()) {
            endTime = null
            costTimesMap.clear()
        }
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
                append("|   Call On Main Thread   |   ${it.callOnMainThread}")
                append("\n")
                append("| ----------------------- | --------------------------------------")
                append("\n")
                append("|   Wait On Main Thread   |   ${it.waitOnMainThread}")
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

    private fun checkOpenStatistics() = StartupCacheManager.instance.initializedConfig?.openStatistic == true
}