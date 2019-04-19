package com.zp.zphoto_lib.content

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import android.os.Message
import android.widget.ImageView
import com.zp.zphoto_lib.R
import com.zp.zphoto_lib.common.ZPhotoHelp
import com.zp.zphoto_lib.util.ZToaster
import java.io.File
import java.io.FilenameFilter
import java.lang.ref.SoftReference
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
 * 图片剪裁
 */
open class ZImageClipping {

    open fun clipping(
        images: ArrayList<ZPhotoDetail>?,
        activity: Activity,
        clippingOutUri: Uri,
        clippingRequestCode: Int,
        clippingResultCode: Int,
        clippingErrorCode: Int
    ) {
        ZToaster.makeText("图片剪裁building", ZToaster.C, R.color.zphoto_blue)
    }
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