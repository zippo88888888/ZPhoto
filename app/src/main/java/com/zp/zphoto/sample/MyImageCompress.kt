package com.zp.zphoto.sample

import android.app.ProgressDialog
import android.text.TextUtils
import android.util.Log
import com.zp.zphoto_lib.content.*
import com.zp.zphoto_lib.util.ZFile
import top.zibin.luban.Luban
import java.io.File
import java.util.*

/**
 * 自己实现的 图片压缩方式
 */
class MyImageCompress : ZImageCompress() {

    private var dialog: ProgressDialog? = null

    override fun onPreExecute() {
        super.onPreExecute()
        dialog = ProgressDialog(softReference?.get()).run {
            setProgressStyle(android.app.ProgressDialog.STYLE_SPINNER)
            setMessage("图片处理中")
            setCancelable(false)
            this
        }
        dialog?.show()
    }

    override fun doingCompressImage(arrayList: ArrayList<ZPhotoDetail>?): ArrayList<ZPhotoDetail>? {
        if (arrayList == null || softReference?.get() == null) {
            return ArrayList()
        }

        val list = ArrayList<File>()
        arrayList.forEach { list.add(File(it.path)) }

        val outDir = ZFile.getPathForPath(ZFile.PHOTO)

        val compactList = Luban.with(softReference?.get())
            .load(list)
            .ignoreBy(50)       // 小于50K不压缩
            .setTargetDir(outDir)    // 压缩后图片的路径
            .filter { filePath ->   // 设置压缩条件 gif、视频 不压缩
                !(TextUtils.isEmpty(filePath) ||
                        filePath.toLowerCase().endsWith(".$GIF") ||
                        filePath.toLowerCase().endsWith(".$MP4"))
            }.get()

        arrayList.indices.forEach {
            val path = compactList[it].path
            val size = ZFile.getFileOrFilesSize(path, ZFile.SIZETYPE_MB)
            Log.e("压缩图片", "原图大小：${arrayList[it].size}M <<<===>>>处理后的大小：${size}M")
            arrayList[it].path = path
            arrayList[it].parentPath = ""
            arrayList[it].size = size
            arrayList[it].isGif = checkGif(path)
        }
        return arrayList
    }

    override fun onPostExecute(list: ArrayList<ZPhotoDetail>?) {
        super.onPostExecute(list)
        dialog?.dismiss()
    }
}