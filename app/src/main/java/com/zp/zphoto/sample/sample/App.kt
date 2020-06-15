package com.zp.zphoto.sample.sample

import android.app.Application
import android.content.Context
import android.graphics.Point
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.zp.zphoto_lib.common.ZPhotoHelp

class App : Application() {

    companion object {
        var application: Application? = null
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        ZPhotoHelp.getInstance().init(this, MyImageLoaderListener())
    }

}

fun Context.getDisplay() = IntArray(2).apply {
    val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val point = Point()
    manager.defaultDisplay.getSize(point)
    this[0] = point.x
    this[1] = point.y
}

fun getAppContext() = App.application!!

fun dip2pxF(dpValue: Float) = dpValue * getAppContext().resources.displayMetrics.density + 0.5f
fun dip2px(dpValue: Float) = dip2pxF(dpValue).toInt()
fun px2dipF(pxValue: Float) = pxValue / getAppContext().resources.displayMetrics.density + 0.5f
fun px2dip(pxValue: Float) = px2dipF(pxValue).toInt()

fun getColorById(colorID: Int) = ContextCompat.getColor(getAppContext(), colorID)
fun getDimenById(dimenID: Int) = getAppContext().resources.getDimension(dimenID)
fun getStringById(stringID: Int) = getAppContext().resources.getString(stringID)