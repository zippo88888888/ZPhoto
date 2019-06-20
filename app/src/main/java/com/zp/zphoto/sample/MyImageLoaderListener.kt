package com.zp.zphoto.sample

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.zp.zphoto.R
import com.zp.zphoto_lib.content.ZImageLoaderListener
import com.zp.zphoto_lib.content.checkGif
import java.io.File

/**
 * 具体以自己业务逻辑为准
 */
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
            defaultPic = R.drawable.loading_pic
        }
        Glide.with(pic.context).load(url).asBitmap()
                .placeholder(defaultPic)
                .error(defaultPic)
                .dontAnimate() // 可以防止图片变形
                .into(pic)
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
        loadGifImg(file, pic)
    }

    /**
     * 加载Gif图
     */
    private fun loadGifImg(file: File, pic: ImageView) {
        val load = Glide.with(pic.context).load(file)
        if (checkGif(file.path)) {
            load.asGif()
                .placeholder(R.drawable.loading_pic)
                .error(R.drawable.loading_pic_error)
                .into(pic)
        } else { // 万一不是Gif图的处理
            load.asBitmap()
                .placeholder(R.drawable.loading_pic)
                .error(R.drawable.loading_pic_error)
                .dontAnimate()
                .into(pic)
        }
    }
}