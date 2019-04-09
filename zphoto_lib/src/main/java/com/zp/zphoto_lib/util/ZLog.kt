package com.zp.zphoto_lib.util

import android.util.Log
import com.zp.zphoto_lib.common.ZPhotoHelp
import com.zp.zphoto_lib.content.getTextValue

object ZLog {

    private const val TAG = "ZPhotoLib"
    private const val I = 1
    private const val D = 2
    private const val E = 3
    private const val V = 4

    fun i(tag: String, message: Any) {
        log(I, tag, message)
    }

    fun d(tag: String, message: Any) {
        log(D, tag, message)
    }

    fun e(tag: String, message: Any) {
        log(E, tag, message)
    }

    fun v(tag: String, message: Any) {
        log(V, tag, message)
    }

    fun i(msg: Any) {
        log(I, TAG, msg)
    }

    fun d(msg: Any) {
        log(D, TAG, msg)
    }

    fun e(msg: Any) {
        log(E, TAG, msg)
    }

    fun v(msg: Any) {
        log(V, TAG, msg)
    }

    private fun log(type: Int, TAG: String, msg: Any) {
        val value = getTextValue(msg)
        if (ZPhotoHelp.getInstance().isDebug()) {
            when (type) {
                D -> Log.d(TAG, value)
                E -> Log.e(TAG, value)
                I -> Log.i(TAG, value)
                V -> Log.v(TAG, value)
            }
        }
    }

}