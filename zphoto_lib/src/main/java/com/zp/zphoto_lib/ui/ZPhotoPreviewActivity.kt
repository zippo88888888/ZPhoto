package com.zp.zphoto_lib.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.zp.zphoto_lib.R
import com.zp.zphoto_lib.common.BaseZPhotoActivity
import com.zp.zphoto_lib.common.ZPhotoHelp
import com.zp.zphoto_lib.common.ZPhotoManager
import com.zp.zphoto_lib.content.ZPHOTO_PREVIEW_RESULT_CODE
import com.zp.zphoto_lib.content.ZPhotoDetail
import com.zp.zphoto_lib.content.forEachNoIterable
import com.zp.zphoto_lib.content.getTipStr
import com.zp.zphoto_lib.ui.view.ZPhotoVideoPlayer
import com.zp.zphoto_lib.util.ZLog
import com.zp.zphoto_lib.util.ZToaster
import kotlinx.android.synthetic.main.activity_zphoto_preview.*

class ZPhotoPreviewActivity : BaseZPhotoActivity(), ViewPager.OnPageChangeListener, Toolbar.OnMenuItemClickListener {

    private var previewAdapter: ZPhotoPreviewAdapter? = null
    // 当前已选中的数据
    private var selectList = ArrayList<ZPhotoDetail>()

    // 上个界面选中的数据
    private var lastPageSelectList: ArrayList<ZPhotoDetail>? = null
    // 是否需要显示所有数据，false表示使用上个界面选中的数据
    private var needAllList = false
    // 所有数据
    private var allList: ArrayList<ZPhotoDetail>? = null
    // 上个界面选中的下标
    private var lastPageSelectIndex = 0
    // ViewPager 上一个选中的下标
    private var lastIndex = 0
    // 数据是否只有一个视频
    private var isVideo = false

    private val count = ZPhotoHelp.getInstance().getConfiguration().run {
        if (allSelect && showVideo) {
            this.maxPicSelect + this.maxVideoSelect
        } else {
            this.maxPicSelect
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.zphoto_select_menu, menu)
        menu?.findItem(R.id.menu_zphoto_select_down)?.let {
            it.isVisible = true
            it.title = "确定"
        }
        return true
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.menu_zphoto_select_down) {
            if (selectList.isNullOrEmpty()) {
                ZToaster.makeTextS("请至少选择一个")
                return true
            }
            setResult(ZPHOTO_PREVIEW_RESULT_CODE, Intent(this,ZPhotoSelectActivity::class.java).apply {
                putExtras(Bundle().apply {
                    putParcelableArrayListExtra("selectList", selectList)
                })
            })
            finish()
        }
        return true
    }

    override fun getContentView() = R.layout.activity_zphoto_preview

    override fun init(savedInstanceState: Bundle?) {
        isVideo = intent.getBooleanExtra("isVideo", false)
        lastPageSelectList = intent.getParcelableArrayListExtra("selectList")
        if (!lastPageSelectList.isNullOrEmpty()) {
            selectList.addAll(lastPageSelectList!!)
        }
        if (isVideo) { // 单个视频处理
            setBarTitle("视频预览")
            zphoto_preview_bottomLayout.visibility = View.GONE
            previewAdapter = ZPhotoPreviewAdapter(this, lastPageSelectList)
        } else {
            zphoto_preview_bottomLayout.visibility = View.VISIBLE

            lastPageSelectIndex = intent.getIntExtra("selectIndex", 0)
            needAllList = intent.getBooleanExtra("needAllList", false)

            if (needAllList) { // 需要展示所有图片信息
                allList = ZPhotoManager.getInstance().getAllList()
                if (!allList.isNullOrEmpty()) {
                    previewAdapter = ZPhotoPreviewAdapter(this, allList)
                    setBarTitle("${lastPageSelectIndex + 1}/${allList?.size ?: 0}")
                } else {
                    ZLog.e("获取所有图片数据异")
                }
            } else { // 只需要展示已选中的图片信息
                previewAdapter = ZPhotoPreviewAdapter(this, lastPageSelectList)
                setBarTitle("${lastPageSelectIndex + 1}/${lastPageSelectList?.size ?: 0}")
            }
            // 设置底部选中的 数量
            zphoto_preview_selectTxt.text = "${lastPageSelectList?.size ?: 0}/$count"
        }
        setNavigationIcon(R.drawable.ic_close_black)
        setOnMenuItemClickListener(this)

        zphoto_preview_vp.adapter = previewAdapter
        if (!isVideo) zphoto_preview_vp.setCurrentItem(lastPageSelectIndex, false)
        zphoto_preview_vp.addOnPageChangeListener(this)

        zphoto_preview_box.setOnClickListener {
            val item = if (needAllList) {
                allList!![zphoto_preview_vp.currentItem]
            } else {
                lastPageSelectList!![zphoto_preview_vp.currentItem]
            }
            if (zphoto_preview_box.isChecked) { // 选中
                if (item.isVideo) {
                    // 判断视频大小
                    if (item.size > ZPhotoHelp.getInstance().getConfiguration().maxVideoSize) {
                        ZToaster.makeTextS(getTipStr(R.string.zphoto_video_size_tip, ZPhotoHelp.getInstance().getConfiguration().maxVideoSize))
                        zphoto_preview_box.isChecked = false
                    } else {
                        // 判断数量
                        val videoSelectCount = getSelectPicOrVideoCount().first
                        if (videoSelectCount >= getVideoMaxSelectCount()) {
                            ZToaster.makeTextS(getTipStr(R.string.zphoto_video_count_tip, getVideoMaxSelectCount()))
                            zphoto_preview_box.isChecked = false
                        } else {
                            selectList.add(item)
                        }
                    }

                } else {

                    // 判断图片大小
                    if (item.size > ZPhotoHelp.getInstance().getConfiguration().maxPicSize) {
                        ZToaster.makeTextS(getTipStr(R.string.zphoto_pic_size_tip, ZPhotoHelp.getInstance().getConfiguration().maxPicSize))
                        zphoto_preview_box.isChecked = false
                    } else {
                        val picSelectCount = getSelectPicOrVideoCount().second
                        if (picSelectCount >= getPicMaxSelectCount()) {
                            ZToaster.makeTextS(getTipStr(R.string.zphoto_pic_count_tip, getPicMaxSelectCount()))
                            zphoto_preview_box.isChecked = false
                        } else {
                            selectList.add(item)
                        }
                    }
                }

            } else { // 取消选中
                if (selectList.contains(item)) {
                    selectList.remove(item)
                }
            }
            // 设置底部选中的 数量
            zphoto_preview_selectTxt.text = "${selectList?.size}/$count"
        }

        // 第一次进来判断 当前是否已经选中
        val item = if (needAllList) {
            allList!![zphoto_preview_vp.currentItem]
        } else {
            lastPageSelectList!![zphoto_preview_vp.currentItem]
        }
        val checked = selectList.contains(item)
        zphoto_preview_box.isChecked = checked
    }

    override fun onPageScrollStateChanged(position: Int) = Unit
    override fun onPageScrolled(position: Int, f: Float, i: Int) = Unit

    override fun onPageSelected(position: Int) {
        if (!isVideo) {
            val item: ZPhotoDetail
            if (needAllList) {
                item = allList!![position]
                // 设置是否选中
                zphoto_preview_box.isChecked = selectList.contains(item)
                // 设置头部下标显示
                setBarTitle("${position + 1}/${allList?.size ?: 0}")
            } else {
                item = lastPageSelectList!![position]
                // 设置是否选中
                zphoto_preview_box.isChecked = selectList.contains(item)
                setBarTitle("${position + 1}/${lastPageSelectList?.size ?: 0}")
            }
            // 判断是否停止播放视频，获取的是上一个
            val roots = previewAdapter!!.rootLayouts[lastIndex]
            val lastItem = if (needAllList)  allList!![lastIndex] else lastPageSelectList!![lastIndex]
            if (lastItem.isVideo) {
                val player = roots.findViewById<ZPhotoVideoPlayer>(R.id.zphoto_preview_player)
                val playerPic = roots.findViewById<ImageView>(R.id.zphoto_preview_playPic)
                val pic = roots.findViewById<ImageView>(R.id.zphoto_preview_pic)
                if (player?.isPlaying() == true) {
                    player.pause()
                    playerPic.visibility = View.VISIBLE
                    pic.visibility = View.VISIBLE
                }
            }
            lastIndex = position
        }
    }

    override fun finish() {
        ZPhotoManager.getInstance().clearAllList()
        super.finish()
        overridePendingTransition(R.anim.anim_zphoto_bottom_in, R.anim.anim_zphoto_bottom_out)
    }

    /**
     * 获取已选中的图片和视频的数量
     */
    private fun getSelectPicOrVideoCount(): Pair<Int, Int> {
        var videoCount = 0
        var picCount = 0
        selectList.forEachNoIterable {
            if (it.isVideo) {
                videoCount++
            } else {
                picCount++
            }
        }
        return Pair(videoCount, picCount)
    }

    private fun getPicMaxSelectCount() = ZPhotoHelp.getInstance().getConfiguration().maxPicSelect
    private fun getVideoMaxSelectCount() = ZPhotoHelp.getInstance().getConfiguration().maxVideoSelect

}
