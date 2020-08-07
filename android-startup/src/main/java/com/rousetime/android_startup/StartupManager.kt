package com.rousetime.android_startup

import android.content.Context
import android.os.Looper
import com.rousetime.android_startup.dispatcher.ManagerDispatcher
import com.rousetime.android_startup.execption.StartupException
import com.rousetime.android_startup.model.LoggerLevel
import com.rousetime.android_startup.model.StartupConfig
import com.rousetime.android_startup.model.StartupSortStore
import com.rousetime.android_startup.run.StartupRunnable
import com.rousetime.android_startup.sort.TopologySort
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
    private var mStartTime: Long = 0L

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

        fun setConfig(config: StartupConfig) = apply {
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
        StartupInitializer.instance.saveConfig(config)
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

        mAwaitCountDownLatch = CountDownLatch(needAwaitCount.get())
        TopologySort.sort(startupList).run {
            mStartTime = System.nanoTime()
            execute(this)
        }
    }

    private fun execute(sortStore: StartupSortStore) {
        StartupLogUtils.d("execute start: size of ${sortStore.mainSortResult.size + sortStore.ioSortResult.size}")

        sortStore.ioSortResult.forEach { mDefaultManagerDispatcher.dispatch(it, sortStore, false) }

        sortStore.mainSortResult.forEach { mDefaultManagerDispatcher.dispatch(it, sortStore, true) }
    }

    /**
     * When dependencyParent startup completed, to notify children
     */
    private val mDefaultManagerDispatcher by lazy {
        object : ManagerDispatcher {

            override fun dispatch(startup: AndroidStartup<*>, sortStore: StartupSortStore, mainThread: Boolean) {
                StartupLogUtils.d("${startup::class.java.simpleName} being executing, onMainThread $mainThread.")
                if (StartupInitializer.instance.hadInitialized(startup::class.java)) {
                    val result = StartupInitializer.instance.obtainInitializedResult<Any>(startup::class.java)
                    StartupLogUtils.d("Startup ${startup::class.java.simpleName} was completed, result from cache.")
                    notifyChildren(startup, result, sortStore)
                } else {
                    val runnable = StartupRunnable(context, startup, sortStore, this)
                    if (!mainThread) {
                        startup.createExecutor().execute(runnable)
                    } else {
                        runnable.run()
                    }
                }
            }

            override fun notifyChildren(dependencyParent: Startup<*>, result: Any?, sortStore: StartupSortStore) {
                // immediately notify main thread,Unblock the main thread.
                if (dependencyParent.waitOnMainThread()) {
                    needAwaitCount.incrementAndGet()
                    mAwaitCountDownLatch?.countDown()
                }

                sortStore.clazzChildrenMap[dependencyParent::class.java]?.forEach {
                    StartupLogUtils.d("notifyChildren => parent ${dependencyParent::class.java.simpleName} to notify children ${it.simpleName}")

                    sortStore.clazzMap[it]?.run {
                        onDependenciesCompleted(dependencyParent, result)
                        toNotify()
                    }
                }
            }

        }
    }

    /**
     * to await startup completed
     * block main thread.
     */
    fun await() {
        if (mAwaitCountDownLatch == null) {
            throw StartupException("must be call start method before call await method.")
        }

        try {
            mAwaitCountDownLatch?.await(config.awaitTimeout, TimeUnit.MILLISECONDS)

            StartupLogUtils.d("mainThread cost totalTime: ${(System.nanoTime() - mStartTime) / 1000L / 1000L}")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}