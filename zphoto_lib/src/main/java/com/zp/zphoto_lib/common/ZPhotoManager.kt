package com.zp.zphoto_lib.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import com.zp.zphoto_lib.content.ZPHOTO_SHOW_CAMEAR
import com.zp.zphoto_lib.content.ZPhotoDetail
import com.zp.zphoto_lib.content.forEach
import com.zp.zphoto_lib.content.forEachNoIterable
import com.zp.zphoto_lib.util.ZLog
import java.lang.ref.SoftReference
import java.util.*

internal class ZPhotoManager {

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
        if (allList.isNullOrEmpty()) {
            return
        }
        var list: List<ZPhotoDetail>
        val config = ZPhotoHelp.getInstance().getConfiguration()
        if (config.showCamera) {
            list = allList.filterNot { it.name == ZPHOTO_SHOW_CAMEAR }
        } else {
            list = allList
        }
        if (softReference == null) {
            softReference = if (config.showVideo) {
                if (config.allSelect) { // 同时选择不处理
                    SoftReference(list as ArrayList<ZPhotoDetail>)
                } else { // 如果不能同时选择，直接将视频移除
                    list = list.filterNot { it.isVideo }
                    SoftReference(list as ArrayList<ZPhotoDetail>)
                }
            } else {
                SoftReference(list as ArrayList<ZPhotoDetail>)
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