package com.rousetime.android_startup.dispatcher

import com.rousetime.android_startup.Startup
import com.rousetime.android_startup.model.StartupSortStore

/**
 * Created by idisfkj on 2020/7/27.
 * Email: idisfkj@gmail.com.
 */
interface ManagerDispatcher {

    /**
     * dispatch prepare
     */
    fun prepare()

    /**
     * dispatch startup to executing.
     */
    fun dispatch(startup: Startup<*>, sortStore: StartupSortStore)

    /**
     * notify children when dependency startup completed.
     */
    fun notifyChildren(dependencyParent: Startup<*>, result: Any?, sortStore: StartupSortStore)
}