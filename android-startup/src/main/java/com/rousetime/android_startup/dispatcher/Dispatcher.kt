package com.rousetime.android_startup.dispatcher

/**
 * Created by idisfkj on 2020/7/27.
 * Email: idisfkj@gmail.com.
 */
interface Dispatcher {

    /**
     * Return true call the create function on main thread otherwise false.
     */
    fun callCreateOnMainThread(): Boolean

    /**
     * Return true block the main thread until the startup completed otherwise false.
     */
    fun waitOnMainThread(): Boolean

    /**
     * To wait dependencies startup completed.
     */
    fun toWait()

    /**
     * To notify the startup when dependencies startup completed.
     */
    fun toNotify()
}