package com.zp.zphoto_lib.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import com.zp.zphoto_lib.content.forEach
import java.util.*

class ZPhotoManager {

    private var applicationCon: Context? = null

    private object Builder {
        @SuppressLint("StaticFieldLeak") val MANAGER = ZPhotoManager()
    }

    private val activities by lazy { LinkedList<Activity>() }

    companion object {
        fun getInstance() = Builder.MANAGER
    }

    fun init(applicationCon: Application) {
        this.applicationCon = applicationCon
    }

    fun getApplicationContext(): Context {
        if (applicationCon == null) throw NullPointerException("请先调用\"init()\"方法")
        return applicationCon!!
    }

    /*@Synchronized
    fun addActivity(activity: Activity) {
        activities.add(activity)
    }

    @Synchronized
    fun removeActivity(activity: Activity) {
        if (activities.contains(activity)) {
            activities.remove(activity)
        }
    }

    @Synchronized
    fun clear() {
        activities.forEach { activity, i ->
            removeActivity(activity)
            activity.finish()
        }
    }*/
}