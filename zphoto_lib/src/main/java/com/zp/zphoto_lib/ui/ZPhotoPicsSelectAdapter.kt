package com.zp.zphoto_lib.ui

import android.content.Context
import android.support.v4.util.ArrayMap
import android.util.SparseBooleanArray
import android.view.Gravity
import android.view.View
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.zp.zphoto_lib.R
import com.zp.zphoto_lib.common.BaseZPhotoAdapter
import com.zp.zphoto_lib.common.BaseZPhotoHolder
import com.zp.zphoto_lib.common.ZPhotoHelp
import com.zp.zphoto_lib.content.*
import com.zp.zphoto_lib.util.ZLog
import com.zp.zphoto_lib.util.ZPhotoUtil
import java.io.File

class ZPhotoPicsSelectAdapter(context: Context, layoutID: Int, spanCount: Int) : BaseZPhotoAdapter<ZPhotoDetail>(context, layoutID) {

    private var selectedMap = ArrayMap<String, ZPhotoDetail>()
    private var selectedArray = SparseBooleanArray()

    var zPhotoSelectListener: ZPhotoSelectListener? = null


    private var wh = 0

    init {
        wh = (context.getDisplay()[0] - dip2px(1f) * 2 * spanCount) / spanCount
    }

    override fun bindView(holder: BaseZPhotoHolder, item: ZPhotoDetail, position: Int) {
        holder.apply {
            val durationTxt = getView<TextView>(R.id.item_zphoto_select_videoDurationiTxt)
            val box = getView<CheckBox>(R.id.item_zphoto_select_box)
            box.isChecked = selectedArray[position]
            val pic = getView<ImageView>(R.id.item_zphoto_select_pic)
            if (item.isVideo) {
                durationTxt.visibility = View.VISIBLE
                durationTxt.text = ZPhotoUtil.videoDurationFormat(item.duration)
            } else {
                durationTxt.visibility = View.GONE
            }

            pic.apply {
                layoutParams = FrameLayout.LayoutParams(wh, wh).apply {
                    gravity = Gravity.CENTER
                }
                ZPhotoHelp.getInstance().getImageLoaderListener().loadImg(this, File(item.path))
            }

            box.setOnClickListener {
                if (selectedArray[position]) { // 选中-->>不选中
                    if (selectedMap.contains(item.path)) { // 包含删除
                        selectedMap.remove(item.path)
                    }
                } else { // 不选中-->>选中
                    selectedMap[item.path] = item
                    ZLog.e("选中 ${item.path}  大小：${item.size}M")
                }
                selectedArray.put(position, !selectedArray[position])
                zPhotoSelectListener?.selected(selectedMap.size)
            }
        }
    }

    override fun setDatas(list: List<ZPhotoDetail>?) {
        if (!list.isNullOrEmpty()) {
            selectedArray.clear()
            list.forEachIndexed { i, _ -> selectedArray.put(i, false) }
        }
        super.setDatas(list)
    }

    fun getSelectedData() = ArrayList<ZPhotoDetail>().apply {
        for ((_, v) in selectedMap) {
            add(v)
        }
    }

    interface ZPhotoSelectListener {
        fun selected(selectedSize: Int)
    }
}