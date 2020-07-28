package com.rousetime.android_startup.provider

import android.content.ComponentName
import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.R
import com.rousetime.android_startup.StartupManager
import com.rousetime.android_startup.execption.StartupException
import com.rousetime.android_startup.model.LoggerLevel
import com.rousetime.android_startup.model.StartupProviderStore

/**
 * Created by idisfkj on 2020/7/28.
 * Email: idisfkj@gmail.com.
 */
class StartupProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        context.takeIf { context -> context != null }?.let {
            val store = discoverAndInitialize()
            StartupManager.Builder()
                .setAwaitTimeout(store.config?.getAwaitTimeout() ?: StartupManager.AWAIT_TIMEOUT)
                .setLoggerLevel(store.config?.getLoggerLevel() ?: LoggerLevel.NONE)
                .addAllStartup(store.result)
                .build(it)
                .start()
                .await()
        } ?: throw StartupException("Context cannot be null.")

        return true
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw IllegalStateException("Not allowed.")
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        throw IllegalStateException("Not allowed.")
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        throw IllegalStateException("Not allowed.")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        throw IllegalStateException("Not allowed.")
    }

    override fun getType(uri: Uri): String? {
        throw IllegalStateException("Not allowed.")
    }

    private fun discoverAndInitialize(): StartupProviderStore {
        val result = mutableListOf<AndroidStartup<*>>()
        var config: StartupProviderConfig? = null
        try {
            context?.let {
                val provider = ComponentName(it.packageName, this::class.java.name)
                val providerInfo = it.packageManager.getProviderInfo(provider, PackageManager.GET_META_DATA)
                val startup = it.getString(R.string.android_startup)
                val providerConfig = it.getString(R.string.android_startup_provider_config)
                providerInfo.metaData?.let { metaData ->
                    metaData.keySet().forEach { key ->
                        val value = metaData[key]
                        val clazz = Class.forName(key)
                        if (startup == value) {
                            if (AndroidStartup::class.java.isAssignableFrom(clazz)) {
                                result.add(clazz.getConstructor().newInstance() as AndroidStartup<*>)
                            }
                        } else if (providerConfig == value) {
                            if (StartupProviderConfig::class.java.isAssignableFrom(clazz)) {
                                config = clazz.getConstructor().newInstance() as StartupProviderConfig?
                            }
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            throw StartupException(t)
        }
        return StartupProviderStore(result, config)
    }
}