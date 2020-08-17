package com.rousetime.android_startup.manager

import com.rousetime.android_startup.Startup
import com.rousetime.android_startup.model.StartupConfig

/**
 * Created by idisfkj on 2020/8/11.
 * Email: idisfkj@gmail.com.
 */
class StartupCacheManager {

    /**
     * Save initialized components result.
     */
    private val mInitializedComponents = mutableMapOf<Class<out Startup<*>>, Any?>()
    var initializedConfig: StartupConfig? = null
        private set

    companion object {
        @JvmStatic
        val instance by lazy { StartupCacheManager() }
    }

    /**
     * save result of initialized component.
     */
    internal fun saveInitializedComponent(zClass: Class<out Startup<*>>, result: Any?) {
        mInitializedComponents[zClass] = result
    }

    /**
     * check initialized.
     */
    fun hadInitialized(zClass: Class<out Startup<*>>): Boolean = mInitializedComponents.contains(zClass)

    @Suppress("UNCHECKED_CAST")
    fun <T> obtainInitializedResult(zClass: Class<out Startup<*>>): T? = mInitializedComponents[zClass] as T?

    fun remove(zClass: Class<out Startup<*>>) {
        mInitializedComponents.remove(zClass)
    }

    fun clear() {
        mInitializedComponents.clear()
    }

    /**
     * save initialized config.
     */
    internal fun saveConfig(config: StartupConfig?) {
        initializedConfig = config
    }
}