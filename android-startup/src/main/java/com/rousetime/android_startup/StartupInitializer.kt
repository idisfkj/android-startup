package com.rousetime.android_startup

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.os.TraceCompat
import com.rousetime.android_startup.execption.StartupException
import com.rousetime.android_startup.manager.StartupCacheManager
import com.rousetime.android_startup.model.StartupProviderStore
import com.rousetime.android_startup.provider.StartupProviderConfig

/**
 * Created by idisfkj on 2020/8/7.
 * Email: idisfkj@gmail.com.
 */
internal class StartupInitializer {

    companion object {
        @JvmStatic
        val instance by lazy { StartupInitializer() }
    }

    internal fun discover(context: Context, providerName: String): StartupProviderStore {

        TraceCompat.beginSection(StartupInitializer::class.java.simpleName)

        val result = mutableListOf<AndroidStartup<*>>()

        val config = doDiscover(context, providerName, result)

        TraceCompat.endSection()

        return StartupProviderStore(result, config)
    }

    private fun doDiscover(context: Context, providerName: String, result: MutableList<AndroidStartup<*>>): StartupProviderConfig? {

        var config: StartupProviderConfig? = null

        try {
            val provider = ComponentName(context.packageName, providerName)
            val providerInfo = context.packageManager.getProviderInfo(provider, PackageManager.GET_META_DATA)
            val startupKey = context.getString(R.string.android_startup)
            val providerConfigKey = context.getString(R.string.android_startup_provider_config)
            val metaData = providerInfo.metaData ?: return config

            metaData.keySet().distinct().forEach { key ->
                val value = metaData[key]
                val clazz = Class.forName(key)
                if (startupKey == value) {
                    if (AndroidStartup::class.java.isAssignableFrom(clazz)) {
                        val startup = clazz.getDeclaredConstructor().newInstance() as AndroidStartup<*>

                        result.add(startup)
                    }
                } else if (providerConfigKey == value) {
                    if (StartupProviderConfig::class.java.isAssignableFrom(clazz)) {
                        config = clazz.getDeclaredConstructor().newInstance() as? StartupProviderConfig
                        // save initialized config
                        StartupCacheManager.instance.saveConfig(config?.getConfig())
                    }
                }
            }
        } catch (t: Throwable) {
            throw StartupException(t)
        }

        return config
    }
}