package com.zp.zphoto.sample.sample

import android.app.Application
import com.zp.zphoto_lib.common.ZPhotoHelp

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        ZPhotoHelp.getInstance().init(this, MyImageLoaderListener())
    }

}