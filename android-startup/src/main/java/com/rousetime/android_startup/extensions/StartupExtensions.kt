package com.rousetime.android_startup.extensions

import com.rousetime.android_startup.Startup

/**
 * Created by idisfkj on 2020/8/10.
 * Email: idisfkj@gmail.com.
 */

private const val DEFAULT_KEY = "com.rousetime.android_startup.defaultKey"

internal fun Class<out Startup<*>>.getUniqueKey(): String {
    return "$DEFAULT_KEY:$name"
}

internal fun String.getUniqueKey(): String = "$DEFAULT_KEY:$this"