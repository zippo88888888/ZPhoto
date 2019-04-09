package com.zp.zphoto.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.zp.zphoto.R
import com.zp.zphoto_lib.util.ZLog
import com.zp.zphoto_lib.util.ZPhotoTask

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ZPhotoTask(this) {
            ZLog.e("包含相册数量：${it.size}")
            ZLog.i("======================================================================>>>")

            it.forEach { k, v ->
                ZLog.e("路径：$k")
                ZLog.e("包含图片数量：${v.size}")
                ZLog.i("======================================================================")
            }
        }.execute()
    }
}
