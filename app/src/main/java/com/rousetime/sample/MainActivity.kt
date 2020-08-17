package com.rousetime.sample

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.rousetime.android_startup.StartupManager
import com.rousetime.android_startup.manager.StartupCacheManager
import com.rousetime.sample.startup.SampleFirstStartup
import com.rousetime.sample.startup.SampleSecondStartup
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        get.setOnClickListener {
            if (StartupCacheManager.instance.hadInitialized(SampleSecondStartup::class.java)) {
                content.text = getString(
                    R.string.sample_second_startup_result_from_cache,
                    StartupCacheManager.instance.obtainInitializedResult<Boolean>(SampleSecondStartup::class.java)
                )
            } else {
                // show initialize tips
                content.text = getString(R.string.sample_second_startup_not_initialized)

                // because SampleSecondStartup need to block on main thread.
                // in order to show initialize tips,to delay a frame times.
                Handler().postDelayed({
                    StartupManager.Builder()
                        .setConfig(StartupCacheManager.instance.initializedConfig)
                        .addStartup(SampleFirstStartup())
                        .addStartup(SampleSecondStartup())
                        .build(this)
                        .start()
                        .await()
                }, 16)
            }
        }

        clear.setOnClickListener {
            StartupCacheManager.instance.remove(SampleSecondStartup::class.java)
            content.text = getString(R.string.clear_cache_success)
        }

        SampleApplication.costTimesLiveData.observe(this, Observer {
            content.text = buildString {
                append("Startup Completed: ")
                append("\n")
                append("\n")
                it.forEach {
                    append("\n")
                    append("Startup Name: ${it.name}")
                    append("\n")
                    append("CallOnMainThread: ${it.callOnMainThread}")
                    append("\n")
                    append("WaitOnMainThread: ${it.waitOnMainThread}")
                    append("\n")
                    append("Cost times: ${it.endTime - it.startTime} ms")
                    append("\n")
                }
            }
        })
    }
}