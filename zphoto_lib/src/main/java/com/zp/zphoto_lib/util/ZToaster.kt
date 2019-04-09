package com.zp.zphoto_lib.util

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.zp.zphoto_lib.R
import com.zp.zphoto_lib.content.*

class ZToaster(con: Context) : Toast(con) {

    companion object {

        const val T = Gravity.TOP
        const val B = Gravity.BOTTOM
        const val C = Gravity.CENTER

        const val SHORT = Toast.LENGTH_SHORT
        const val LONG = Toast.LENGTH_LONG

        private var toast: Toast? = null

        private fun checkToast() {
            if (toast != null) toast?.cancel()
        }

        /**
         * 系统自带的 消息提醒
         */
        fun makeTextS(str: Any, duration: Int = SHORT) {
            checkToast()
            toast = makeText(getAppContext(), getTextValue(str), duration)
            toast?.show()
        }

        /**
         * 自定义 消息提醒
         * @param str           消息内容
         * @param location      位置
         * @param duration      显示时间
         * @param bgColor       背景颜色
         * @param textColor     文字颜色
         *
         */
        fun makeText(
            str: Any,
            location: Int = T,
            duration: Int = SHORT,
            bgColor: Int = R.color.red,
            textColor: Int = R.color.white
        ) {
            checkToast()
            toast = Toast(getAppContext())
            toast?.duration = duration
            if (location != T && location != B && location != C) {
                throw IllegalArgumentException("Toaster location only is CENTER TOP or BOTTOM")
            }
            if (location == T) toast?.setGravity(location, 0, getToolBarHeight() - getAppContext().getStatusBarHeight())
            else toast?.setGravity(location, 0, 0)
            toast?.view = LayoutInflater.from(getAppContext()).inflate(R.layout.layout_toast, null).apply {
                alpha = 0.8f
                translationY = -300f
                animate().translationY(0f).duration = 300
                findViewById<TextView>(R.id.toast_msg).apply {
                    text = getTextValue(str)
                    setTextColor(getColorById(textColor))
                    setBackgroundColor(getColorById(bgColor))
                    layoutParams =
                        LinearLayout.LayoutParams(getAppContext().getDisplay()[0], ViewGroup.LayoutParams.WRAP_CONTENT)
                }
            }
            toast?.show()
        }

    }
}