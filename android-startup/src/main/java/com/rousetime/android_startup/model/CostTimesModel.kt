package com.rousetime.android_startup.model

/**
 * Created by idisfkj on 2020/8/10.
 * Email: idisfkj@gmail.com.
 */
data class CostTimesModel(
    val name: String,
    val callOnMainThread: Boolean,
    val startTime: Long,
    var endTime: Long = 0L
)
