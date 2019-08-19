package com.zp.zphoto_lib.util

import android.content.Context
import android.os.Environment
import com.zp.zphoto_lib.content.getAppContext
import java.io.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

object ZFile {

    /** 文件大小单位为B */
    const val SIZETYPE_B = 1
    /** 文件大小单位为KB */
    const val SIZETYPE_KB = 2
    /** 文件大小单位为MB */
    const val SIZETYPE_MB = 3
    /** 文件大小单位为GB */
    const val SIZETYPE_GB = 4

    /** 根目录 */
    private const val ROOT_DIR = "zphoto_lib"

    /** 照片目录 */
    const val PHOTO = "/photo/"
    /** 图片压缩 */
    const val COMPRESS = "/compress/"
    /** 图片裁剪 */
    const val CROP = "/crop/"

    private var storagePath: String? = null
    private var packageFilesDirectory: String? = null

    /** 缓存列表 */
    private fun getCacheList() = arrayOf(
        getPath() + PHOTO,
        getPath() + COMPRESS,
        getPath() + CROP
    )

    /**
     * 得到具体的路径
     */
    fun getPathForPath(path: String): String {
        val url = getPath() + path
        val file = File(url)
        if (!file.exists()) {
            file.mkdirs()
        }
        return url
    }


    private fun getPath(context: Context = getAppContext()): String? {
        if (storagePath == null) {
            storagePath = context.getExternalFilesDir(null).path + "/" + ROOT_DIR
            val file = File(storagePath)
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    storagePath = getPathInPackage(context, true)
                }
            }
        }
        return storagePath
    }

    private fun getPathInPackage(context: Context, grantPermissions: Boolean): String? {
        if (packageFilesDirectory != null) return packageFilesDirectory
        val path = "${context.filesDir}/$ROOT_DIR"
        val file = File(path)
        if (!file.exists()) {
            if (!file.mkdirs()) {
                ZLog.e("在pakage目录创建CGE临时目录失败!")
                return null
            }
            if (grantPermissions) { // 设置隐藏目录权限.
                if (file.setExecutable(true, false)) ZLog.e("文件可执行")
                if (file.setReadable(true, false)) ZLog.e("文件可读")
                if (file.setWritable(true, false)) ZLog.e("文件可写")
            }
        }
        packageFilesDirectory = path
        return packageFilesDirectory
    }

    /**
     * 根据当前时间 + 后缀 命名
     * @param suffix 后缀
     * @param index  下标
     */
    fun getFileName(suffix: String, index: Int = -1): String {
        val simpleDateFormat = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.CHINA)
        val timeStamp = simpleDateFormat.format(Date())
        return if (index > 0) timeStamp + "_$index" + suffix else timeStamp + suffix
    }

    /**
     * 获取指定文件或文件夹的指定单位的大小
     * @param filePath 文件或文件夹路径
     * @param sizeType 获取大小的类型1为B、2为KB、3为MB、4为GB
     */
    @JvmOverloads
    @JvmStatic
    fun getFileOrFilesSize(filePath: String, sizeType: Int = SIZETYPE_MB): Double {
        val file = File(filePath)
        var blockSize: Long = 0
        try {
            blockSize = if (file.isDirectory) getFileSizes(file)
            else getFileSize(file)
        } catch (e: Exception) {
            e.printStackTrace()
            ZLog.e("获取文件大小--->>>获取失败!")
        } finally {
            return formetFileSize(blockSize, sizeType)
        }
    }

    /**
     * 获取指定文件大小
     */
    private fun getFileSize(file: File) = if (file.exists()) file.length() else 0L

    /**
     * 获取指定文件夹
     */
    private fun getFileSizes(f: File): Long {
        var size: Long = 0
        val flist = f.listFiles()
        for (i in flist!!.indices) {
            size = if (flist[i].isDirectory) {
                size + getFileSizes(flist[i])
            } else {
                size + getFileSize(flist[i])
            }
        }
        return size
    }

    /**
     * 转换文件大小,指定转换的类型
     */
    fun formetFileSize(fileS: Long, sizeType: Int = SIZETYPE_MB): Double {
        val df = DecimalFormat("#.00")
        return when (sizeType) {
            SIZETYPE_B -> java.lang.Double.valueOf(df.format(fileS.toDouble()))
            SIZETYPE_KB -> java.lang.Double.valueOf(df.format(fileS.toDouble() / 1024))
            SIZETYPE_MB -> java.lang.Double.valueOf(df.format(fileS.toDouble() / 1048576))
            SIZETYPE_GB -> java.lang.Double.valueOf(df.format(fileS.toDouble() / 1073741824))
            else -> 0.0
        }
    }

    /**
     * 删除文件
     */
    private fun delete(file: File) {
        if (!file.exists()) return
        if (file.isFile) {
            file.delete()
            return
        }
        if (file.isDirectory) {
            val childFiles = file.listFiles()
            if (childFiles == null || childFiles.isEmpty()) {
                file.delete()
                return
            }
            for (i in childFiles.indices) {
                delete(childFiles[i])
            }
            file.delete()
        }
    }

    /**
     * 获取缓存目录的大小  建议在非UI线程中操作
     */
    fun getZPhotoCacheSize(sizeType: Int = SIZETYPE_MB): Double {
        var size = 0.0
        getCacheList().forEach {
            if (File(it).exists()) {
                val filesSize = getFileOrFilesSize(it, sizeType)
                size += filesSize
            }
        }
        return size
    }

    /**
     * 清除缓存
     */
    fun deleteZPhotoCache(clearSuccess: () -> Unit) {
        Thread {
            getCacheList().forEach {
                delete(File(it))
            }
            clearSuccess.invoke()
        }.start()
    }

}