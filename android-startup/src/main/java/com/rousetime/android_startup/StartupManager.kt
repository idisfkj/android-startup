package com.rousetime.android_startup

/**
 * Created by idisfkj on 2020/7/24.
 * Email : idisfkj@gmail.com.
 */
class StartupManager private constructor(private val startupList: List<AndroidStartup<*>>) {

    companion object {

        class Builder {

            private val mStartupList = mutableListOf<AndroidStartup<*>>()

            fun addStartup(startup: AndroidStartup<*>) = apply {
                mStartupList.add(startup)
            }

            fun build(): StartupManager {
                return StartupManager(mStartupList)
            }
        }
    }

    fun start() = apply {
        if (startupList.isNullOrEmpty()) {
            throw NullPointerException("startup list is empty!")
        }
    }

    fun await() {

    }
}