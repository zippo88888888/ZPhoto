package com.zp.zphoto.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.zp.zphoto.R
import com.zp.zphoto_lib.content.jumpActivity
import com.zp.zphoto_lib.ui.ZPhotoSelectActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_photoBtn.setOnClickListener {
            jumpActivity(ZPhotoSelectActivity::class.java)
        }
    }
}
