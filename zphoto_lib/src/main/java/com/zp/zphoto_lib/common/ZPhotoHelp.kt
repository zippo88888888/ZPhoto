package com.zp.zphoto_lib.common

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import com.zp.zphoto_lib.content.ZImageCompressListener
import com.zp.zphoto_lib.content.ZImageLoaderListener
import com.zp.zphoto_lib.content.ZImageResultListener
import java.io.File

/**
 * Created by ZP on 2018/8/22.
 * description:
 * version: 1.0
 */
class ZPhotoHelp {

    private object BUILDER {
        val builder = ZPhotoHelp()
    }

    companion object {
        fun getInstance() = BUILDER.builder

        /** 调用相机 */
        const val TO_CAMEAR_REQUEST_CODE = 0x2001
        /** 剪裁 */
//        const val CROP_REQUEST_CODE = ZPCrop.REQUEST_CROP
        const val CROP_REQUEST_CODE = 0x2020
        /** 剪裁失败 */
//        const val CROP_ERROR_CODE = ZPCrop.RESULT_ERROR
        const val CROP_ERROR_CODE = 0x2021

        /** 默认最大选中数量 */
        const val DEFAULT_MAX_SELECT = 9
        /** 默认最大可选 size 10M */
        private const val DEFAULT_MAX_SIZE = 10
        /** 默认的压缩比率 */
        private const val DEFAULT_COMPACT_RATIO = 0.6f

        /** 默认视频最大可选 size 50M */
        private const val DEFAULT_MAX_VIDEO_SIZE = 50

        /** 默认剪裁尺寸比 默认为1:1 */
        private val DEFAULT_CROP by lazy { arrayOf(1, 1) }
    }

    fun init(application: Application, imageLoaderListener: ZImageLoaderListener, isDebug: Boolean) {
        this.isDebug = isDebug
        this.imageLoaderListener = imageLoaderListener
        ZPhotoManager.getInstance().init(application)
    }

    private lateinit var imageLoaderListener: ZImageLoaderListener
    fun getImageLoaderListener() = imageLoaderListener

    /**
     * 设置图片压缩
     */
    private var imageCompressListener: ZImageCompressListener? = null
    fun getImageCompressListener() = imageCompressListener
    fun setImageCompressListener(imageCompressListener: ZImageCompressListener?): ZPhotoHelp {
        this.imageCompressListener = imageCompressListener
        return this
    }

    /**
     * 获取拍照的 intent
     * @param outUri 拍照后保存的路径
     */
    fun getCameraIntent(context: Context, outUri: String) = Intent().apply {
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

    private var listener: ZImageResultListener? = null
    fun getZImageResultListener() = listener
    fun setZImageResultListener(listener: ZImageResultListener) : ZPhotoHelp {
        this.listener = listener
        return this
    }

    /**
     * 重置
     */
    fun reset() {
        listener = null
        maxSelect = DEFAULT_MAX_SELECT
        showGif = true
        needCrop = true
        needCompress = true
        imageCompressListener = null
    }

    private var maxSelect = DEFAULT_MAX_SELECT
    fun getMaxSelect() = maxSelect
    fun setMaxSelect(maxSelect: Int): ZPhotoHelp {
        this.maxSelect = maxSelect
        return this
    }

    private var maxSize = DEFAULT_MAX_SIZE
    fun getMaxSize() = maxSize
    fun setMaxSize(maxSize: Int): ZPhotoHelp {
        this.maxSize = maxSize
        return this
    }

    /**
     * 是否显示Gif
     */
    private var showGif = true
    fun getShowGif() = showGif
    fun setShowGif(showGif: Boolean): ZPhotoHelp {
        this.showGif = showGif
        return this
    }

    /**
     * 是否需要裁剪  只针对单张图片
     */
    private var needCrop = true
    fun getNeedCrop() = needCrop
    fun setNeedCrop(needCrop: Boolean): ZPhotoHelp {
        this.needCrop = needCrop
        return this
    }

    /**
     * 是否需要压缩
     */
    private var needCompress = true
    fun getNeedCompress() = needCompress
    fun setNeedCompress(needCompress: Boolean): ZPhotoHelp {
        this.needCompress = needCompress
        return this
    }

    /**
     * 压缩比率 0 到 1
     */
    private var compressRatio = DEFAULT_COMPACT_RATIO
    fun getCompressRatio() = compressRatio
    fun setCompressRatio(compressRatio: Float): ZPhotoHelp {
        this.compressRatio = compressRatio
        return this
    }

    /**
     * 设置是否显示视频
     */
    private var isShowVideo = false
    fun getShowVideo() = isShowVideo
    fun setShowVideo(isShowVideo: Boolean): ZPhotoHelp {
        this.isShowVideo = isShowVideo
        return this
    }

    /**
     * 设置视频最大可选取的size
     */
    private var maxVideoSize = DEFAULT_MAX_VIDEO_SIZE
    fun getMaxVideoSize() = maxVideoSize
    fun setMaxVideoSize(maxVideoSize: Int): ZPhotoHelp {
        this.maxVideoSize = maxVideoSize
        return this
    }

    private var isDebug = false
    fun isDebug() = isDebug

}