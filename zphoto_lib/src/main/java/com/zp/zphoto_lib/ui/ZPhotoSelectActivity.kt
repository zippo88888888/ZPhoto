package com.zp.zphoto_lib.ui

import android.app.Activity
import android.content.Intent
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
import com.zp.zphoto_lib.common.ZPhotoManager
import com.zp.zphoto_lib.content.*
import com.zp.zphoto_lib.ui.view.ZPhotoRVDivider
import com.zp.zphoto_lib.util.ZPermission
import com.zp.zphoto_lib.util.ZPhotoImageAnsy
import com.zp.zphoto_lib.util.ZToaster
import kotlinx.android.synthetic.main.activity_zphoto_select.*

internal class ZPhotoSelectActivity : BaseZPhotoActivity(), Toolbar.OnMenuItemClickListener {

    private var zPhotoPicsSelectAdapter: ZPhotoPicsSelectAdapter? = null
    private var zPhotoDirSelectAdapter: ZPhotoDirSelectAdapter? = null
    private var bottomBehavior: BottomSheetBehavior<View>? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.zphoto_select_menu, menu)
        return true
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.menu_zphoto_select_down) {
            disposePicData(zPhotoPicsSelectAdapter!!.getSelectedData(), ZPHOTO_SELECT_PIC_BACK_CODE)
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
            ZPermission.requestPermission(this, ZPHOTO_WRITE_EXTERNAL_CODE, *noPermissionArray)
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
            val item = zPhotoPicsSelectAdapter!!.getItem(position)
            if (item.name == ZPHOTO_SHOW_CAMEAR) { // 拍照
                ZPhotoHelp.getInstance().toCamera(this)
            } else {
                val config = ZPhotoHelp.getInstance().getConfiguration()
                // 判断第0个
                val firstItem = zPhotoPicsSelectAdapter!!.getItem(0)
                val hasCamera = firstItem.name == ZPHOTO_SHOW_CAMEAR
                val index = if (hasCamera) position - 1 else position
                if (config.allSelect) { // 视频和图片可以同时选择
                    ZPhotoManager.getInstance().setAllList(zPhotoPicsSelectAdapter?.getDatas())
                    val selectList = zPhotoPicsSelectAdapter?.getSelectedData()
                    jumpActivity(ZPhotoPreviewActivity::class.java, ArrayMap<String, Any>().apply {
                        put("selectIndex", index)
                        put("selectList", selectList)
                        put("needAllList", true)
                    }, ZPHOTO_PREVIEW_REQUEST_CODE)
                } else { // 不可以同时选择
                    if (item.isVideo) {
                        // 直接跳转视频预览，且默认选中的就是点击的视频
                        jumpActivity(ZPhotoPreviewActivity::class.java, ArrayMap<String, Any>().apply {
                            put("isVideo", true)
                            put("selectList", ArrayList<ZPhotoDetail>().apply {
                                add(item)
                            })
                        }, ZPHOTO_PREVIEW_REQUEST_CODE)
                    } else {
                        ZPhotoManager.getInstance().setAllList(zPhotoPicsSelectAdapter?.getDatas())
                        val selectList = zPhotoPicsSelectAdapter?.getSelectedData()
                        jumpActivity(ZPhotoPreviewActivity::class.java, ArrayMap<String, Any>().apply {
                            put("selectIndex", index)
                            put("selectList", selectList)
                            put("needAllList", true)
                        }, ZPHOTO_PREVIEW_REQUEST_CODE)
                    }
                }
                overridePendingTransition(R.anim.anim_zphoto_bottom_in, R.anim.anim_zphoto_bottom_out)
            }
        }
        zPhotoPicsSelectAdapter?.zPhotoSelectListener = { selectedSize ->
            if (selectedSize <= 0) {
                setBarTitle("选择")
                getMenu().findItem(R.id.menu_zphoto_select_down).isVisible = false
            } else {
                setBarTitle("$selectedSize 已选")
                getMenu().findItem(R.id.menu_zphoto_select_down).isVisible = true
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
                    put("selectIndex", 0)
                    put("selectList", selectList)
                    put("needAllList", false)
                }, ZPHOTO_PREVIEW_REQUEST_CODE)
                overridePendingTransition(R.anim.anim_zphoto_bottom_in, R.anim.anim_zphoto_bottom_out)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ZPHOTO_PREVIEW_REQUEST_CODE && resultCode == ZPHOTO_PREVIEW_RESULT_CODE) {
            if (data != null) {
                val selectList = data.getParcelableArrayListExtra<ZPhotoDetail>("selectList")
                disposePicData(selectList, ZPHOTO_SELECT_PIC_BACK_CODE)
            }
        } else if (requestCode == ZPHOTO_TO_CAMEAR_REQUEST_CODE) { // 拍照
            if (resultCode == Activity.RESULT_OK) {
                // 需要将已选中的图片信息返回出去
                val selectData = zPhotoPicsSelectAdapter?.getSelectedData()
                if (!selectData.isNullOrEmpty()) {
                    val intent = data ?: Intent()
                    intent.putParcelableArrayListExtra("selectData", selectData)
                    ZPhotoHelp.getInstance().onActivityResult(requestCode, resultCode, intent, this)
                } else {
                    ZPhotoHelp.getInstance().onActivityResult(requestCode, resultCode, data, this)
                }
//                finish()
            } else {
                ZToaster.makeTextS("用户取消")
            }
        } else if (requestCode == ZPHOTO_CROP_REQUEST_CODE) { // 剪裁
            ZPhotoHelp.getInstance().nextCropCheck(this, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ZPHOTO_WRITE_EXTERNAL_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAll()
                } else {
                    ZToaster.makeTextS("权限获取失败")
                    finish()
                }
            }
            ZPHOTO_CAMEAR_CODE ->
                ZPhotoHelp.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults, this)
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

    override fun finish() {
        ZPhotoManager.getInstance().clearAllList()
        super.finish()
    }

    /**
     * 处理图片
     * GIF和视频 不能剪裁和压缩
     */
    private fun disposePicData(pics: ArrayList<ZPhotoDetail>, requestCode: Int) {
        val intent = Intent()
        // 需要将已选中的图片信息返回出去
        intent.putParcelableArrayListExtra("selectData", pics)
        ZPhotoHelp.getInstance().onActivityResult(requestCode, Activity.RESULT_OK, intent, this)
    }
}


