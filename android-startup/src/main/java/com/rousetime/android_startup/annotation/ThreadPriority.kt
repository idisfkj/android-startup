package com.rousetime.android_startup.annotation

import android.os.Process

/**
 * Created by idisfkj on 2020/9/16.
 * Email: idisfkj@gmail.com.
 */
@MustBeDocumented
@Retention
@Target(AnnotationTarget.CLASS)
annotation class ThreadPriority(val priority: Int = Process.THREAD_PRIORITY_DEFAULT)
