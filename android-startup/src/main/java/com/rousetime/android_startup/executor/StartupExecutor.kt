package com.rousetime.android_startup.executor

import java.util.concurrent.Executor

/**
 * Created by idisfkj on 2020/7/23.
 * Email: idisfkj@gmail.com.
 */
interface StartupExecutor {

    fun createExecutor(): Executor
}