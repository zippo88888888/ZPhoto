package com.zp.zphoto_lib.ui.crop

import android.graphics.Bitmap
import android.graphics.Matrix

internal class RotateBitmap(bitmap: Bitmap?, rotation: Int) {

    var bitmap: Bitmap? = null
    var rotation = 0

    init {
        this.bitmap = bitmap
        this.rotation = rotation % 360
    }

    fun getRotateMatrix() = Matrix().apply {
        // By default this is an identity matrix
        if (bitmap != null && rotation != 0) {
            // We want to do the rotation at origin, but since the bounding
            // rectangle will be changed after rotation, so the delta values
            // are based on old & new width/height respectively.
            val cx = bitmap!!.width / 2
            val cy = bitmap!!.height / 2
            preTranslate((-cx).toFloat(), (-cy).toFloat())
            postRotate(rotation.toFloat())
            postTranslate((getWidth() / 2).toFloat(), (getHeight() / 2).toFloat())
        }
    }

    fun isOrientationChanged() = rotation / 90 % 2 != 0

    fun getHeight(): Int {
        if (bitmap == null) return 0
        return if (isOrientationChanged()) {
            bitmap!!.width
        } else {
            bitmap!!.height
        }
    }

    fun getWidth(): Int {
        if (bitmap == null) return 0
        return if (isOrientationChanged()) {
            bitmap!!.height
        } else {
            bitmap!!.width
        }
    }

    fun recycle() {
        if (bitmap != null) {
            bitmap!!.recycle()
            bitmap = null
        }
    }

}