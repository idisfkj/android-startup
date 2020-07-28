package com.rousetime.android_startup.sort

import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.Startup
import com.rousetime.android_startup.model.StartupSortStore
import com.rousetime.android_startup.utils.StartupLogUtils
import java.util.*

/**
 * Created by idisfkj on 2020/7/24.
 * Email: idisfkj@gmail.com.
 */
object TopologySort {

    fun sort(startupList: List<AndroidStartup<*>>): StartupSortStore {

        val mainResult = mutableListOf<AndroidStartup<*>>()
        val ioResult = mutableListOf<AndroidStartup<*>>()
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
                throw RuntimeException("$it multiple add.")
            }
        }

        while (!zeroDeque.isEmpty()) {
            zeroDeque.poll()?.let {
                clazzMap[it]?.let { androidStartup ->
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
            throw RuntimeException("have circle dependencies.")
        }

        printResult(mutableListOf<AndroidStartup<*>>().apply {
            addAll(mainResult)
            addAll(ioResult)
        })

        return StartupSortStore(
            mainResult,
            ioResult,
            clazzMap,
            clazzChildrenMap
        )
    }

    private fun printResult(result: List<AndroidStartup<*>>) {
        val printBuilder = buildString {
            result.forEach {
                this.append("\n")
                    .append("Class: ${it::class.java.simpleName}")
                    .append(" =>")
                    .append(" Dependencies size: ${it.dependencies()?.size ?: 0}")
                    .append(" MainThread: ${it.callCreateOnMainThread()}")
                    .append(" NeedWait: ${it.waitOnMainThread()}")
                    .append("\n")
            }
        }
        StartupLogUtils.d("TopologySort result: $printBuilder")
    }
}