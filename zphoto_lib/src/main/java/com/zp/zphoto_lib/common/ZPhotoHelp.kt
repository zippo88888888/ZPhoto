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
import com.zp.zphoto_lib.R
import com.zp.zphoto_lib.content.*
import com.zp.zphoto_lib.ui.ZPhotoSelectActivity
import com.zp.zphoto_lib.util.*
import java.io.File
import java.lang.NullPointerException

class ZPhotoHelp {

    private var outUri: String? = null

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

    /**
     * 图片加载方式，必须手动实现
     */
    private lateinit var imageLoaderListener: ZImageLoaderListener
    fun getImageLoaderListener() = imageLoaderListener

    /**
     * 配置信息
     */
    private var configuration: ZPhotoConfiguration? = null
    fun getConfiguration() = configuration ?: ZPhotoConfiguration()
    fun config(configuration: ZPhotoConfiguration): ZPhotoHelp {
        this.configuration = configuration
        return this
    }

    /**
     * 设置图片剪裁
     */
    private var imageClippingListener: ZImageClippingListener? = null
    fun getImageClippingListener() = imageClippingListener
    fun setImageClippingListener(imageClippingListener: ZImageClippingListener?): ZPhotoHelp {
        this.imageClippingListener = imageClippingListener
        return this
    }

    /**
     * 设置图片压缩 实现方式
     */
    private var imageCompress: ZImageCompress? = null
    fun getZImageCompress() = imageCompress
    fun setZImageCompress(imageCompress: ZImageCompress?): ZPhotoHelp {
        this.imageCompress = imageCompress
        return this
    }

    /**
     * 选择结果回调
     */
    private var resultListener: ZImageResultListener? = null
    fun getZImageResultListener() = resultListener
    fun setZImageResultListener(resultListener: ZImageResultListener) : ZPhotoHelp {
        this.resultListener = resultListener
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
        fragment.activity?.let {
            toPhoto(it)
        }
    }

    /**
     * 去相机
     * @param outUri    拍照后保存的路径，空为默认值
     */
    fun toCamear(activity: Activity, outUri: String? = null) {
        val noPermissionArray = ZPermission.checkPermission(activity, ZPermission.CAMERA, ZPermission.WRITE_EXTERNAL_STORAGE)
        if (noPermissionArray.isNullOrEmpty()) {
            val uri = if (outUri.isNullOrEmpty()) {
                ZFile.getPathForPath(ZFile.PHOTO) + ZFile.getFileName(".jpg")
            } else outUri
            this.outUri = uri
            activity.startActivityForResult(getCameraIntent(activity, uri), ZPHOTO_TO_CAMEAR_REQUEST_CODE)
        } else {
            ZPermission.requestPermission(activity, ZPermission.CAMEAR_CODE, *noPermissionArray)
        }
    }

    /**
     * 去相机
     * @param outUri    拍照后保存的路径，空为默认值
     */
    fun toCamear(fragment: Fragment, outUri: String? = null) {
        val noPermissionArray =
            ZPermission.checkPermission(fragment.activity!!, ZPermission.CAMERA, ZPermission.WRITE_EXTERNAL_STORAGE)
        if (noPermissionArray.isNullOrEmpty()) {
            val uri = if (outUri.isNullOrEmpty()) {
                ZFile.getPathForPath(ZFile.PHOTO) + ZFile.getFileName(".jpg")
            } else outUri
            this.outUri = uri
            fragment.startActivityForResult(getCameraIntent(fragment.activity!!, uri), ZPHOTO_TO_CAMEAR_REQUEST_CODE)
        } else {
            ZPermission.requestPermission(fragment, ZPermission.CAMEAR_CODE, *noPermissionArray)
        }
    }

    /**
     * 重置
     */
    fun reset() {
        resultListener = null
        configuration = null
        imageCompress = null
        imageClippingListener = null
        outUri = null
    }

    /**
     * 处理相机 回调
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, activityOrFragment: Any) {
        val resultFile = File(outUri)
        if (!resultFile.exists() && data == null) {
            ZLog.e("无法获取拍照或剪裁后的数据")
            getZImageResultListener()?.selectFailure()
            return
        }
        val context = when (activityOrFragment) {
            is Activity -> activityOrFragment
            is Fragment -> activityOrFragment.context
            else -> throw IllegalArgumentException("activityOrFragment is not Activity or Fragment")
        }
        val config = getConfiguration()
        when (requestCode) {
            ZPHOTO_TO_CAMEAR_REQUEST_CODE -> { // 拍照后
                if (config.needClipping) { // 剪裁
                    ZToaster.makeText("拍照后剪裁loading", ZToaster.C, R.color.zphoto_blue)
                } else {
                    val uri = Uri.fromFile(resultFile) // 获取拍照后的图片数据

                    val datas = if (data != null) { // 代表从上个界面回来的，携带了上个界面选中的图片信息
                        data.getParcelableArrayListExtra<ZPhotoDetail>("selectData")
                    } else ArrayList<ZPhotoDetail>() // 不是从上个界面来的

                    ZLog.e("拍照后path--->>> ${uri.path}")

                    uri.path?.let {
                        val displayName = it.substring(it.lastIndexOf("/") + 1, it.length)
                        datas.add(
                            ZPhotoDetail(
                                it,
                                displayName,
                                ZFile.getFileOrFilesSize(it, ZFile.SIZETYPE_MB),
                                checkGif(it),
                                false,
                                0,
                                "",
                                System.currentTimeMillis()
                            )
                        )
                    }
                    if (config.needCompress) { // 压缩图片
                        val imageCompress = getZImageCompress() ?: throw NullPointerException(
                            getStringById(R.string.zphoto_imageCompressErrorMsg)
                        )
                        imageCompress.start(context!!, datas) {
                            getZImageResultListener()?.selectSuccess(it)
                        }
                    } else {
                        getZImageResultListener()?.selectSuccess(datas)
                    }
                }
            }
            ZPHOTO_CROP_REQUEST_CODE -> { // 剪裁
                if (config.needCompress) { // 压缩图片

                } else {

                }
            }
        }
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