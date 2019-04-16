package com.zp.zphoto_lib.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.util.ArrayMap
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.zp.zphoto_lib.R
import com.zp.zphoto_lib.common.BaseZPhotoActivity
import com.zp.zphoto_lib.common.ZPhotoHelp
import com.zp.zphoto_lib.content.getDisplay
import com.zp.zphoto_lib.content.jumpActivity
import com.zp.zphoto_lib.ui.view.ZPhotoRVDivider
import com.zp.zphoto_lib.util.ZPermission
import com.zp.zphoto_lib.util.ZPhotoImageAnsy
import com.zp.zphoto_lib.util.ZToaster
import kotlinx.android.synthetic.main.activity_zphoto_select.*

class ZPhotoSelectActivity : BaseZPhotoActivity(), Toolbar.OnMenuItemClickListener {

    private var zPhotoPicsSelectAdapter: ZPhotoPicsSelectAdapter? = null
    private var zPhotoDirSelectAdapter: ZPhotoDirSelectAdapter? = null
    private var bottomBehavior: BottomSheetBehavior<View>? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.zphoto_select_menu, menu)
        return true
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.menu_zphoto_select_down) {
            ZPhotoHelp.getInstance().getZImageResultListener()
                ?.selectSuccess(zPhotoPicsSelectAdapter?.getSelectedData())
            finish()
        }
        return true
    }

    override fun getContentView() = R.layout.activity_zphoto_select

    override fun init(savedInstanceState: Bundle?) {
        setBarTitle("选择")

        val noPermissionArray = ZPermission.checkPermission(this, ZPermission.WRITE_EXTERNAL_STORAGE)
        if (noPermissionArray.isNullOrEmpty()) {
            initAll()
        } else {
            ZPermission.requestPermission(this, ZPermission.WRITE_EXTERNAL_CODE, *noPermissionArray)
        }
    }

    private fun initAll() {
        setOnMenuItemClickListener(this)
        initSelectLayout()
        initBottomLayout()

        ZPhotoImageAnsy(this, { dirs, pics ->
            zPhotoPicsSelectAdapter?.setDatas(pics)
            zPhotoDirSelectAdapter?.setDatas(dirs)
        }, true).start()
    }

    private fun initSelectLayout() {
        val spanCount = 3
        zPhotoPicsSelectAdapter = ZPhotoPicsSelectAdapter(this, R.layout.item_zphoto_select_pic, spanCount)
        zPhotoPicsSelectAdapter?.onItemClickListener = {_, position ->
            val item = zPhotoPicsSelectAdapter?.getItem(position)
            if (item?.isVideo == true) {

            } else {

            }
            ZToaster.makeText(position, ZToaster.C, R.color.zphoto_violet)
        }
        zPhotoPicsSelectAdapter?.zPhotoSelectListener = object : ZPhotoPicsSelectAdapter.ZPhotoSelectListener {
            override fun selected(selectedSize: Int) {
                if (selectedSize <= 0) {
                    setBarTitle("选择")
                    getMenu().findItem(R.id.menu_zphoto_select_down).isVisible = false
                } else {
                    setBarTitle("$selectedSize 已选")
                    getMenu().findItem(R.id.menu_zphoto_select_down).isVisible = true
                }
            }
        }
        zphoto_select_picRecyclerView.apply {
            layoutManager = GridLayoutManager(this@ZPhotoSelectActivity, spanCount)
            adapter = zPhotoPicsSelectAdapter
        }
    }

    private fun initBottomLayout() {
        zPhotoDirSelectAdapter = ZPhotoDirSelectAdapter(this, R.layout.item_zphoto_dialog_select)
        zPhotoDirSelectAdapter?.onItemClickListener = { _, i ->
            // 相同的不做处理
            if (zPhotoDirSelectAdapter?.getSelectedByIndex(i) == false) {
                val item = zPhotoDirSelectAdapter?.getItem(i)
                zPhotoPicsSelectAdapter?.setDatas(item?.childs)
                zPhotoDirSelectAdapter?.setSelectedIndex(i)
                bottomBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                zphoto_select_changePicTxt.text = zPhotoDirSelectAdapter?.getItem(i)?.folderName ?: "所有图片"
            }

        }
        zphoto_select_dirRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ZPhotoSelectActivity)
            addItemDecoration(ZPhotoRVDivider.getDefaultDivider(this@ZPhotoSelectActivity))
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
            if (bottomBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
                bottomBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                bottomBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        zphoto_select_previewPicTxt.setOnClickListener {
            if (zPhotoPicsSelectAdapter?.hasSelectedData() == true) {
                val selectList = zPhotoPicsSelectAdapter?.getSelectedData()
                jumpActivity(ZPhotoPreviewActivity::class.java, ArrayMap<String, Any>().apply {
                    put("selectList", selectList)
                }, 0)
                overridePendingTransition(R.anim.anim_zphoto_bottom_in, R.anim.anim_zphoto_bottom_out)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == ZPermission.WRITE_EXTERNAL_CODE) {
            initAll()
        } else {
            ZToaster.makeTextS("权限获取失败")
            finish()
        }
    }

    override fun onBackPressed() {
        if (bottomBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            ZPhotoHelp.getInstance().getZImageResultListener()
                ?.selectCancel()
            super.onBackPressed()
        }
    }
}


