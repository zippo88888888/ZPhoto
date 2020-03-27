package com.zp.zphoto.sample.sample

import android.net.Uri
import android.widget.ImageView
import com.zp.zphoto_lib.content.ZImageLoaderListener
import java.io.File

/**
 * 具体以自己业务逻辑为准
 */
class MyImageLoaderListener : ZImageLoaderListener {

    override fun loadImg(imageView: ImageView, file: File) {
        ImageLoad.loadImage(file, imageView)
    }

    override fun loadImg(imageView: ImageView, uri: Uri?, file: File) {
        if (uri == null) {
            loadImg(imageView, file)
        } else {
            ImageLoad.loadImage(uri, imageView)
        }
    }

    override fun loadImg(imageView: ImageView, path: String) {
        ImageLoad.loadImage(path, imageView)
    }

    override fun loadImg(imageView: ImageView, res: Int) {
        ImageLoad.loadImage(res, imageView)
    }


}