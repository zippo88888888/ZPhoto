package com.zp.zphoto.sample

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.zp.zphoto_lib.content.ZPhotoDetail
import com.zp.zphoto_lib.content.ZImageCompressListener
import com.zp.zphoto_lib.content.checkGif
import com.zp.zphoto_lib.util.ZFile
import top.zibin.luban.Luban
import java.io.File
import java.util.*

class MyImageCompressListener : ZImageCompressListener() {

    override fun getCompressList(arrayList: ArrayList<ZPhotoDetail>?, context: Context?): ArrayList<ZPhotoDetail>? {
        if (arrayList == null || context == null) {
            return ArrayList()
        }
        val list = ArrayList<File>()
        arrayList.forEach { list.add(File(it.path)) }

        val outDir = ZFile.getPathForPath(ZFile.PHOTO)

        val compactList = Luban.with(context)
            .load(list)
            .ignoreBy(50)       // 小于50K不压缩
            .setTargetDir(outDir)    // 压缩后图片的路径
            .filter { filePath ->   // 设置压缩条件 gif 不压缩
                !(TextUtils.isEmpty(filePath) || filePath.toLowerCase().endsWith(".gif"))
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
}