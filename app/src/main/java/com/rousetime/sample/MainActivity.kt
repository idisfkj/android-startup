package com.rousetime.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.rousetime.sample.startup.SampleStartupProviderConfig
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SampleStartupProviderConfig.costTimesLiveData.observe(this, Observer {
            content.text = buildString {
                append("Startup Completed: ")
                append("\n")
                append("\n")
                it.forEach {
                    append("\n")
                    append("Startup Name: ${it.name}")
                    append("\n")
                    append("onMainThread: ${it.callOnMainThread}")
                    append("\n")
                    append("Cost times: ${it.endTime - it.startTime} ms")
                    append("\n")
                }
            }
        })
    }
}