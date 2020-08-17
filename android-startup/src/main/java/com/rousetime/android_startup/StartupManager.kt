package com.rousetime.android_startup

import android.content.Context
import android.os.Looper
import android.os.SystemClock
import androidx.core.os.TraceCompat
import com.rousetime.android_startup.dispatcher.StartupManagerDispatcher
import com.rousetime.android_startup.execption.StartupException
import com.rousetime.android_startup.manager.StartupCacheManager
import com.rousetime.android_startup.model.LoggerLevel
import com.rousetime.android_startup.model.StartupConfig
import com.rousetime.android_startup.model.StartupSortStore
import com.rousetime.android_startup.sort.TopologySort
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

    class Builder {

        private var mStartupList = mutableListOf<AndroidStartup<*>>()
        private var mNeedAwaitCount = AtomicInteger()
        private var mLoggerLevel = LoggerLevel.NONE
        private var mAwaitTimeout = AWAIT_TIMEOUT
        private var mConfig: StartupConfig? = null

        fun addStartup(startup: AndroidStartup<*>) = apply {
            mStartupList.add(startup)
            if (startup.waitOnMainThread() && !startup.callCreateOnMainThread()) {
                mNeedAwaitCount.incrementAndGet()
            }
        }

        fun addAllStartup(list: List<AndroidStartup<*>>) = apply {
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

        fun build(context: Context): StartupManager {
            return StartupManager(
                context,
                mStartupList,
                mNeedAwaitCount,
                mConfig ?: StartupConfig.Builder()
                    .setLoggerLevel(mLoggerLevel)
                    .setAwaitTimeout(mAwaitTimeout)
                    .build()
            )
        }
    }

    init {
        // save initialized config
        StartupCacheManager.instance.saveConfig(config)
        StartupLogUtils.level = config.loggerLevel
    }

    fun start() = apply {
        if (startupList.isNullOrEmpty()) {
            throw StartupException("Startup is empty, add at least one startup.")
        }

        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw StartupException("start method must be call in MainThread.")
        }

        if (mAwaitCountDownLatch != null) {
            throw StartupException("start method repeated call.")
        }

        TraceCompat.beginSection(StartupManager::class.java.simpleName)
        StartupCostTimesUtils.startTime = System.nanoTime()

        mAwaitCountDownLatch = CountDownLatch(needAwaitCount.get())
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
}