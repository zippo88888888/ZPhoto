package com.zp.zphoto_lib.content

import android.content.Context
import android.widget.ImageView
import com.zp.zphoto_lib.common.ZPhotoHelp
import java.io.File
import java.io.FilenameFilter
import java.util.ArrayList

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
    fun loadImg(imageView: ImageView, path: String)
    fun loadImg(imageView: ImageView, res: Int)
}

/**
 * 图片压缩
 */
open class ZImageCompressListener {
    /**
     * 压缩方法 如果有需要请自己实现
     */
    open fun getCompressList(arrayList: ArrayList<ZPhotoDetail>?, context: Context?) = arrayList
}

class ZPhotoFilenameFilter : FilenameFilter {

    override fun accept(filename: File, s: String): Boolean {
        val name = s.toLowerCase()
        val showGif = ZPhotoHelp.getInstance().getConfiguration().showGif
        if (showGif) return name.endsWith(JPEG) || name.endsWith(JPG)
                || name.endsWith(PNG) || name.endsWith(GIF)
        return name.endsWith(JPEG) || name.endsWith(JPG)
                || name.endsWith(PNG)
    }
}