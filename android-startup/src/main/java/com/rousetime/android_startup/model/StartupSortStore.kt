package com.rousetime.android_startup.model

import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.Startup

/**
 * Created by idisfkj on 2020/7/27.
 * Email: idisfkj@gmail.com.
 */
data class StartupSortStore(
    val result: MutableList<AndroidStartup<*>>,
    val clazzMap: Map<Class<out Startup<*>>, AndroidStartup<*>>,
    val clazzChildrenMap: Map<Class<out Startup<*>>, MutableList<Class<out Startup<*>>>>
)
