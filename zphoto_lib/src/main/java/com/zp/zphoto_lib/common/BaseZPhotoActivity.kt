package com.zp.zphoto_lib.common

import android.app.Dialog
import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.LinearLayout
import android.widget.TextView
import com.zp.zphoto_lib.R
import com.zp.zphoto_lib.content.getColorById

abstract class BaseZPhotoActivity : AppCompatActivity() {

    protected lateinit var toolbar: Toolbar
    protected lateinit var titleTxt: TextView

    private var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = getContentView()
        if (id <= 0) throw NullPointerException("Activity contentView is not null")
        setContentView(id)
        if (getBarState()) {
            initBar()
        }
        init(savedInstanceState)
    }

    abstract fun getContentView(): Int

    open protected fun getBarState() = true

    abstract fun init(savedInstanceState: Bundle?)

    private fun createDialog(): Dialog {
        val dialog = ProgressDialog(this)
        dialog.setMessage("Loading...")
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        dialog.window!!.setWindowAnimations(android.R.style.Animation_Translucent)
        return dialog
    }

    fun showDialog(cancelable: Boolean?) {
        if (dialog != null) {
            dialog!!.dismiss()
            dialog = null
        }
        if (dialog == null) {
            dialog = createDialog()
        }
        dialog?.setCanceledOnTouchOutside(false)
        if (cancelable != null) {
            dialog?.setCancelable(cancelable)
        }
        dialog?.show()
    }

    fun dismissDialog() {
        if (dialog != null && dialog?.isShowing == true) {
            dialog?.dismiss()
            dialog = null
        }
    }

    private fun initBar() {
        toolbar = findViewById(R.id.tool_bar)
        titleTxt = findViewById(R.id.tool_bar_title)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { back() }
    }


    protected fun setBarTitle(title: String) {
        supportActionBar?.title = ""
        titleTxt.text = title
    }

    protected fun setNavigationIcon(icon: Int) {
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        toolbar.setNavigationIcon(icon)
        toolbar.setNavigationOnClickListener { back() }
    }

    protected fun setBarBgColor(color: Int) {
        val rColor = getColorById(color)
        findViewById<LinearLayout>(R.id.tool_app_barLayout).setBackgroundColor(rColor)
    }

    protected fun setBarBgColor(color: String) {
        val rColor = Color.parseColor(color)
        findViewById<LinearLayout>(R.id.tool_app_barLayout).setBackgroundColor(rColor)
    }

    protected fun setBarAlpha(alpha: Int) {
        findViewById<LinearLayout>(R.id.tool_app_barLayout).background.alpha = alpha
    }

    protected fun removeBarBgColor() {
        findViewById<LinearLayout>(R.id.tool_app_barLayout).setBackgroundResource(0)
    }


    protected fun setOnMenuItemClickListener(listener: Toolbar.OnMenuItemClickListener) {
        toolbar.setOnMenuItemClickListener(listener)
    }

    protected fun back() {
        onBackPressed()
    }
}