package com.rousetime.android_startup.provider

import com.rousetime.android_startup.model.LoggerLevel

/**
 * Created by idisfkj on 2020/7/28.
 * Email: idisfkj@gmail.com.
 */
interface StartupProviderConfig {

    fun getLoggerLevel(): LoggerLevel

    fun getAwaitTimeout(): Long
}