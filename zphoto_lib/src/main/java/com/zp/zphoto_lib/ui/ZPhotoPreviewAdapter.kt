package com.zp.zphoto_lib.ui

import android.content.Context
import android.os.Build
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.zp.zphoto_lib.R
import com.zp.zphoto_lib.common.ZPhotoHelp
import com.zp.zphoto_lib.content.ZPhotoDetail
import com.zp.zphoto_lib.ui.view.ZPhotoVideoPlayer
import java.io.File

internal class ZPhotoPreviewAdapter(
    private var context: Context,
    private var list: ArrayList<ZPhotoDetail>?
) : PagerAdapter() {

    var rootLayouts = SparseArray<View>()

    init {
        list?.forEachIndexed { i, item ->
            if (item.isVideo) {
                rootLayouts.put(i, LayoutInflater.from(context).inflate(R.layout.layout_zphoto_preview, null))
            } else {
                rootLayouts.put(i, ImageView(context).apply {
//                    scaleType = ImageView.ScaleType.CENTER_CROP
                })
            }
        }
    }

    override fun getCount() = list?.size ?: 0

    override fun isViewFromObject(v: View, any: Any) = v == any

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        container.removeView(rootLayouts[position])
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val rootLayout = rootLayouts[position]
        container.addView(rootLayout)

        val item = list?.get(position) ?: ZPhotoDetail(
            "", "", 0.0,
            false, false, 0, "", 0L
        )
        val file = File(item.path)
        if (item.isVideo) {
            val player = rootLayout.findViewById<ZPhotoVideoPlayer>(R.id.zphoto_preview_player)
            val playerPic = rootLayout.findViewById<ImageView>(R.id.zphoto_preview_playPic)
            val pic = rootLayout.findViewById<ImageView>(R.id.zphoto_preview_pic)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ZPhotoHelp.getInstance().getImageLoaderListener().loadImg(pic, item.uri, file)
            } else {
                ZPhotoHelp.getInstance().getImageLoaderListener().loadImg(pic, file)
            }
            player.apply {
                videoPath = item.path
                videoUri = item.uri
                size_type = ZPhotoVideoPlayer.CENTER_CROP_MODE
            }
            playerPic.setOnClickListener {
                player.play()
                playerPic.visibility = View.GONE
                pic.visibility = View.GONE
            }
        } else {
            val pic = rootLayout as ImageView
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ZPhotoHelp.getInstance().getImageLoaderListener().loadImg(pic, item.uri, file)
            } else {
                ZPhotoHelp.getInstance().getImageLoaderListener().loadImg(pic, file)
            }
        }

        return rootLayout
    }



}