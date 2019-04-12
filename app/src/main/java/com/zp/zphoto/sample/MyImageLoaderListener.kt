package com.zp.zphoto.sample

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.zp.zphoto.R
import com.zp.zphoto_lib.content.ZImageLoaderListener
import com.zp.zphoto_lib.content.checkGif
import java.io.File

class MyImageLoaderListener : ZImageLoaderListener {

    override fun loadImg(imageView: ImageView, file: File) {
        loadImg(file, imageView)
    }

    override fun loadImg(imageView: ImageView, path: String) {
        loadImg(path, imageView, 0)
    }

    override fun loadImg(imageView: ImageView, res: Int) {
        loadImg(res, imageView)
    }

    /**
     * 加载 网络 路径图片
     */
    private fun loadImg(url: String, pic: ImageView, defaultPic: Int = 0) {
        var defaultPic = defaultPic
        if (defaultPic <= 0) {
            defaultPic = R.drawable.ic_default_all_pic
        }
        Glide.with(pic.context).load(url).asBitmap()
                .placeholder(defaultPic)
                .error(defaultPic)
                .dontAnimate() // 可以防止图片变形
                .into(pic)
    }

    /**
     * 加载Gif图
     */
    private fun loadGifImg(url: String, pic: ImageView) {
        val load = Glide.with(pic.context).load(url)
        if (checkGif(url)) {
            load.asGif().into(pic)
        } else { // 万一不是Gif图的处理
            load.asBitmap()
                    .placeholder(R.drawable.ic_default_all_pic)
                    .error(R.drawable.ic_default_all_pic)
                    .dontAnimate()
                    .into(pic)
        }
    }

    /**
     * 加载 资源文件 路径图片
     */
    private fun loadImg(resID: Int, pic: ImageView) {
        Glide.with(pic.context)
                .load(resID)
                .dontAnimate()
                .into(pic)
    }

    /**
     * 加载 file 图片
     */
    private fun loadImg(file: File, pic: ImageView) {
        Glide.with(pic.context)
                .load(file)
                .asBitmap()
                .dontAnimate()
                .into(pic)
    }
}