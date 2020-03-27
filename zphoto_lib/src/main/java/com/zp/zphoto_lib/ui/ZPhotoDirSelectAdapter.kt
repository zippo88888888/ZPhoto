package com.zp.zphoto_lib.ui

import android.content.Context
import android.os.Build
import android.util.SparseBooleanArray
import android.widget.ImageView
import com.zp.zphoto_lib.R
import com.zp.zphoto_lib.common.BaseZPhotoAdapter
import com.zp.zphoto_lib.common.BaseZPhotoHolder
import com.zp.zphoto_lib.common.ZPhotoHelp
import com.zp.zphoto_lib.content.ZPhotoFolder
import com.zp.zphoto_lib.content.forEach
import java.io.File

internal class ZPhotoDirSelectAdapter(context: Context, layoutID: Int) : BaseZPhotoAdapter<ZPhotoFolder>(context, layoutID) {

    private val stateArray by lazy {
        SparseBooleanArray()
    }

    override fun bindView(holder: BaseZPhotoHolder, item: ZPhotoFolder, position: Int) {
        holder.apply {
            setTextValue(R.id.item_zphoto_dialog_selectNameTxt, item.folderName)
            setTextValue(R.id.item_zphoto_dialog_selectcountTxt, "${item.childs.size}张")
            getView<ImageView>(R.id.item_zphoto_dialog_selectPic).apply {
                val file = File(item.firstImagePath)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ZPhotoHelp.getInstance().getImageLoaderListener().loadImg(this, item.firstImageUri, file)
                } else {
                    ZPhotoHelp.getInstance().getImageLoaderListener().loadImg(this, file)
                }
            }
            setVisibility(R.id.item_zphoto_dialog_selectCheckPic, stateArray[position])
        }
    }

    override fun setDatas(list: List<ZPhotoFolder>?) {
        list?.forEachIndexed { index, _ ->
            stateArray.put(index, index == 0)
        }
        super.setDatas(list)
    }

    fun getSelectedByIndex(selectedIndex: Int) = stateArray[selectedIndex]

    fun setSelectedIndex(selectedIndex: Int) {
        var oldIndex = -1
        stateArray.forEach {
            if (stateArray[it]) {
                oldIndex = it
                stateArray.put(it, false)
            }
        }
        stateArray.put(selectedIndex, true)
        if (oldIndex != -1) notifyItemChanged(oldIndex)
        notifyItemChanged(selectedIndex)
    }
}