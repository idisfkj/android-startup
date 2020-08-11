package com.rousetime.android_startup.sort

import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.Startup
import com.rousetime.android_startup.execption.StartupException
import com.rousetime.android_startup.model.StartupSortStore
import com.rousetime.android_startup.utils.StartupLogUtils
import java.util.*

/**
 * Created by idisfkj on 2020/7/24.
 * Email: idisfkj@gmail.com.
 */
internal object TopologySort {

    fun sort(startupList: List<AndroidStartup<*>>): StartupSortStore {

        val mainResult = mutableListOf<AndroidStartup<*>>()
        val ioResult = mutableListOf<AndroidStartup<*>>()
        val temp = mutableListOf<AndroidStartup<*>>()
        val clazzMap = hashMapOf<Class<out Startup<*>>, AndroidStartup<*>>()
        val zeroDeque = ArrayDeque<Class<out Startup<*>>>()
        val clazzChildrenMap = hashMapOf<Class<out Startup<*>>, MutableList<Class<out Startup<*>>>>()
        val inDegreeMap = hashMapOf<Class<out Startup<*>>, Int>()

        startupList.forEach {
            if (!clazzMap.containsKey(it.javaClass)) {
                clazzMap[it.javaClass] = it
                // save in-degree
                inDegreeMap[it.javaClass] = it.dependencies()?.size ?: 0
                if (it.dependencies().isNullOrEmpty()) {
                    zeroDeque.offer(it.javaClass)
                } else {
                    // add key parent, value list children
                    it.dependencies()?.forEach { parent ->
                        if (clazzChildrenMap[parent] == null) {
                            clazzChildrenMap[parent] = arrayListOf()
                        }
                        clazzChildrenMap[parent]?.add(it.javaClass)
                    }
                }
            } else {
                throw StartupException("$it multiple add.")
            }
        }

        while (!zeroDeque.isEmpty()) {
            zeroDeque.poll()?.let {
                clazzMap[it]?.let { androidStartup ->
                    temp.add(androidStartup)
                    // add zero in-degree to result list
                    if (androidStartup.callCreateOnMainThread()) {
                        mainResult.add(androidStartup)
                    } else {
                        ioResult.add(androidStartup)
                    }
                }
                clazzChildrenMap[it]?.forEach { children ->
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

        val result = mutableListOf<AndroidStartup<*>>().apply {
            addAll(ioResult)
            addAll(mainResult)
        }
        printResult(temp)

        return StartupSortStore(
            result,
            clazzMap,
            clazzChildrenMap
        )
    }

    private fun printResult(result: List<AndroidStartup<*>>) {
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