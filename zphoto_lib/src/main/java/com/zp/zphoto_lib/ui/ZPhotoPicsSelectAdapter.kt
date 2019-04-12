package com.zp.zphoto_lib.ui

import android.content.Context
import android.util.SparseBooleanArray
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.zp.zphoto_lib.R
import com.zp.zphoto_lib.common.BaseZPhotoAdapter
import com.zp.zphoto_lib.common.BaseZPhotoHolder
import com.zp.zphoto_lib.common.ZPhotoHelp
import com.zp.zphoto_lib.content.ZPhotoDetail
import com.zp.zphoto_lib.content.ZPhotoFolder
import com.zp.zphoto_lib.content.dip2px
import com.zp.zphoto_lib.content.getDisplay
import java.io.File

class ZPhotoPicsSelectAdapter(context: Context, layoutID: Int, spanCount: Int) : BaseZPhotoAdapter<ZPhotoDetail>(context, layoutID) {

    private var selectedArray = SparseBooleanArray()

    private var wh = 0

    init {
        wh = (context.getDisplay()[0] - dip2px(1f) * 2 * spanCount) / spanCount
    }

    override fun bindView(holder: BaseZPhotoHolder, item: ZPhotoDetail, position: Int) {
        holder.apply {
            val durationTxt = getView<TextView>(R.id.item_zphoto_select_videoDurationiTxt)
            if (item.isVideo) {
                durationTxt.visibility = View.VISIBLE
                durationTxt.text = item.duration.toString()
            } else {
                durationTxt.visibility = View.GONE
                if (item.isGif) {

                } else {

                }
            }

            getView<ImageView>(R.id.item_zphoto_select_pic).apply {
                layoutParams = FrameLayout.LayoutParams(wh, wh).apply {
                    gravity = Gravity.CENTER
                }
                ZPhotoHelp.getInstance().getImageLoaderListener().loadImg(this, File(item.path))
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
}