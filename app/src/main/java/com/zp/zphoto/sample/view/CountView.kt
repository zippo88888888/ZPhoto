package com.zp.zphoto.sample.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.zp.zphoto.R

class CountView : LinearLayout, View.OnClickListener {

    private lateinit var valueTxt: TextView

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.layout_count_view, this)
        findViewById<TextView>(R.id.count_layout_add).setOnClickListener(this)
        findViewById<TextView>(R.id.count_layout_subtract).setOnClickListener(this)
        valueTxt = findViewById(R.id.count_layout_edit)
        valueTxt.setOnClickListener(this)

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.count_layout_add -> {
                var count = valueTxt.text.toString().toInt()
                valueTxt.text = "${++count}"
            }
            R.id.count_layout_subtract -> {
                val count = valueTxt.text.toString().toInt()
                if (count - 1 <= 0) {
                    valueTxt.text = "1"
                } else {
                    valueTxt.text = "${count - 1}"
                }
            }
        }
    }

    fun getValue() = valueTxt.text.toString().toInt()

    fun setValue(value: Int) {
        valueTxt.text = "$value"
    }

}