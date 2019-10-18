package com.zp.zphoto_lib.content

import android.app.Activity
import android.content.*
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v4.util.ArrayMap
import android.support.v7.app.AppCompatActivity
import android.util.SparseArray
import android.util.SparseBooleanArray
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.zp.zphoto_lib.R
import com.zp.zphoto_lib.common.ZPhotoManager
import com.zp.zphoto_lib.util.ZLog
import java.io.Serializable
import java.util.*

const val JPEG = "jpeg"
const val JPG = "jpg"
const val PNG = "png"
const val GIF = "gif"
const val MP4 = "mp4"

const val Z_ALL_DATA_KEY = "ZPhotoAllDate"
const val Z_ALL_VIDEO_KEY = "ZPhotoAllVideo"
const val ZPHOTO_SHOW_CAMEAR = "显示拍照"

const val ZPHOTO_SELECT_PIC_BACK_CODE = 9000
/** 调用相机 */
const val ZPHOTO_TO_CAMEAR_REQUEST_CODE = 9001
/** 剪裁 requestCode */
const val ZPHOTO_CROP_REQUEST_CODE = 9020
const val ZPHOTO_PICK_REQUEST_CODE = 9021
/** 剪裁 错误code */
const val ZPHOTO_CROP_ERROR_CODE = 500

/** 预览图片requestCode */
const val ZPHOTO_PREVIEW_REQUEST_CODE = 9022
/** 预览图片resultCode */
const val ZPHOTO_PREVIEW_RESULT_CODE = 9023

/** 图片默认最多可选的取数量 */
const val ZPHOTO_DEFAULT_MAX_PIC_SELECT = 9
/** 图片默认最大可选 size 5M */
const val ZPHOTO_DEFAULT_MAX_PIC_SIZE = 5
/** 图片默认最小可选 size 10 byte */
const val ZPHOTO_DEFAULT_MIN_PIC_SIZE = 10L

/** 视频默认最多可选取的数量 */
const val ZPHOTO_DEFAULT_MAX_VIDEO_SELECT = 2
/** 视频默认最大可选 size 50M */
const val ZPHOTO_DEFAULT_MAX_VIDEO_SIZE = 50
/** 视频默认最小可选 size 10 byte */
const val ZPHOTO_DEFAULT_MIN_VIDEO_SIZE = 10L

/** CheckBox样式 */
const val ZPHOTO_BOX_STYLE_DEFAULT = 1
const val ZPHOTO_BOX_STYLE_DIY = 2
@Deprecated("慎用")
const val ZPHOTO_BOX_STYLE_NUM = 3



internal fun Context.jumpActivity(clazz: Class<*>, map: ArrayMap<String, Any>? = null) {
    startActivity(Intent(this, clazz).apply {
        if (!map.isNullOrEmpty()) {
            putExtras(getBundleFormMapKV(map))
        }
    })
}

internal fun Activity.jumpActivity(clazz: Class<*>, map: ArrayMap<String, Any>? = null, requestCode: Int) {
    startActivityForResult(Intent(this, clazz).apply {
        if (!map.isNullOrEmpty()) {
            putExtras(getBundleFormMapKV(map))
        }
    }, requestCode)
}

internal fun getBundleFormMapKV(map: ArrayMap<String, Any>) = Bundle().apply {
    for ((k, v) in map) {
        when (v) {
            is Int -> putInt(k, v)
            is Double -> putDouble(k, v)
            is Float -> putFloat(k, v)
            is Long -> putLong(k, v)
            is Boolean -> putBoolean(k, v)
            is Char -> putChar(k, v)
            is String -> putString(k, v)
            is Serializable -> putSerializable(k, v)
            is Parcelable ->  putParcelableArrayList(k, v as ArrayList<out Parcelable>?)
            else -> ZLog.e("Unsupported format")
        }
    }
}

internal fun getAppContext() = ZPhotoManager.getInstance().getApplicationContext()

internal fun getToolBarHeight() = getAppContext().resources.getDimension(R.dimen.zphoto_toolBarHeight).toInt()

fun Activity.setStatusBarTransparent() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val decorView = window.decorView
        val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        decorView.systemUiVisibility = option
        window.statusBarColor = Color.TRANSPARENT
    }
}

fun Context.getStatusBarHeight() = resources.getDimensionPixelSize(
    resources.getIdentifier("status_bar_height", "dimen", "android")
)

fun Context.getDisplay() = IntArray(2).apply {
    val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val point = Point()
    manager.defaultDisplay.getSize(point)
    this[0] = point.x
    this[1] = point.y
}

fun dip2pxF(dpValue: Float) = dpValue * getAppContext().resources.displayMetrics.density + 0.5f
fun dip2px(dpValue: Float) = dip2pxF(dpValue).toInt()
fun px2dipF(pxValue: Float) = pxValue / getAppContext().resources.displayMetrics.density + 0.5f
fun px2dip(pxValue: Float) = px2dipF(pxValue).toInt()

fun getColorById(colorID: Int) = ContextCompat.getColor(getAppContext(), colorID)
fun getDimenById(dimenID: Int) = getAppContext().resources.getDimension(dimenID)
fun getStringById(stringID: Int) = getAppContext().resources.getString(stringID)

fun getTipStr(strRes: Int, value: Int) = String.format(getStringById(strRes), value)


fun getTextValue(any: Any) = try {
    when (any) {
        is Int -> getStringById(any)
        is String -> any
        else -> any.toString()
    }
} catch (e: Exception) {
    any.toString()
}!!

fun checkGif(url: String) = try {
    val gif = url.substring(url.lastIndexOf(".") + 1, url.length)
    "gif" == gif
} catch (e: Exception) {
    e.printStackTrace()
    false
}

internal inline fun SparseBooleanArray.forEach(block: (Int) -> Unit) {
    var index = 0
    val size = size()
    while (index < size) {
        block(index)
        index ++
    }
}

internal inline fun <E> List<E>.forEachNoIterable(block: (E) -> Unit) {
    var index = 0
    val size = size
    while (index < size) {
        block(get(index))
        index ++
    }
}


