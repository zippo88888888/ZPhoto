package com.zp.zphoto_lib.common

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import com.zp.zphoto_lib.content.*
import com.zp.zphoto_lib.ui.ZPhotoSelectActivity
import java.io.File

class ZPhotoHelp {

    private object BUILDER {
        val builder = ZPhotoHelp()
    }

    companion object {
        fun getInstance() = BUILDER.builder
    }

    fun init(application: Application, imageLoaderListener: ZImageLoaderListener) {
        this.imageLoaderListener = imageLoaderListener
        ZPhotoManager.getInstance().init(application)
    }

    private lateinit var imageLoaderListener: ZImageLoaderListener
    fun getImageLoaderListener() = imageLoaderListener

    /**
     * 配置信息
     */
    private var configuration: ZPhotoConfiguration? = null
    fun getConfiguration() = configuration ?: ZPhotoConfiguration()
    fun builder(configuration: ZPhotoConfiguration): ZPhotoHelp {
        this.configuration = configuration
        return this
    }

    /**
     * 设置图片压缩 监听
     */
    private var imageCompressListener: ZImageCompressListener? = null
    fun getImageCompressListener() = imageCompressListener
    fun setImageCompressListener(imageCompressListener: ZImageCompressListener?): ZPhotoHelp {
        this.imageCompressListener = imageCompressListener
        return this
    }

    /**
     * 选择结果回调
     */
    private var listener: ZImageResultListener? = null
    fun getZImageResultListener() = listener
    fun setZImageResultListener(listener: ZImageResultListener) : ZPhotoHelp {
        this.listener = listener
        return this
    }

    /**
     * 去相册
     */
    fun toPhoto(activity: Activity) {
        activity.jumpActivity(ZPhotoSelectActivity::class.java)
    }

    /**
     * 去相册
     */
    fun toPhoto(fragment: Fragment) {

    }

    /**
     * 去相机
     */
    fun toCamear(activity: Activity, outUri: String? = null) {

    }

    /**
     * 去相机
     */
    fun toCamear(fragment: Fragment, outUri: String? = null) {

    }

    /**
     * 重置
     */
    fun reset() {
        listener = null
        configuration = null
        imageCompressListener = null
    }

    /**
     * 获取拍照的 intent
     * @param outUri 拍照后保存的路径
     */
    private fun getCameraIntent(context: Context, outUri: String) = Intent().apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        action = MediaStore.ACTION_IMAGE_CAPTURE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val contentUri = FileProvider.getUriForFile(context,
                "${context.packageName}.FileProvider", File(outUri))
            putExtra(MediaStore.EXTRA_OUTPUT, contentUri)
        } else {
            putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(File(outUri)))
        }
    }

}