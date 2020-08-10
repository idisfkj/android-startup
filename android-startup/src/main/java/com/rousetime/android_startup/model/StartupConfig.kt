package com.rousetime.android_startup.model

import com.rousetime.android_startup.StartupListener

/**
 * Created by idisfkj on 2020/7/31.
 * Email: idisfkj@gmail.com.
 */
class StartupConfig private constructor(
    val loggerLevel: LoggerLevel,
    val awaitTimeout: Long,
    val listener: StartupListener?
) {

    class Builder {
        private var mLoggerLevel: LoggerLevel? = null
        private var mAwaitTimeout: Long? = null
        private var mListener: StartupListener? = null

        companion object {
            const val AWAIT_TIMEOUT = 10000L
        }

        fun setLoggerLevel(level: LoggerLevel) = apply {
            mLoggerLevel = level
        }

        fun setAwaitTimeout(timeoutMilliSeconds: Long) = apply {
            mAwaitTimeout = timeoutMilliSeconds
        }

        fun setListener(listener: StartupListener) = apply {
            mListener = listener
        }

        fun build(): StartupConfig {
            return StartupConfig(
                mLoggerLevel ?: LoggerLevel.NONE,
                mAwaitTimeout ?: AWAIT_TIMEOUT,
                mListener
            )
        }
    }

}