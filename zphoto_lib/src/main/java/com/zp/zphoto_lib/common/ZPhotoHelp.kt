package com.zp.zphoto_lib.common

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.zp.zphoto_lib.R
import com.zp.zphoto_lib.content.*
import com.zp.zphoto_lib.ui.ZPhotoSelectActivity
import com.zp.zphoto_lib.ui.crop.ZPhotoCrop
import com.zp.zphoto_lib.util.*
import java.io.File
import java.lang.NullPointerException

class ZPhotoHelp {

    // 用户拍照的路径，始终为沙盒目录
    private var cameraPath = ""

    // 用户设置的拍照路径、名称
    internal var cameraOutPath: String? = ""
    internal var cameraOutName: String? = ""

    // 需要剪裁的数据
    private val needCropDataArray by lazy {
        ArrayList<ZPhotoDetail>()
    }
    // 剪裁后的数据
    private val afterCropDataArray by lazy {
        ArrayList<String>()
    }
    private var cropIndex = 0

    private object BUILDER {
        val builder = ZPhotoHelp()
    }

    companion object {
        @JvmStatic
        fun getInstance() = BUILDER.builder
    }

    fun init(application: Application, imageLoaderListener: ZImageLoaderListener) {
        this.imageLoaderListener = imageLoaderListener
        ZPhotoManager.getInstance().init(application)
    }

    /**
     * 图片加载方式，必须手动实现
     */
    private var imageLoaderListener: ZImageLoaderListener? = null
    fun getImageLoaderListener(): ZImageLoaderListener =
        if (imageLoaderListener == null) throw NullPointerException("ZImageLoaderListener is not Null")
        else imageLoaderListener!!

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
     * 获取ZPhoto缓存大小 单位MB
     */
    fun getZPhotoCacheSize() = ZFile.getZPhotoCacheSize()

    /**
     * 清除ZPhoto缓存
     */
    fun clearZPhotoCache() = ZFile.deleteZPhotoCache()

    /**
     * 去相册
     */
    fun toPhoto(activity: Activity) {
        clearArray()
        activity.jumpActivity(ZPhotoSelectActivity::class.java)
    }

    /**
     * 去相册
     */
    fun toPhoto(fragment: Fragment) {
        clearArray()
        fragment.activity?.let {
            toPhoto(it)
        }
    }

    /**
     * 去相机
     * @param outPath String?       拍照后保存的路径，空为默认值
     * @param outName String?       拍照后保存的名称
     */
    @JvmOverloads
    fun toCamera(activity: Activity, outPath: String? = null, outName: String? = null) {
        clearArray()
        val noPermissionArray = ZPermission.checkPermission(activity, ZPermission.CAMERA, ZPermission.WRITE_EXTERNAL_STORAGE)
        if (noPermissionArray.isNullOrEmpty()) {
            /*val path = if (outPath.isNullOrEmpty()) {
                ZFile.getPathForPath(ZFile.PHOTO) + ZFile.getFileName(".jpg")
            } else outPath
            this.cameraPath = path*/
            getCameraPath(outPath, outName)
            activity.startActivityForResult(getCameraIntent(activity, cameraPath), ZPHOTO_TO_CAMEAR_REQUEST_CODE)
        } else {
            ZPermission.requestPermission(activity, ZPHOTO_CAMEAR_CODE, *noPermissionArray)
        }
    }

    /**
     * 去相机
     * @param outPath String?       拍照后保存的路径，空为默认值
     * @param outName String?       拍照后保存的名称
     */
    @JvmOverloads
    fun toCamera(fragment: Fragment, outPath: String? = null, outName: String? = null) {
        clearArray()
        val noPermissionArray =
            ZPermission.checkPermission(
                fragment.activity!!,
                ZPermission.CAMERA,
                ZPermission.WRITE_EXTERNAL_STORAGE
            )
        if (noPermissionArray.isNullOrEmpty()) {
            /*val path = if (outPath.isNullOrEmpty()) {
                ZFile.getPathForPath(ZFile.PHOTO) + ZFile.getFileName(".jpg")
            } else outPath
            this.cameraPath = path*/
            getCameraPath(outPath, outName)
            fragment.startActivityForResult(
                getCameraIntent(fragment.activity!!, cameraPath),
                ZPHOTO_TO_CAMEAR_REQUEST_CODE
            )
        } else {
            ZPermission.requestPermission(fragment, ZPHOTO_CAMEAR_CODE, *noPermissionArray)
        }
    }

    private fun getCameraPath(outPath: String?, outName: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.cameraPath = ZFile.getPathForPath(ZFile.PHOTO) + ZFile.getFileName(".jpg")
        } else {
            val path = if (outPath.isNullOrEmpty()) {
                ZFile.getPathForPath(ZFile.PHOTO) + ZFile.getFileName(".jpg")
            } else outPath + outName
            this.cameraPath = path
        }
        this.cameraOutPath = outPath
        this.cameraOutName = outName
    }

    private fun clearArray() {
        needCropDataArray.clear()
        afterCropDataArray.clear()
        cropIndex = 0
        cameraPath = ""
        cameraOutPath = ""
        cameraOutName = ""
    }

    /**
     * 重置
     */
    fun reset() {
        resultListener = null
        configuration = null
        imageCompress = null
        needCropDataArray.clear()
        afterCropDataArray.clear()
        cropIndex = 0
        cameraPath = ""
        cameraOutPath = ""
        cameraOutName = ""
    }

    /**
     * 处理相机 回调
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, activityOrFragment: Any) {
        val context = when (activityOrFragment) {
            is Activity -> activityOrFragment
            is Fragment -> activityOrFragment.activity
            else -> throw IllegalArgumentException(getStringById(R.string.zphoto_a_f_error))
        }
        val config = getConfiguration()
        when (requestCode) {
            ZPHOTO_SELECT_PIC_BACK_CODE -> { // 从ZPhotoSelectActivity 确定跳转过来的

                val pics = data?.getParcelableArrayListExtra<ZPhotoDetail>("selectData")
                val configuration = getInstance().getConfiguration()
                if (configuration.needCrop) { // 剪裁
//                    needCropDataArray = pics
                    pics?.let { needCropDataArray.addAll(it) }
                    cropIndex = 0
                    cropPic(context!!)
                } else { // 压缩
                    compressPic(context!!, pics)
                }
            }
            ZPHOTO_TO_CAMEAR_REQUEST_CODE -> { // 拍照后

                if (resultCode == Activity.RESULT_CANCELED) {
                    getZImageResultListener()?.selectCancel()
                    return
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (!cameraOutPath.isNullOrEmpty()) {
                        // 需要将 沙盒里的拍照图片 复制到 公共目录
                        val outFolderPath = cameraOutPath!!.substring(cameraOutPath!!.lastIndexOf("/") + 1)
                        ZFile.saveDataInPublicFolderByQ(
                            context!!,
                            cameraPath,
                            outFolderPath,
                            outFileName = cameraOutName
                        )
                    }
                }

                val resultFile = File(cameraPath)
                if (!resultFile.exists() && data == null) {
                    ZLog.e("无法获取拍照或剪裁后的数据")
                    getZImageResultListener()?.selectFailure()
                    return
                }

                val uri = Uri.fromFile(resultFile) // 获取拍照后的图片数据

                val datas = if (data != null) { // 代表从从ZPhotoSelectActivity界面回来的，携带了上个界面选中的图片信息
                    data.getParcelableArrayListExtra<ZPhotoDetail>("selectData")
                } else ArrayList<ZPhotoDetail>() // 不是从上个界面来的

                uri.path?.let {
                    val displayName = it.substring(it.lastIndexOf("/") + 1, it.length)
                    datas.add(
                        ZPhotoDetail(
                            it,
                            displayName,
                            ZFile.getFileOrFilesSize(it),
                            checkGif(it),
                            false,
                            0,
                            "",
                            System.currentTimeMillis(),
                            uri
                        )
                    )
                }
                if (config.needCrop) { // 剪裁
//                    needCropDataArray = datas
                    datas?.let { needCropDataArray.addAll(it) }
                    cropIndex = 0
                    cropPic(context!!)
                } else { // 压缩图片
                    compressPic(context!!, datas)
                }
            }
            ZPHOTO_CROP_REQUEST_CODE -> { // 剪裁
                nextCropCheck(context!!, resultCode, data)
            }
        }
    }

    /**
     * 剪裁判断
     */
    fun nextCropCheck(context: Activity,resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val cropUri = ZPhotoCrop.getOutput(data)
                needCropDataArray[cropIndex].apply {
                    path = cropUri?.path ?: path
                    name = path.substring(path.lastIndexOf("/") + 1, path.length)
                    size = ZFile.getFileOrFilesSize(path)
                }
            }
        } else {
            ZLog.i("用户不想剪裁该图片，继续")
        }
        if (cropIndex < needCropDataArray.size - 1) {
            cropIndex++
            cropPic(context)
        } else {
            compressPic(context, needCropDataArray)
        }
    }

    /**
     * 剪裁图片
     */
    private fun cropPic(activity: Activity) {
        val item = needCropDataArray[cropIndex]
        if (item.isVideo || item.isGif) { // 视频，获取GIF不剪裁
            ZLog.e("不支持剪裁的类型--->>>${item.path}")
            if (cropIndex < needCropDataArray.size - 1) {
                cropIndex ++
                cropPic(activity)
            } else {
                compressPic(activity, needCropDataArray)
            }
        } else {
            val inputPath = item.path
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // 先使用沙盒 ， 后再复制到 指定路径
                val outPath = ZFile.getPathForPath(ZFile.CROP) + "crop_" +
                        ZFile.getFileName(".jpg")
                val outUri = Uri.fromFile(File(outPath))
                afterCropDataArray.add(outPath)
                ZPhotoCrop.of(item.uri!!, outUri).asSquare().start(activity)
            } else {
                val outCropFolderPath = getConfiguration().cropFolderPath
                val outPath = if (outCropFolderPath.isEmpty()) {
                    ZFile.getPathForPath(ZFile.CROP) + "crop_" + ZFile.getFileName(".jpg")
                } else {
                    outCropFolderPath + "crop_" + ZFile.getFileName(".jpg")
                }
                val outUri = Uri.fromFile(File(outPath))
                ZPhotoCrop.of(Uri.fromFile(File(inputPath)), outUri).asSquare().start(activity)
            }
        }
    }

    /**
     * 压缩图片
     */
    private fun compressPic(activity: Activity, datas: ArrayList<ZPhotoDetail>?) {
        // 从沙盒复制到指定位置
        ZFile.saveCropData(activity, afterCropDataArray)
        if (getConfiguration().needCompress) { // 压缩图片
            val imageCompress = getZImageCompress() ?: throw NullPointerException(
                getStringById(R.string.zphoto_imageCompressErrorMsg)
            )
            imageCompress.start(activity, datas) {
                getZImageResultListener()?.selectSuccess(it)
            }
        } else {
            getZImageResultListener()?.selectSuccess(datas)
        }
        if (activity is ZPhotoSelectActivity) {
            activity.finish()
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
            val contentUri = FileProvider.getUriForFile(context, getConfiguration().authority, File(outUri))
            putExtra(MediaStore.EXTRA_OUTPUT, contentUri)
        } else {
            putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(File(outUri)))
        }
    }

    /**
     * 权限处理
     */
    @JvmOverloads
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray,
                                   activityOrFragment: Any, outUri :String? = null) {
        if (requestCode == ZPHOTO_CAMEAR_CODE) {
            val noPermissionArray = ZPermission.onPermissionsResult(permissions, grantResults)
            if (noPermissionArray.isNullOrEmpty()) {
                when (activityOrFragment) {
                    is Activity -> toCamera(activityOrFragment, outUri)
                    is Fragment -> toCamera(activityOrFragment, outUri)
                    else -> throw IllegalArgumentException(getStringById(R.string.zphoto_a_f_error))
                }
            } else {
                ZToaster.makeTextS("权限获取失败")
            }

        }
    }

}