package com.rousetime.android_startup.dispatcher

/**
 * Created by idisfkj on 2020/7/27.
 * Email: idisfkj@gmail.com.
 */
interface Dispatcher {

    fun isOnMainThread(): Boolean

    fun isNeedWait(): Boolean

    fun toWait()

    fun toNotify()
}