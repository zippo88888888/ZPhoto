package com.zp.zphoto_lib.ui

import android.content.Context
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import com.zp.zphoto_lib.R
import com.zp.zphoto_lib.common.BaseZPhotoAdapter
import com.zp.zphoto_lib.common.BaseZPhotoHolder
import com.zp.zphoto_lib.common.ZPhotoHelp
import com.zp.zphoto_lib.content.ZPhotoFolder
import com.zp.zphoto_lib.content.dip2px
import com.zp.zphoto_lib.content.getDisplay
import java.io.File

class ZPhotoPicsSelectAdapter(context: Context, layoutID: Int) : BaseZPhotoAdapter<ZPhotoFolder>(context, layoutID) {

    private var wh = 0

    init {
        wh = (context.getDisplay()[0] - dip2px(1f) * 2 * 2) / 2
    }

    override fun bindView(holder: BaseZPhotoHolder, item: ZPhotoFolder, position: Int) {
        holder.apply {
            setTextValue(R.id.item_zphoto_select_nameTxt, item.folderName)
            getView<ImageView>(R.id.item_zphoto_select_pic).apply {
                layoutParams = FrameLayout.LayoutParams(wh, wh).apply {
                    gravity = Gravity.CENTER
                }
                ZPhotoHelp.getInstance().getImageLoaderListener().loadImg(this, File(item.firstImagePath))
            }
        }
    }
}