package com.zp.zphoto_lib.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import com.zp.zphoto_lib.content.ZPhotoDetail
import com.zp.zphoto_lib.content.forEach
import com.zp.zphoto_lib.content.forEachNoIterable
import com.zp.zphoto_lib.util.ZLog
import java.lang.ref.SoftReference
import java.util.*

class ZPhotoManager {

    private var applicationCon: Context? = null

    private object Builder {
        @SuppressLint("StaticFieldLeak") val MANAGER = ZPhotoManager()
    }

    private var softReference: SoftReference<ArrayList<ZPhotoDetail>?>? = null

    companion object {
        fun getInstance() = Builder.MANAGER
    }

    fun init(applicationCon: Application) {
        this.applicationCon = applicationCon
    }

    fun getApplicationContext() = if (applicationCon != null) applicationCon!!
    else throw NullPointerException("请先调用\"init()\"方法")

    fun setAllList(allList: ArrayList<ZPhotoDetail>?) {
        if (softReference == null) {
            val config = ZPhotoHelp.getInstance().getConfiguration()
            softReference = if (config.showVideo) {
                if (config.allSelect) { // 同时选择不处理
                    SoftReference(allList)
                } else { // 如果不能同时选择，直接将视频移除
                    ZLog.e("如果不能同时选择，需要将视频移除  size：${allList?.size}")
                    val list = allList?.filterNot { it.isVideo }
                    ZLog.e("如果不能同时选择，直接将视频移除  size：${list?.size}")
                    SoftReference(list as ArrayList<ZPhotoDetail>)
                }
            } else {
                SoftReference(allList)
            }
        }
    }

    fun getAllList() = softReference?.get()

    fun clearAllList() {
        if (softReference != null) {
            softReference?.clear()
            softReference = null
            System.gc()
        }
    }
}