package com.rousetime.android_startup.sort

import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.Startup
import com.rousetime.android_startup.utils.StartupLogUtils
import java.util.*

/**
 * Created by idisfkj on 2020/7/24.
 * Email: idisfkj@gmail.com.
 */
object TopologySort {

    fun sort(startupList: List<AndroidStartup<*>>): List<AndroidStartup<*>> {

        val result = mutableListOf<AndroidStartup<*>>()
        val startupMap = hashMapOf<Class<out Startup<*>>, AndroidStartup<*>>()
        val zeroDeque = ArrayDeque<Class<out Startup<*>>>()
        val childrenStartupMap = hashMapOf<Class<out Startup<*>>, MutableList<Class<out Startup<*>>>>()
        val inDegreeMap = hashMapOf<Class<out Startup<*>>, Int>()

        startupList.forEach {
            if (!startupMap.containsKey(it.javaClass)) {
                startupMap[it.javaClass] = it
                // save in-degree
                inDegreeMap[it.javaClass] = it.dependencies()?.size ?: 0
                if (it.dependencies().isNullOrEmpty()) {
                    zeroDeque.offer(it.javaClass)
                } else {
                    // add key parent, value list children
                    it.dependencies()?.forEach { parent ->
                        if (childrenStartupMap[parent] == null) {
                            childrenStartupMap[parent] = arrayListOf()
                        }
                        childrenStartupMap[parent]?.add(it.javaClass)
                    }
                }
            } else {
                throw RuntimeException("$it multiple add.")
            }
        }

        while (!zeroDeque.isEmpty()) {
            zeroDeque.poll()?.let {
                startupMap[it]?.let { androidStartup ->
                    // add zero in-degree to result list
                    result.add(androidStartup)
                }
                childrenStartupMap[it]?.forEach { children ->
                    inDegreeMap[children] = inDegreeMap[children]?.minus(1) ?: 0
                    // add zero in-degree to deque
                    if (inDegreeMap[children] == 0) {
                        zeroDeque.offer(children)
                    }
                }
            }
        }

        if (result.size != startupList.size) {
            throw RuntimeException("have circle dependencies.")
        }
        printResult(result)
        return result
    }

    private fun printResult(result: List<AndroidStartup<*>>) {
        val printBuilder = buildString {
            result.forEach {
                this.append("\n")
                    .append("Class: ${it::class.java.simpleName}")
                    .append(" => ")
                    .append("Dependencies size: ${it.dependencies()?.size ?: 0}")
                    .append("MainThread: ${it.isOnMainThread()}")
                    .append("NeedWait: ${it.isNeedWait()}")
                    .append("\n")
            }
        }
        StartupLogUtils.d("TopologySort result: $printBuilder")
    }
}