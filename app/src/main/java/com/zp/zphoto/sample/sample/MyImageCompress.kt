package com.zp.zphoto.sample.sample

import android.content.Context
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Log
import com.zp.zphoto_lib.content.GIF
import com.zp.zphoto_lib.content.MP4
import com.zp.zphoto_lib.content.ZImageCompress
import com.zp.zphoto_lib.content.ZPhotoDetail
import com.zp.zphoto_lib.util.ZPhotoUtil
import top.zibin.luban.Luban
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * 自己实现的 图片压缩方式
 */
class MyImageCompress : ZImageCompress() {

    override fun onPreExecute() {
        super.onPreExecute()
        Log.i("ZPhotoLib", "onPreExecute")
    }

    /**
     * 这里仅供参考，具体以自己的业务逻辑为主
     */
    override fun doingCompressImage(arrayList: ArrayList<ZPhotoDetail>?): ArrayList<ZPhotoDetail>? {
        if (arrayList == null || getContext() == null) {
            return ArrayList()
        }

        // 输出路径  Android Q ---> 这里是沙盒里面的，如果要放在外部，需要将沙盒里面的 复制 到 外部路径
        val outDir = ZPhotoUtil.getCompressPath()

        val builder = Luban.with(getContext())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val list = ArrayList<File>()
            arrayList.forEach {
                val newFile = File(outDir + it.name)
                val isSuccess = copyFile(getContext(), it.uri!!, newFile)
                if (isSuccess) {
                    Log.i("ZPhotoLib", "图片复制到沙盒目录成功")
                    list.add(newFile)
                } else {
                    list.add(File(it.path))
                }
            }
            builder.load(list)
        } else {
            val list = ArrayList<File>()
            arrayList.forEach { list.add(File(it.path)) }
            builder.load(list)
        }

        val compactList = builder
            .ignoreBy(50)       // 小于50K不压缩
            .setTargetDir(outDir)    // 压缩后图片的路径
            .filter {
                // 设置压缩条件 gif、视频 不压缩
                !(TextUtils.isEmpty(it) ||
                        it.toLowerCase().endsWith(".$GIF") ||
                        it.toLowerCase().endsWith(".$MP4"))
            }.get()

        arrayList.indices.forEach {
            val path = compactList[it].path
            val size = ZPhotoUtil.getDefaultFileSize(path)
            Log.e("ZPhotoLib", "原图大小：${arrayList[it].size}M <<<===>>>Luban处理后的大小：${size}M")
            arrayList[it].path = path
            arrayList[it].parentPath = ""
            arrayList[it].size = size
            arrayList[it].isGif = checkGif(path)
        }
        return arrayList
    }

    override fun onPostExecute(list: ArrayList<ZPhotoDetail>?) {
        super.onPostExecute(list)
        Log.i("ZPhotoLib", "onPostExecute")
    }

    /**
     * 将SD卡上的文件复制到沙盒目录
     * @param uri           原文件
     * @param targetFile    沙盒目录的文件
     */
    private fun copyFile(context: Context?, uri: Uri, targetFile: File): Boolean {
        var success = false
        // 新建文件输入流并对它进行缓冲
        val input = context?.contentResolver?.openInputStream(uri)
        val inBuff = BufferedInputStream(input)
        // 新建文件输出流并对它进行缓冲
        val output = FileOutputStream(targetFile)
        val outBuff = BufferedOutputStream(output)
        try {
            // 缓冲数组
            val b = ByteArray(1024 * 5)
            while (true) {
                val len = inBuff.read(b)
                if (len == -1) {
                    break
                } else {
                    outBuff.write(b, 0, len)
                }
            }
            // 刷新此缓冲的输出流
            outBuff.flush()
            success = true
        } catch (e: Exception) {
            e.printStackTrace()
            success = false
        } finally {
            //关闭流
            inBuff.close()
            outBuff.close()
            output.close()
            input?.close()
            return success
        }
    }

    private fun checkGif(url: String) = try {
        val gif = url.substring(url.lastIndexOf(".") + 1, url.length)
        "gif" == gif
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}