package com.rousetime.android_startup

import android.os.Looper
import com.rousetime.android_startup.executor.ExecutorManager
import com.rousetime.android_startup.model.LoggerLevel
import com.rousetime.android_startup.sort.TopologySort
import com.rousetime.android_startup.utils.StartupLogUtils
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by idisfkj on 2020/7/24.
 * Email : idisfkj@gmail.com.
 */
class StartupManager private constructor(builder: Builder) {

    private var startupList: List<AndroidStartup<*>>
    private var needAwaitCount: AtomicInteger? = null
    private var loggerLevel: LoggerLevel = LoggerLevel.NONE

    init {
        startupList = builder.startupList
        needAwaitCount = builder.needAwaitCount
        loggerLevel = builder.loggerLevel
        StartupLogUtils.level = loggerLevel
    }

    private var mAwaitCountDownLatch: CountDownLatch? = null
    private var mStartTime: Long = 0L

    companion object {
        private const val AWAIT_TIMEOUT = 10000L

        class Builder {

            var startupList = mutableListOf<AndroidStartup<*>>()
                private set
            var needAwaitCount = AtomicInteger()
                private set
            var loggerLevel = LoggerLevel.NONE
                private set

            fun addStartup(startup: AndroidStartup<*>) = apply {
                startupList.add(startup)
                if (startup.isNeedWait() && !startup.isOnMainThread()) {
                    needAwaitCount.incrementAndGet()
                }
            }

            fun setLoggerLevel(level: LoggerLevel) = apply {
                loggerLevel = level
            }

            fun build(): StartupManager {
                return StartupManager(this)
            }
        }
    }

    fun start() = apply {
        if (startupList.isNullOrEmpty()) {
            throw RuntimeException("Startup is empty, add at least one startup.")
        }

        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw RuntimeException("start method must be call in MainThread.")
        }

        if (mAwaitCountDownLatch != null) {
            throw RuntimeException("start method repeated call.")
        }

        mAwaitCountDownLatch = CountDownLatch(needAwaitCount?.get() ?: 0)
        TopologySort.sort(startupList).run {
            mStartTime = System.nanoTime()
            execute(this)
        }
    }

    private fun execute(list: List<AndroidStartup<*>>) {
        StartupLogUtils.d("execute start ${list.size}")
    }

    fun await() {
        if (mAwaitCountDownLatch == null) {
            throw RuntimeException("must be call start method before call await method.")
        }

        try {
            mAwaitCountDownLatch?.await(AWAIT_TIMEOUT, TimeUnit.MILLISECONDS)
            StartupLogUtils.d("totalTime: ${System.nanoTime() - mStartTime}")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}