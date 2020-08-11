package com.rousetime.android_startup.model

import com.rousetime.android_startup.Startup

/**
 * Created by idisfkj on 2020/7/27.
 * Email: idisfkj@gmail.com.
 */
data class StartupSortStore(
    val result: MutableList<Startup<*>>,
    val startupMap: Map<String, Startup<*>>,
    val startupChildrenMap: Map<String, MutableList<String>>
)
