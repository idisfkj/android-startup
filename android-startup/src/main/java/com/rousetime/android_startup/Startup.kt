package com.rousetime.android_startup

import android.content.Context

/**
 * Created by idisfkj on 2020/7/23.
 * Email: idisfkj@gmail.com.
 */
interface Startup<T> {

    fun create(context: Context): T

    fun dependencies(): List<Class<out Startup<*>>>?

}