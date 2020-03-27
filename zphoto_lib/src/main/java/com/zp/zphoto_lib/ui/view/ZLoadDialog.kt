package com.zp.zphoto_lib.ui.view

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.zp.zphoto_lib.R
import com.zp.zphoto_lib.content.dip2px
import com.zp.zphoto_lib.content.getColorById
import com.zp.zphoto_lib.content.getTextValue

internal class ZLoadDialog(context: Context, private var title: String? = getTextValue(R.string.zphoto_loadding)) : AlertDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val wh = dip2px(136f)
        setContentView(getContentView(wh))
    }

    private fun getContentView(wh: Int) = LinearLayout(context).apply {
        window?.setLayout(wh, wh)
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER

        val barWh = dip2px(45f)
        val bar = ProgressBar(context).run {
            layoutParams  = LinearLayout.LayoutParams(barWh, barWh)
            this
        }
        addView(bar)
        val padding = dip2px(14f)
        val titleTxt = TextView(context).run {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
            setPadding(0, padding, 0, 0)
            textSize = 13f
            setTextColor(getColorById(R.color.black))
            text = if (title.isNullOrEmpty()) getTextValue(R.string.zphoto_loadding) else title
            this
        }
        addView(titleTxt)
    }

    override fun dismiss() {
        System.gc()
        super.dismiss()
    }

}