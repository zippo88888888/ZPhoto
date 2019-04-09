package com.zp.zphoto_lib.common

import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.zp.zphoto_lib.content.getTextValue

@Suppress("UNCHECKED_CAST")
class BaseZPhotoHolder(itemView: View)  : RecyclerView.ViewHolder(itemView) {

    private var arrayView = SparseArray<View>()

    fun <V : View> getView(id :Int): V {
        var view = arrayView[id]
        if (view == null) {
            view = itemView.findViewById(id)
            arrayView.put(id, view)
        }
        return view as V
    }

    fun setTextValue(id: Int, str: Any) {
        getView<TextView>(id).text = getTextValue(str)
    }

    fun setVisibility(id: Int, isShow: Boolean) {
        setVisibility(id, if (isShow) View.VISIBLE else View.GONE)
    }

    fun setVisibility(id: Int, visibility: Int) {
        getView<View>(id).visibility = visibility
    }

    fun setOnClickListener(id: Int, block: (View) -> Unit) {
        getView<View>(id).setOnClickListener { block(it) }
    }

    fun setOnItemClickListener(position: Int, block: (View, Int) -> Unit) {
        itemView.setOnClickListener { block(it, position) }
    }

    fun setOnItemLongClickListener(position: Int, block: (View, Int) -> Boolean) {
        itemView.setOnLongClickListener { block(it, position) }
    }

    fun setOnTouchListener(position: Int, block: (View, MotionEvent, Int) -> Boolean) {
        itemView.setOnTouchListener { v, event -> block(v, event, position) }
    }
}