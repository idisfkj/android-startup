package com.rousetime.android_startup.extensions

import com.rousetime.android_startup.Startup

/**
 * Created by idisfkj on 2020/8/10.
 * Email: idisfkj@gmail.com.
 */

const val DEFAULT_KEY = "com.rousetime.android_startup.defaultKey"

fun Class<out Startup<*>>.getUniqueKey(): String {
    val canonicalName = this.canonicalName
    requireNotNull(canonicalName) { "Local and anonymous classes can not be Startup" }
    return "$DEFAULT_KEY:$canonicalName"
}