package com.zp.zphoto_lib.ui

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.View
import android.widget.ImageView
import com.zp.zphoto_lib.R
import com.zp.zphoto_lib.common.BaseZPhotoActivity
import com.zp.zphoto_lib.content.ZPhotoDetail
import com.zp.zphoto_lib.ui.view.ZPhotoVideoPlayer
import kotlinx.android.synthetic.main.activity_zphoto_preview.*

class ZPhotoPreviewActivity : BaseZPhotoActivity(), ViewPager.OnPageChangeListener {

    private var previewAdapter: ZPhotoPreviewAdapter? = null
    private var selectList: ArrayList<ZPhotoDetail>? = null

    private var lastIndex = 0

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.zphoto_select_menu, menu)
        menu?.findItem(R.id.menu_zphoto_select_down)?.let {
            it.isVisible = true
            it.title = "确定"
        }

        return true
    }

    override fun getContentView() = R.layout.activity_zphoto_preview

    override fun init(savedInstanceState: Bundle?) {
        selectList = intent.getParcelableArrayListExtra("selectList")
        setBarTitle("1/${selectList?.size ?: 0}")
        previewAdapter = ZPhotoPreviewAdapter(this, selectList)
        zphoto_preview_vp.adapter = previewAdapter
        zphoto_preview_vp.addOnPageChangeListener(this)
    }

    override fun onPageScrollStateChanged(position: Int) = Unit
    override fun onPageScrolled(position: Int, f: Float, i: Int) = Unit

    override fun onPageSelected(position: Int) {
        val roots = previewAdapter!!.rootLayouts[lastIndex]
        if (selectList!![lastIndex].isVideo) {
            val player = roots.findViewById<ZPhotoVideoPlayer>(R.id.zphoto_preview_player)
            val playerPic = roots.findViewById<ImageView>(R.id.zphoto_preview_playPic)
            if (player?.isPlaying() == true) {
                player.pause()
                playerPic.visibility = View.VISIBLE
            }
        }
        setBarTitle("${position + 1}/${selectList?.size ?: 0}")
        lastIndex = position
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.anim_zphoto_bottom_in, R.anim.anim_zphoto_bottom_out)
    }

}
