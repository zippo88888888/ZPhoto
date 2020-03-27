package com.zp.zphoto_lib.content

import android.content.Context
import android.os.Handler
import android.os.Message
import androidx.annotation.Nullable
import java.lang.ref.SoftReference
import java.util.*

open class ZImageCompress {

    private var softReference: SoftReference<Context>? = null
    private var listener: ((ArrayList<ZPhotoDetail>?) -> Unit)? = null

    fun start(
        context: Context,
        arrayList: ArrayList<ZPhotoDetail>?,
        listener: ((ArrayList<ZPhotoDetail>?) -> Unit)
    ) {
        this.listener = listener
        if (softReference == null) {
            softReference = SoftReference(context)
        }
        if (handler == null) {
            handler = CompressHandler(this)
        }
        onPreExecute()
        Thread {
            val list = doingCompressImage(arrayList)
            handler?.sendMessage(Message().apply {
                obj = list
                what = 0
            })
        }.start()
    }

    private var handler: CompressHandler? = null

    @Nullable
    protected fun getContext() = softReference?.get()

    /**
     * 压缩图片执行前调用 mainThread
     */
    protected open fun onPreExecute() = Unit

    /**
     * 压缩图片 子线程 ，如有需要，请重写该方法实现
     * 一般情况下 GIF和视频 不压缩
     */
    protected open fun doingCompressImage(arrayList: ArrayList<ZPhotoDetail>?) = arrayList

    /**
     * 压缩图片完成后调用 mainThread
     */
    protected open fun onPostExecute(list: ArrayList<ZPhotoDetail>?) {
        handler?.removeMessages(0)
        handler?.removeCallbacksAndMessages(null)
        handler = null
        softReference?.clear()
        listener?.invoke(list)
    }

    class CompressHandler(zImageCompress: ZImageCompress) : Handler() {

        private val softReference by lazy {
            SoftReference<ZImageCompress>(zImageCompress)
        }

        override fun handleMessage(msg: Message) {
            val list = msg.obj as ArrayList<ZPhotoDetail>
            softReference.get()?.onPostExecute(list)
        }
    }

}