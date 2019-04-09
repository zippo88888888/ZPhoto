package com.zp.zphoto_lib.content

import android.app.Activity
import android.content.*
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.content.ContextCompat
import android.support.v4.util.ArrayMap
import android.support.v7.app.AppCompatActivity
import android.util.SparseArray
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.zp.zphoto_lib.BuildConfig
import com.zp.zphoto_lib.R
import com.zp.zphoto_lib.common.ZPhotoManager
import com.zp.zphoto_lib.util.ZLog
import java.io.Serializable
import java.util.*

const val JPEG = "jpeg"
const val JPG = "jpg"
const val PNG = "png"
const val GIF = "gif"

const val DEFAULT_COUNT = 6
const val DELAY = 150
const val DURATION = 1500

// Context 相关 ===========================================================
fun Context.jumpActivity(clazz: Class<*>, map: ArrayMap<String, Any>? = null) {
    startActivity(Intent(this, clazz).apply {
        if (!map.isNullOrEmpty()) {
            putExtras(getBundleFormMapKV(map))
        }
    })
}

/**
 * 根据Map 获取 Bundle
 */
fun getBundleFormMapKV(map: ArrayMap<String, Any>) = Bundle().apply {
//                map.forEach { k, v ->
    map.forEachNoIterable { k, v ->
        when (v) {
            is Int -> putInt(k, v)
            is Double -> putDouble(k, v)
            is Float -> putFloat(k, v)
            is Long -> putLong(k, v)
            is Boolean -> putBoolean(k, v)
            is Char -> putChar(k, v)
            is String -> putString(k, v)
            is Serializable -> putSerializable(k, v)
            is Parcelable -> putParcelable(k, v)
            else -> ZLog.e("Unsupported format")
        }
    }
}

/** 获取全局的ApplicationContext */
fun getAppContext() = ZPhotoManager.getInstance().getApplicationContext()

/** 返回ToolBar的高度 */
fun getToolBarHeight() = getAppContext().resources.getDimension(R.dimen.toolBarHeight).toInt()

/**
 * 设置状态栏 显示状态
 * @param enable true: 隐藏；false：显示
 */
fun Activity.setFullScreen(enable: Boolean) {
    val lp = window.attributes
    if (enable) lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
    else lp.flags = lp.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
    window.attributes = lp
    window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
}

/** 设置状态栏透明 */
fun Activity.setStatusBarTransparent() {
    val decorView = window.decorView
    val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    decorView.systemUiVisibility = option
    window.statusBarColor = Color.TRANSPARENT
}

/** 获取状态栏高度 */
fun Context.getStatusBarHeight() = resources.getDimensionPixelSize(
    resources.getIdentifier("status_bar_height", "dimen", "android")
)

/** 获取屏幕的宽，高 */
fun Context.getDisplay() = IntArray(2).apply {
    val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val point = Point()
    manager.defaultDisplay.getSize(point)
    this[0] = point.x
    this[1] = point.y
}

/** 返回当前程序版本名 */
fun Context.getAppVersionName() = packageManager.getPackageInfo(packageName, 0).versionName

/** 返回当前程序版本号 */
fun Context.getAppCode() = packageManager.getPackageInfo(packageName, 0).longVersionCode

/** 复制到剪贴板管理器 */
fun Context.copy(content: String) {
    val cmb = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cmb.primaryClip = ClipData.newPlainText(ClipDescription.MIMETYPE_TEXT_PLAIN, content)
}

/** 关闭软键盘 */
fun Activity.closeKeyboard() {
    try {
        val m = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (m.isActive) { // 表示打开
            // 如果打开，则关闭
            m.hideSoftInputFromWindow(currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/** 根据Tag检查是否存在Fragment实例，如果存在就移除！ */
fun AppCompatActivity.checkFragmentByTag(fragmentTag: String) {
    val fragment = supportFragmentManager.findFragmentByTag(fragmentTag)
    if (fragment != null) {
        supportFragmentManager.beginTransaction().remove(fragment).commit()
    }
}

// 资源相关 ================================================

fun dip2pxF(dpValue: Float) = dpValue * getAppContext().resources.displayMetrics.density + 0.5f
fun dip2px(dpValue: Float) = dip2pxF(dpValue).toInt()
fun px2dipF(pxValue: Float) = pxValue / getAppContext().resources.displayMetrics.density + 0.5f
fun px2dip(pxValue: Float) = px2dipF(pxValue).toInt()

fun getColorById(colorID: Int) = ContextCompat.getColor(getAppContext(), colorID)
fun getDimenById(dimenID: Int) = getAppContext().resources.getDimension(dimenID)
fun getStringById(stringID: Int) = getAppContext().resources.getString(stringID)

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

// 列表、集合 ===============================================

internal inline fun <K, E> ArrayMap<K, E>.forEachNoIterable(block: (K, E) -> Unit) {
    var index = 0
    val size = size
    while (index < size) {
        block(keyAt(index), valueAt(index))
        index ++
    }
}

internal inline fun <E> SparseArray<E>.forEach(block: (E) -> Unit) {
    var index = 0
    val size = size()
    while (index < size) {
        block(get(index))
        index ++
    }
}

internal inline fun <E> SparseArray<E>.forEachIndices(block: (E, Int) -> Unit) {
    var index = 0
    val size = size()
    while (index < size) {
        block(get(index), index)
        index ++
    }
}

val SparseArray<*>.indices: IntRange
    get() = 0 until size()

internal inline fun <E> List<E>.forEachNoIterable(block: (E) -> Unit) {
    var index = 0
    val size = size
    while (index < size) {
        block(get(index))
        index ++
    }
}

internal fun <E> LinkedList<E>.forEach(block: (E, Int) -> Unit) {
    var i = size - 1
    while (i > -1) {
        block(get(i), i)
        i = size
        i--
    }
}
