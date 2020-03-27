package com.zp.zphoto_lib.content

import android.net.Uri
import android.widget.ImageView
import java.io.File
import java.util.*

/**
 * 图片选择回调
 */
interface ZImageResultListener {

    fun selectSuccess(list: ArrayList<ZPhotoDetail>?)
    fun selectFailure()
    fun selectCancel()
}

/**
 * 图片加载 必须实现
 */
interface ZImageLoaderListener {
    fun loadImg(imageView: ImageView, file: File)
    fun loadImg(imageView: ImageView, uri: Uri?, file: File)
    fun loadImg(imageView: ImageView, path: String)
    fun loadImg(imageView: ImageView, res: Int)
}

