package com.rousetime.sample.startup

import com.rousetime.android_startup.model.LoggerLevel
import com.rousetime.android_startup.provider.StartupProviderConfig

/**
 * Created by idisfkj on 2020/7/28.
 * Email: idisfkj@gmail.com.
 */
class SampleStartupProviderConfig : StartupProviderConfig {

    override fun getLoggerLevel(): LoggerLevel = LoggerLevel.DEBUG

    override fun getAwaitTimeout(): Long = 12000L
}