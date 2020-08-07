package com.rousetime.android_startup.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.rousetime.android_startup.StartupInitializer
import com.rousetime.android_startup.StartupManager
import com.rousetime.android_startup.execption.StartupException
import com.rousetime.android_startup.model.StartupConfig

/**
 * Created by idisfkj on 2020/7/28.
 * Email: idisfkj@gmail.com.
 */
class StartupProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        context.takeIf { context -> context != null }?.let {
            val store = StartupInitializer.instance.discoverAndInitialize(it)
            StartupManager.Builder()
                .setConfig(store.config?.getConfig() ?: StartupConfig.Builder().build())
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
}