package com.rousetime.sample

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by idisfkj on 2020/8/12.
 * Email: idisfkj@gmail.com.
 */
class SampleMoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more)
    }

    fun onClick(view: View) {
        goToCommonActivity(view.id)
    }

    private fun goToCommonActivity(id: Int) {
        startActivity(Intent(this, SampleCommonActivity::class.java).apply {
            putExtra("id", id)
        })
    }
}