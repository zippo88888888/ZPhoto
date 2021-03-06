package com.zp.zphoto_lib.ui

import android.content.Context
import android.os.Build
import android.util.SparseBooleanArray
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.collection.ArrayMap
import com.zp.zphoto_lib.R
import com.zp.zphoto_lib.common.BaseZPhotoAdapter
import com.zp.zphoto_lib.common.BaseZPhotoHolder
import com.zp.zphoto_lib.common.ZPhotoHelp
import com.zp.zphoto_lib.content.*
import com.zp.zphoto_lib.util.ZLog
import com.zp.zphoto_lib.util.ZPhotoUtil
import com.zp.zphoto_lib.util.ZToaster
import java.io.File

internal class ZPhotoPicsSelectAdapter(context: Context, layoutID: Int, spanCount: Int) : BaseZPhotoAdapter<ZPhotoDetail>(context, layoutID) {

    // 保存已选择的图片数据
    private var selectedMap = ArrayMap<String, ZPhotoDetail>()
    // 记录当前box选中的下标
    private var selectedArray = SparseBooleanArray()

    var zPhotoSelectListener: ((Int) -> Unit)? = null

    private val config by lazy {
        ZPhotoHelp.getInstance().getConfiguration()
    }

    private var wh = 0

    init {
        wh = (context.getDisplay()[0] - dip2px(1f) * 2 * spanCount) / spanCount
    }

    override fun bindView(holder: BaseZPhotoHolder, item: ZPhotoDetail, position: Int) {
        holder.apply {
            val durationTxt = getView<TextView>(R.id.item_zphoto_select_videoDurationiTxt)
            val box = getView<CheckBox>(R.id.item_zphoto_select_box)
            val diyBox = getView<TextView>(R.id.item_zphoto_select_txt)
            when (config.selectedBoxStyle) {
                ZPHOTO_BOX_STYLE_DIY -> {
                    diyBox.visibility = View.VISIBLE
                    box.visibility = View.GONE
                    diyBox.isSelected = selectedArray[position]
                    diyBox.setBackgroundResource(R.drawable.zphoto_checkbox_my_selector)
                }
                ZPHOTO_BOX_STYLE_NUM -> {
                    ZLog.e("Please do not use \"ZPHOTO_BOX_STYLE_NUM\" style")
                    diyBox.visibility = View.VISIBLE
                    box.visibility = View.GONE
                    diyBox.isSelected = selectedArray[position]
                    diyBox.setBackgroundResource(R.drawable.zphoto_checkbox_number_selector)
                }
                else -> {
                    diyBox.visibility = View.GONE
                    box.visibility = View.VISIBLE
                    box.isChecked = selectedArray[position]
                }
            }
            if (config.allSelect) { // 能够同时选择图片和视频
                if (config.selectedBoxStyle == ZPHOTO_BOX_STYLE_DEFAULT) {
                    box.visibility = View.VISIBLE
                } else {
                    diyBox.visibility = View.VISIBLE
                }
            } else {
                if (config.selectedBoxStyle == ZPHOTO_BOX_STYLE_DEFAULT) {
                    box.visibility = if (item.isVideo) View.GONE else View.VISIBLE
                } else {
                    diyBox.visibility = if (item.isVideo) View.GONE else View.VISIBLE
                }
            }

            val pic = getView<ImageView>(R.id.item_zphoto_select_pic)
            if (item.isVideo) {
                durationTxt.visibility = View.VISIBLE
                durationTxt.text = ZPhotoUtil.videoDurationFormat(item.duration)
            } else {
                durationTxt.visibility = View.GONE
            }
            val cameraLayout = getView<LinearLayout>(R.id.item_zphoto_select_camearLayout)
            val layoutParams = FrameLayout.LayoutParams(wh, wh).apply {
                gravity = Gravity.CENTER
            }
            pic.layoutParams = layoutParams
            if (item.name == ZPHOTO_SHOW_CAMEAR) {
                cameraLayout.visibility = View.VISIBLE
                cameraLayout.layoutParams = layoutParams
            } else {
                cameraLayout.visibility = View.GONE
                val file = File(item.path)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ZPhotoHelp.getInstance().getImageLoaderListener().loadImg(pic, item.uri, file)
                } else {
                    ZPhotoHelp.getInstance().getImageLoaderListener().loadImg(pic, file)
                }

            }

            box.setOnClickListener {
                boxClick(item, position, box)
            }
            diyBox.setOnClickListener {
                boxClick(item, position, diyBox)
            }
        }
    }

    override fun setDatas(list: List<ZPhotoDetail>?) {
        if (!list.isNullOrEmpty()) {
            selectedArray.clear()
            list.indices.forEach {
                if (selectedMap.isNullOrEmpty()) {
                    selectedArray.put(it, false)
                } else {
                    selectedArray.put(it, selectedMap.containsValue(list[it]))
                }
            }
        }
        super.setDatas(list)
    }

    fun hasSelectedData() = selectedMap.size > 0

    fun getSelectedData() = ArrayList<ZPhotoDetail>().apply {
        for ((_, v) in selectedMap) {
            add(v)
        }
    }

    /**
     * 点击逻辑
     */
    private fun boxClick(item: ZPhotoDetail, position: Int, box: TextView) {
        ZLog.i("选择了 ${item.path}  大小：${item.size}M")
        if (selectedArray[position]) { // 选中-->>不选中
            if (selectedMap.contains(item.path)) { // 包含删除
                selectedMap.remove(item.path)
                selectedArray.put(position, !selectedArray[position])
                zPhotoSelectListener?.invoke(selectedMap.size)
                if (box !is CheckBox) {
                    box.isSelected = false
                    if (config.selectedBoxStyle == ZPHOTO_BOX_STYLE_NUM) {
                        box.text = ""
                    }
                }
            } else {
                ZLog.e("当前取消选中的不在map集合里面")
            }
        } else { // 不选中-->>选中
            val counts = getSelectPicOrVideoCount()
            if (item.isVideo) {
                // 判断视频大小
                if (item.size > config.maxVideoSize) {
                    ZToaster.makeTextS(getTipStr(R.string.zphoto_video_size_tip, config.maxVideoSize))
                    if (box is CheckBox) box.isChecked = false
                    else box.isSelected = false
                    return
                }
                val videoCount = counts.second
                if (videoCount >= config.maxVideoSelect) {
                    ZToaster.makeTextS(getTipStr(R.string.zphoto_video_count_tip, config.maxVideoSelect))
                    if (box is CheckBox) box.isChecked = false
                    else box.isSelected = false
                } else {
                    if (box !is CheckBox) {
                        box.isSelected = true
                        if (config.selectedBoxStyle == ZPHOTO_BOX_STYLE_NUM) {
                            box.text = "${selectedMap.size + 1}"
                        }
                    }
                    selectedMap[item.path] = item
                    selectedArray.put(position, !selectedArray[position])
                    zPhotoSelectListener?.invoke(selectedMap.size)
                }
            } else {
                // 判断图片大小
                if (item.size > config.maxPicSize) {
                    ZToaster.makeTextS(getTipStr(R.string.zphoto_pic_size_tip, config.maxPicSize))
                    if (box is CheckBox) box.isChecked = false
                    else box.isSelected = false
                    return
                }
                val picCount = counts.first
                if (picCount >= config.maxPicSelect) {
                    ZToaster.makeTextS(getTipStr(R.string.zphoto_pic_count_tip, config.maxPicSelect))
                    if (box is CheckBox) box.isChecked = false
                    else box.isSelected = false
                } else {
                    if (box !is CheckBox) {
                        box.isSelected = true
                        if (config.selectedBoxStyle == ZPHOTO_BOX_STYLE_NUM) {
                            box.text = "${selectedMap.size + 1}"
                        }
                    }
                    selectedMap[item.path] = item
                    selectedArray.put(position, !selectedArray[position])
                    zPhotoSelectListener?.invoke(selectedMap.size)
                }
            }
        }

    }

    /**
     * 获取已选中的图片和视频的数量
     */
    private fun getSelectPicOrVideoCount(): Pair<Int, Int> {
        var picCount = 0
        var videoCount = 0
        for ((_, v) in selectedMap) {
            if (v.isVideo) {
                videoCount ++
            } else {
                picCount ++
            }
        }
        return Pair(picCount, videoCount)
    }

}