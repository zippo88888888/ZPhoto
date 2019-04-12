package com.zp.zphoto_lib.ui

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import com.zp.zphoto_lib.R
import com.zp.zphoto_lib.common.BaseZPhotoActivity
import com.zp.zphoto_lib.content.ZPhotoFolder
import com.zp.zphoto_lib.util.ZPhotoImageTask
import com.zp.zphoto_lib.util.ZToaster
import kotlinx.android.synthetic.main.activity_zphoto_select.*
import android.support.design.widget.BottomSheetBehavior
import android.view.View
import com.zp.zphoto_lib.content.Z_ALL_VIDEO_KEY
import com.zp.zphoto_lib.content.getDisplay
import com.zp.zphoto_lib.util.RecyclerViewDivider

class ZPhotoSelectActivity : BaseZPhotoActivity() {

    private var pics: ArrayList<ZPhotoFolder>? = null

    private var zPhotoPicsSelectAdapter: ZPhotoPicsSelectAdapter? = null
    private var zPhotoDirSelectAdapter: ZPhotoDirSelectAdapter? = null
    private var bottomBehavior: BottomSheetBehavior<View>? = null

    override fun getContentView() = R.layout.activity_zphoto_select

    override fun init(savedInstanceState: Bundle?) {
        setBarTitle("选择")
        zPhotoPicsSelectAdapter = ZPhotoPicsSelectAdapter(this, R.layout.item_zphoto_select_pic)
        zPhotoPicsSelectAdapter?.onItemClickListener = {_, position ->
            ZToaster.makeText(position, ZToaster.C, R.color.zphoto_violet)
        }
        zphoto_select_picRecyclerView.apply {
            layoutManager = GridLayoutManager(this@ZPhotoSelectActivity,2)
            adapter = zPhotoPicsSelectAdapter
        }
        initBottomLayout()

        ZPhotoImageTask(this, {
            pics = ArrayList<ZPhotoFolder>().apply {
                for ((k, v) in it) {
                    if (k == Z_ALL_VIDEO_KEY) {
                        add(ZPhotoFolder("videoPath", v[0].path, "所有视频", v))
                    } else {
                        add(ZPhotoFolder(k, v[0].path, k.substring(k.lastIndexOf("/") + 1, k.length), v))
                    }
                }
            }
            pics?.sortByDescending { it.childs[0].date_modified }
            zPhotoPicsSelectAdapter?.setDatas(pics)
            zPhotoDirSelectAdapter?.setDatas(pics)
        }).execute()
    }

    private fun initBottomLayout() {
        zPhotoDirSelectAdapter = ZPhotoDirSelectAdapter(this, R.layout.item_zphoto_dialog_select)
        zPhotoDirSelectAdapter?.onItemClickListener = { _, i ->
            // 相同的不做处理
            if (zPhotoDirSelectAdapter?.getSelectedByIndex(i) == false) {
                zPhotoDirSelectAdapter?.setSelectedIndex(i)
                bottomBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                zphoto_select_changePicTxt.text = zPhotoDirSelectAdapter?.getItem(i)?.folderName ?: "全部相册"
            }

        }
        zphoto_select_dirRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ZPhotoSelectActivity)
            addItemDecoration(RecyclerViewDivider.getDefaultDivider(this@ZPhotoSelectActivity))
            isNestedScrollingEnabled = false
            adapter = zPhotoDirSelectAdapter
        }

        val height = getDisplay()[1]
        // 初始化底部
        bottomBehavior = BottomSheetBehavior.from(zphoto_select_nestedScrollView)
        bottomBehavior?.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // 1 张开 0 关闭
                val changeY = height + (zphoto_select_bottomLayout.height) *
                        slideOffset - zphoto_select_bottomLayout.height
                zphoto_select_bottomLayout.y = changeY
            }
            override fun onStateChanged(bottomSheet: View, newState: Int) = Unit
            /*when (newState) {
                BottomSheetBehavior.STATE_DRAGGING -> ZLog.e("开始拖拽 STATE_DRAGGING--->>>$newState")
                BottomSheetBehavior.STATE_SETTLING -> ZLog.e("惯性滚动 STATE_SETTLING--->>>$newState")
                BottomSheetBehavior.STATE_EXPANDED -> ZLog.e("展开 STATE_EXPANDED--->>>$newState")
                BottomSheetBehavior.STATE_COLLAPSED -> ZLog.e("收缩 STATE_COLLAPSED--->>>$newState")
                BottomSheetBehavior.STATE_HIDDEN -> ZLog.e("隐藏 STATE_HIDDEN--->>>$newState")
                BottomSheetBehavior.STATE_HALF_EXPANDED -> ZLog.e("STATE_HALF_EXPANDED--->>>$newState")
            }*/
        })
        zphoto_select_changePicTxt.setOnClickListener {
            if (bottomBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) { // 张开
                bottomBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                bottomBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun onBackPressed() {
        if (bottomBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) { // 张开
            bottomBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            super.onBackPressed()
        }
    }
}


