package com.rousetime.android_startup

import com.rousetime.android_startup.model.CostTimesModel

/**
 * Created by idisfkj on 2020/8/10.
 * Email: idisfkj@gmail.com.
 */
interface StartupListener {

    /**
     * call when all startup completed.
     * @param totalMainThreadCostTime cost times of main thread.
     * @param costTimesModels list of cost times for every startup.
     */
    fun onCompleted(totalMainThreadCostTime: Long, costTimesModels: List<CostTimesModel>)
}