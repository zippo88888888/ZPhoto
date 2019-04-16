package com.zp.zphoto_lib.common

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.ArrayList

abstract class BaseZPhotoAdapter<T>(protected var context: Context) : RecyclerView.Adapter<BaseZPhotoHolder>() {

    constructor(context: Context, layoutID: Int) : this(context) {
        this.layoutID = layoutID
    }

    var onItemClickListener: ((View, Int) -> Unit)? = null

    private var layoutID = -1
    private var datas: ArrayList<T> = ArrayList()

    open fun getDatas() = datas

    /**
     * 设置值
     */
    open fun setDatas(list: List<T>?) {
        clear()
        if (!list.isNullOrEmpty()) {
            if (datas.addAll(list)) {
                notifyDataSetChanged()
            }
        }

    }

    open fun addAll(list: List<T>) {
        val oldSize = itemCount
        if (datas.addAll(list)) {
            notifyItemRangeChanged(oldSize, list.size)
        }
    }

    open fun addItem(position: Int, t: T) {
        datas.add(position, t)
        notifyItemInserted(position)
    }

    open fun setItem(position: Int, t: T) {
        if (itemCount > 0) {
            datas[position] = t
            notifyItemChanged(position)
        }
    }

    open fun remove(position: Int, changeDataNow: Boolean = true) {
        if (itemCount > 0) {
            datas.removeAt(position)
            if (changeDataNow) {
                notifyItemRangeRemoved(position, 1)
            }
        }
    }

    open fun clear(changeDataNow: Boolean = true) {
        datas.clear()
        if (changeDataNow) {
            notifyItemRangeRemoved(0, itemCount)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseZPhotoHolder {
        val layoutRes = getLayoutID(viewType)
        if (layoutRes > 0) {
            val view = LayoutInflater.from(context).inflate(layoutRes, parent, false)
            return BaseZPhotoHolder(view)
        } else {
            throw NullPointerException("adapter layoutId is not null")
        }
    }

    override fun onBindViewHolder(holder: BaseZPhotoHolder, position: Int) {
        holder.setOnItemClickListener(position) { view, i ->
            onItemClickListener?.invoke(view, i)
        }
        bindView(holder, getItem(position), position)
    }

    override fun getItemCount() = datas.size

    fun getItem(position: Int) = datas[position]

    open fun getLayoutID(viewType: Int) = layoutID

    protected abstract fun bindView(holder: BaseZPhotoHolder, item: T, position: Int)

}