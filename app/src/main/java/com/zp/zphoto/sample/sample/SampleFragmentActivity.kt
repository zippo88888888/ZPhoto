package com.zp.zphoto.sample.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zp.zphoto.R

class SampleFragmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample_fragment)

        supportFragmentManager
            .beginTransaction()
            .add(R.id.sample_fragment_rootLayout, BlankFragment(), BlankFragment::class.java.simpleName)
            .commit()
    }
}
