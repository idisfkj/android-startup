package com.rousetime.android_startup

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.os.TraceCompat
import com.rousetime.android_startup.execption.StartupException
import com.rousetime.android_startup.extensions.getUniqueKey
import com.rousetime.android_startup.manager.StartupCacheManager
import com.rousetime.android_startup.model.StartupProviderStore
import com.rousetime.android_startup.provider.StartupProviderConfig

/**
 * Created by idisfkj on 2020/8/7.
 * Email: idisfkj@gmail.com.
 */
class StartupInitializer {

    companion object {
        @JvmStatic
        val instance by lazy { StartupInitializer() }
    }

    internal fun discoverAndInitialize(context: Context, providerName: String): StartupProviderStore {

        TraceCompat.beginSection(StartupInitializer::class.java.simpleName)

        val result = mutableListOf<AndroidStartup<*>>()
        val initialize = mutableListOf<String>()
        val initialized = mutableListOf<String>()
        var config: StartupProviderConfig? = null
        try {
            val provider = ComponentName(context.packageName, providerName)
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
                            StartupCacheManager.instance.saveConfig(config?.getConfig())
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            throw StartupException(t)
        }

        TraceCompat.endSection()

        return StartupProviderStore(result, config)
    }

    private fun doInitialize(
        startup: AndroidStartup<*>,
        result: MutableList<AndroidStartup<*>>,
        initialize: MutableList<String>,
        initialized: MutableList<String>
    ) {
        try {
            val uniqueKey = startup::class.java.getUniqueKey()
            if (initialize.contains(uniqueKey)) {
                throw IllegalStateException("have circle dependencies.")
            }
            if (!initialized.contains(uniqueKey)) {
                initialize.add(uniqueKey)
                result.add(startup)
                if (startup.dependenciesByName().isNullOrEmpty()) {
                    startup.dependencies()?.forEach {
                        doInitialize(it.getDeclaredConstructor().newInstance() as AndroidStartup<*>, result, initialize, initialized)
                    }
                } else {
                    startup.dependenciesByName()?.forEach {
                        val clazz = Class.forName(it)
                        doInitialize(clazz.getDeclaredConstructor().newInstance() as AndroidStartup<*>, result, initialize, initialized)
                    }
                }
                initialize.remove(uniqueKey)
                initialized.add(uniqueKey)
            }
        } catch (t: Throwable) {
            throw StartupException(t)
        }
    }

}