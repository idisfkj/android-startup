package com.rousetime.android_startup.annotation

/**
 * Created by idisfkj on 2020/9/14.
 * Email: idisfkj@gmail.com.
 */
@MustBeDocumented
@Retention
@Target(AnnotationTarget.CLASS)
annotation class MultipleProcess(vararg val process: String)
