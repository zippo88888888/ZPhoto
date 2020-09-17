package com.zp.zphoto.sample.sample

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("ZPhotoLib","FragmentActivity  onActivityResult")
    }
}
