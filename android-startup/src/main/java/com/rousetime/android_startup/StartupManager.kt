package com.rousetime.android_startup

import android.content.Context
import android.os.Looper
import androidx.core.os.TraceCompat
import com.rousetime.android_startup.annotation.MultipleProcess
import com.rousetime.android_startup.dispatcher.StartupManagerDispatcher
import com.rousetime.android_startup.execption.StartupException
import com.rousetime.android_startup.manager.StartupCacheManager
import com.rousetime.android_startup.model.LoggerLevel
import com.rousetime.android_startup.model.StartupConfig
import com.rousetime.android_startup.model.StartupSortStore
import com.rousetime.android_startup.sort.TopologySort
import com.rousetime.android_startup.utils.ProcessUtils
import com.rousetime.android_startup.utils.StartupCostTimesUtils
import com.rousetime.android_startup.utils.StartupLogUtils
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by idisfkj on 2020/7/24.
 * Email : idisfkj@gmail.com.
 */
class StartupManager private constructor(
        private val context: Context,
        private val startupList: List<AndroidStartup<*>>,
        private val needAwaitCount: AtomicInteger,
        private val config: StartupConfig
) {

    private var mAwaitCountDownLatch: CountDownLatch? = null

    companion object {
        const val AWAIT_TIMEOUT = 10000L
    }

    init {
        // save initialized config
        StartupCacheManager.instance.saveConfig(config)
        StartupLogUtils.level = config.loggerLevel
    }

    fun start() = apply {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw StartupException("start method must be call in MainThread.")
        }

        if (mAwaitCountDownLatch != null) {
            throw StartupException("start method repeated call.")
        }
        mAwaitCountDownLatch = CountDownLatch(needAwaitCount.get())

        if (startupList.isNullOrEmpty()) {
            StartupLogUtils.e("startupList is empty in the current process.")
            return@apply
        }

        TraceCompat.beginSection(StartupManager::class.java.simpleName)
        StartupCostTimesUtils.startTime = System.nanoTime()

        TopologySort.sort(startupList).run {
            mDefaultManagerDispatcher.prepare()
            execute(this)
        }

        if (needAwaitCount.get() <= 0) {
            StartupCostTimesUtils.endTime = System.nanoTime()
            TraceCompat.endSection()
        }
    }

    private fun execute(sortStore: StartupSortStore) {
        sortStore.result.forEach { mDefaultManagerDispatcher.dispatch(it, sortStore) }
    }

    /**
     * Startup dispatcher
     */
    private val mDefaultManagerDispatcher by lazy {
        StartupManagerDispatcher(context, needAwaitCount, mAwaitCountDownLatch, startupList.size, config.listener)
    }

    /**
     * to await startup completed
     * block main thread.
     */
    fun await() {
        if (mAwaitCountDownLatch == null) {
            throw StartupException("must be call start method before call await method.")
        }

        val count = needAwaitCount.get()
        try {
            mAwaitCountDownLatch?.await(config.awaitTimeout, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        if (count > 0) {
            StartupCostTimesUtils.endTime = System.nanoTime()
            TraceCompat.endSection()
        }
    }

    class Builder {
        private var mStartupMap = mutableMapOf<String, AndroidStartup<*>>()
        private var mNeedAwaitCount = AtomicInteger()
        private var mLoggerLevel = LoggerLevel.NONE
        private var mAwaitTimeout = AWAIT_TIMEOUT
        private var mConfig: StartupConfig? = null

        fun addStartup(startup: AndroidStartup<*>) = apply {
            val existed = mStartupMap[startup.id]
            if (existed != null) {
                throw IllegalStateException("both ${startup::class.qualifiedName} and ${existed::class.qualifiedName} have same id,please check their id,or you add same instance more than one times.")
            }

            mStartupMap[startup.id] = startup
        }

        fun setAllStartup(list: List<AndroidStartup<*>>) = apply {
            list.forEach {
                addStartup(it)
            }
        }

        fun setConfig(config: StartupConfig?) = apply {
            mConfig = config
        }

        @Deprecated("Use setConfig() instead.")
        fun setLoggerLevel(level: LoggerLevel) = apply {
            mLoggerLevel = level
        }

        @Deprecated("Use setConfig() instead.")
        fun setAwaitTimeout(timeoutMilliSeconds: Long) = apply {
            mAwaitTimeout = timeoutMilliSeconds
        }

        private fun MutableMap<String, AndroidStartup<*>>.buildStartUp(allStartUpMap: Map<String, AndroidStartup<*>>, startup: AndroidStartup<*>) {
            startup.dependencyIds()
                    ?.forEach { dependencyId ->
                        val dependencyStartup = allStartUpMap[dependencyId]
                                ?: throw IllegalStateException("can not find dependency which id is $dependencyId,please check it in ${startup::class.qualifiedName}.")

                        if (!containsKey(dependencyId)) {
                            this[dependencyId] = dependencyStartup
                        }
                    }
            this[startup.id] = startup
        }

        fun build(context: Context): StartupManager {
            val realStartupMap = mutableMapOf<String, AndroidStartup<*>>()

            mStartupMap.values.forEach {
                val process = it::class.java.getAnnotation(MultipleProcess::class.java)?.process
                        ?: arrayOf()

                if (process.isNullOrEmpty() || ProcessUtils.isMultipleProcess(context, process)) {
                    realStartupMap.buildStartUp(mStartupMap, it)

                    if (it.waitOnMainThread() && !it.callCreateOnMainThread()) {
                        mNeedAwaitCount.incrementAndGet()
                    }
                }
            }

            return StartupManager(
                    context,
                    realStartupMap.values.toList(),
                    mNeedAwaitCount,
                    mConfig ?: StartupConfig.Builder()
                            .setLoggerLevel(mLoggerLevel)
                            .setAwaitTimeout(mAwaitTimeout)
                            .build()
            )
        }
    }

}