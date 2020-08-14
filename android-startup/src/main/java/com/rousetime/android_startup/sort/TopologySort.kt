package com.rousetime.android_startup.sort

import androidx.core.os.TraceCompat
import com.rousetime.android_startup.Startup
import com.rousetime.android_startup.execption.StartupException
import com.rousetime.android_startup.extensions.getUniqueKey
import com.rousetime.android_startup.model.StartupSortStore
import com.rousetime.android_startup.utils.StartupLogUtils
import java.util.*

/**
 * Created by idisfkj on 2020/7/24.
 * Email: idisfkj@gmail.com.
 */
internal object TopologySort {

    fun sort(startupList: List<Startup<*>>): StartupSortStore {
        TraceCompat.beginSection(TopologySort::class.java.simpleName)

        val mainResult = mutableListOf<Startup<*>>()
        val ioResult = mutableListOf<Startup<*>>()
        val temp = mutableListOf<Startup<*>>()
        val startupMap = hashMapOf<String, Startup<*>>()
        val zeroDeque = ArrayDeque<String>()
        val startupChildrenMap = hashMapOf<String, MutableList<String>>()
        val inDegreeMap = hashMapOf<String, Int>()

        startupList.forEach {
            val uniqueKey = it::class.java.getUniqueKey()
            if (!startupMap.containsKey(uniqueKey)) {
                startupMap[uniqueKey] = it
                // save in-degree
                inDegreeMap[uniqueKey] = it.dependencies()?.size ?: 0
                if (it.dependencies().isNullOrEmpty()) {
                    zeroDeque.offer(uniqueKey)
                } else {
                    // add key parent, value list children
                    it.dependencies()?.forEach { parent ->
                        val parentUniqueKey = parent.getUniqueKey()
                        if (startupChildrenMap[parentUniqueKey] == null) {
                            startupChildrenMap[parentUniqueKey] = arrayListOf()
                        }
                        startupChildrenMap[parentUniqueKey]?.add(uniqueKey)
                    }
                }
            } else {
                throw StartupException("$it multiple add.")
            }
        }

        while (!zeroDeque.isEmpty()) {
            zeroDeque.poll()?.let {
                startupMap[it]?.let { androidStartup ->
                    temp.add(androidStartup)
                    // add zero in-degree to result list
                    if (androidStartup.callCreateOnMainThread()) {
                        mainResult.add(androidStartup)
                    } else {
                        ioResult.add(androidStartup)
                    }
                }
                startupChildrenMap[it]?.forEach { children ->
                    inDegreeMap[children] = inDegreeMap[children]?.minus(1) ?: 0
                    // add zero in-degree to deque
                    if (inDegreeMap[children] == 0) {
                        zeroDeque.offer(children)
                    }
                }
            }
        }

        if (mainResult.size + ioResult.size != startupList.size) {
            throw StartupException("lack of dependencies or have circle dependencies.")
        }

        val result = mutableListOf<Startup<*>>().apply {
            addAll(ioResult)
            addAll(mainResult)
        }
        printResult(temp)

        TraceCompat.endSection()

        return StartupSortStore(
            result,
            startupMap,
            startupChildrenMap
        )
    }

    private fun printResult(result: List<Startup<*>>) {
        val printBuilder = buildString {
            append("TopologySort result: ")
            append("\n")
            append("|================================================================")
            result.forEachIndexed { index, it ->
                append("\n")
                append("|         order          |    [${index + 1}] ")
                append("\n")
                append("|----------------------------------------------------------------")
                append("\n")
                append("|        Startup         |    ${it::class.java.simpleName}")
                append("\n")
                append("|----------------------------------------------------------------")
                append("\n")
                append("|   Dependencies size    |    ${it.dependencies()?.size ?: 0}")
                append("\n")
                append("|----------------------------------------------------------------")
                append("\n")
                append("| callCreateOnMainThread |    ${it.callCreateOnMainThread()}")
                append("\n")
                append("|----------------------------------------------------------------")
                append("\n")
                append("|    waitOnMainThread    |    ${it.waitOnMainThread()}")
                append("\n")
                append("|================================================================")
            }
        }
        StartupLogUtils.d(printBuilder)
    }
}