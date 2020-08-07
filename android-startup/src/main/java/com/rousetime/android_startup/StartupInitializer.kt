package com.rousetime.android_startup

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.rousetime.android_startup.execption.StartupException
import com.rousetime.android_startup.model.StartupConfig
import com.rousetime.android_startup.model.StartupProviderStore
import com.rousetime.android_startup.provider.StartupProvider
import com.rousetime.android_startup.provider.StartupProviderConfig

/**
 * Created by idisfkj on 2020/8/7.
 * Email: idisfkj@gmail.com.
 */
class StartupInitializer {

    /**
     * Save initialized components result.
     */
    private val mInitializedComponents = mutableMapOf<Class<out Startup<*>>, Any?>()
    private var mInitializedConfig: StartupConfig? = null

    companion object {
        @JvmStatic
        val instance by lazy { StartupInitializer() }
    }

    internal fun discoverAndInitialize(context: Context): StartupProviderStore {
        val result = mutableListOf<AndroidStartup<*>>()
        val initialize = mutableListOf<Class<out Startup<*>>>()
        val initialized = mutableListOf<Class<out Startup<*>>>()
        var config: StartupProviderConfig? = null
        try {
            val provider = ComponentName(context.packageName, StartupProvider::class.java.name)
            val providerInfo = context.packageManager.getProviderInfo(provider, PackageManager.GET_META_DATA)
            val startup = context.getString(R.string.android_startup)
            val providerConfig = context.getString(R.string.android_startup_provider_config)
            providerInfo.metaData?.let { metaData ->
                metaData.keySet().forEach { key ->
                    val value = metaData[key]
                    val clazz = Class.forName(key)
                    if (startup == value) {
                        if (AndroidStartup::class.java.isAssignableFrom(clazz)) {
                            doInitialize((clazz.getDeclaredConstructor().newInstance() as AndroidStartup<*>), result, initialize, initialized)
                        }
                    } else if (providerConfig == value) {
                        if (StartupProviderConfig::class.java.isAssignableFrom(clazz)) {
                            config = clazz.getDeclaredConstructor().newInstance() as? StartupProviderConfig
                            // save initialized config
                            saveConfig(config?.getConfig())
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            throw StartupException(t)
        }
        return StartupProviderStore(result, config)
    }

    private fun doInitialize(
        startup: AndroidStartup<*>,
        result: MutableList<AndroidStartup<*>>,
        initialize: MutableList<Class<out Startup<*>>>,
        initialized: MutableList<Class<out Startup<*>>>
    ) {
        try {
            if (initialize.contains(startup::class.java)) {
                throw IllegalStateException("have circle dependencies.")
            }
            if (!initialized.contains(startup::class.java)) {
                initialize.add(startup::class.java)
                result.add(startup)
                startup.dependencies()?.forEach {
                    doInitialize(it.getDeclaredConstructor().newInstance() as AndroidStartup<*>, result, initialize, initialized)
                }
                initialize.remove(startup::class.java)
                initialized.add(startup::class.java)
            }
        } catch (t: Throwable) {
            throw StartupException(t)
        }
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

    /**
     * save initialized config.
     */
    internal fun saveConfig(config: StartupConfig?) {
        mInitializedConfig = config
    }
}