package com.rousetime.android_startup.model

import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.provider.StartupProviderConfig

/**
 * Created by idisfkj on 2020/7/28.
 * Email: idisfkj@gmail.com.
 */
internal data class StartupProviderStore(
    val result: List<AndroidStartup<*>>,
    val config: StartupProviderConfig?
)
