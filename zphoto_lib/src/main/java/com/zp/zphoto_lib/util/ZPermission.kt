package com.zp.zphoto_lib.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import com.zp.zphoto_lib.common.ZPhotoHelp
import java.lang.IllegalArgumentException
import java.util.ArrayList

internal object ZPermission {

    /** 读写SD卡权限  */
    internal const val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    /** 相机权限  */
    internal const val CAMERA = Manifest.permission.CAMERA

    /**
     * 判断是否申请过权限
     * @param context   Context
     * @param permissions  权限
     * @return  返回未授权的数组
     */
    fun checkPermission(context: Context, vararg permissions: String): Array<String> {
        val list = ArrayList<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                list.add(permission)
            }
        }
        return list.toTypedArray()
    }

    /**
     * 请求权限
     * @param a Activity
     * @param code  请求码
     * @param requestPermission 权限
     */
    fun requestPermission(a: Activity, code: Int, vararg requestPermission: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(a, requestPermission, code)
        }
    }

    /**
     * 请求权限
     * @param fragment Fragment
     * @param code  请求码
     * @param requestPermission 权限
     */
    fun requestPermission(fragment: Fragment, code: Int, vararg requestPermission: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fragment.requestPermissions(requestPermission, code)
        }
    }

    /**
     * 权限检测
     * @return  ArrayList<String> 返回权限申请失败的集合
     */
    fun onPermissionsResult(permissions: Array<out String>, grantResults: IntArray): ArrayList<String> {
        val noPermissions = ArrayList<String>()
        for (i in grantResults.indices) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                ZLog.i(permissions[i] + " 权限 申请成功")
            } else {
                noPermissions.add(permissions[i])
                ZLog.e(permissions[i] + " 权限 申请失败")
            }
        }
        return noPermissions
    }


}