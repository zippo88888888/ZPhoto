package com.zp.zphoto.sample

import android.app.Application
import android.widget.ImageView
import com.zp.zphoto.BuildConfig
import com.zp.zphoto_lib.content.ZImageLoaderListener
import com.zp.zphoto_lib.common.ZPhotoHelp
import java.io.File

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        ZPhotoHelp.getInstance().init(this, MyImageLoaderListener())
    }

}