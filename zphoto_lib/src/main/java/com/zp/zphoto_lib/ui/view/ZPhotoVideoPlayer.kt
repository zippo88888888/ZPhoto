package com.zp.zphoto_lib.ui.view

import android.content.Context
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.os.Handler
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import com.zp.zphoto_lib.util.ZLog

class ZPhotoVideoPlayer : TextureView, TextureView.SurfaceTextureListener {

    private var player: MediaPlayer? = null

    var videoPath = ""

    private var videoWidth = 0
    private var videoHeight = 0

    private var leftVolume = 1f
    private var rightVolume = 1f

    private val IS_INIT = 0
    private val IS_PLAYING = 1
    private val IS_PAUSE = 2

    private var playState = IS_INIT

    companion object {
        /** 中心裁剪模式 */
        const val CENTER_CROP_MODE = 0x10001
        /** 中心填充模式 */
        const val CENTER_MODE = 0x10002
    }

    var size_type = CENTER_MODE

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)
    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int) : super(context, attributes, defStyleAttr) {
        surfaceTextureListener = this
        ZLog.i("ZPhotoVideoPlayer初始化....")
    }

    var videoPlayListener: ((MediaPlayer?) -> Unit)? = null

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        if (player == null) {
            player = MediaPlayer()

            /** 当装载流媒体完毕的时候回调 */
            player?.setOnPreparedListener {
                ZLog.i("媒体装载完成")
                setVolume()
                videoPlayListener?.invoke(player)
            }
            player?.setOnBufferingUpdateListener { _, percent ->
                ZLog.i("缓存中：$percent")
            }
            player?.setOnCompletionListener {
                ZLog.i("播放完成")
                play()
            }
            player?.setOnVideoSizeChangedListener { _, videoWidth, videoHeight ->
                this.videoWidth = videoWidth
                this.videoHeight = videoHeight
                updateTextureViewSize()
//                setVideoSize(videoWidth, videoHeight)
            }
            player?.setOnErrorListener { _, _, _ ->
                ZLog.e("播放失败")
                false
            }
        }
        val s = Surface(surface)
        // 将surface 与播放器进行绑定
        player?.setSurface(s)
        ZLog.i("播放器与Surface绑定成功")
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
//        updateTextureViewSize()
//        setVideoSize(videoWidth, videoHeight)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        player?.pause()
        player?.stop()
        player?.release()
        player = null
        videoPlayListener = null
        ZLog.i("播放器被销毁...")
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) = Unit

    fun setVolume(leftVolume: Float = 1f, rightVolume: Float = 1f) {
        this.leftVolume = leftVolume
        this.rightVolume = rightVolume
        player?.setVolume(leftVolume, rightVolume)
    }

    /**
     * 播放或暂停后播放
     */
    fun play() {
        if (player == null) return
        if (videoPath.isEmpty()) {
            ZLog.e("视频播放地址不能为空")
            return
        }
        if (isPlaying() == true) {
            ZLog.i("视频正在播放...")
            return
        }
        try {
            if (isPause()) {
                player?.start()
            } else {
                player?.reset()
                player?.setDataSource(videoPath)
                player?.prepare()
                player?.start()
            }
            playState = IS_PLAYING
        } catch (e: Exception) {
            e.printStackTrace()
            ZLog.e("播放失败！视频路径为：$videoPath")
        }
    }

    /**
     * 暂停
     */
    fun pause() {
        if (player == null) return
        if (isPlaying() == true) {
            player?.pause()
            playState = IS_PAUSE
        }
    }

    fun isPlaying() = player?.isPlaying
    fun isPause() = playState == IS_PAUSE

    fun setVideoSize(videoWidth: Int, videoHeight: Int) {
        this.videoWidth = videoWidth
        this.videoHeight = videoHeight
        requestLayout()
    }

    private fun updateTextureViewSize() {
        when (size_type) {
            CENTER_MODE -> updateTextureViewSizeCenter()
            CENTER_CROP_MODE -> updateTextureViewSizeCenterCrop()
            else -> throw IllegalArgumentException("参数错误")
        }
    }

    // 剪裁部分视频内容并全屏显示
    private fun updateTextureViewSizeCenterCrop() {
        val sx = width.toFloat() / videoWidth.toFloat()
        val sy = height.toFloat() / videoHeight.toFloat()
        val matrix = Matrix()
        val maxScale = Math.max(sx, sy)
        matrix.preTranslate(((width - videoWidth) / 2).toFloat(), ((height - videoHeight) / 2).toFloat())
        matrix.preScale(videoWidth / width.toFloat(), videoHeight / height.toFloat())
        matrix.postScale(maxScale, maxScale, (width / 2).toFloat(), (height / 2).toFloat())
        setTransform(matrix)
        postInvalidate()
    }

    // 居中显示
    private fun updateTextureViewSizeCenter(rotation: Float = 0f) {
        if (rotation == 90f || rotation == 270f) { // 宽高与之前相反

        }
        val sx = width.toFloat() / videoWidth.toFloat()
        val sy = height.toFloat() / videoHeight.toFloat()
        val matrix = Matrix()
        matrix.preTranslate(((width - videoWidth) / 2).toFloat(), ((height - videoHeight) / 2).toFloat())
        matrix.preScale(videoWidth / width.toFloat(), videoHeight / height.toFloat())
        if (sx >= sy) {
            matrix.postScale(sy, sy, (width / 2).toFloat(), (height / 2).toFloat())
        } else {
            matrix.postScale(sx, sx, (width / 2).toFloat(), (height / 2).toFloat())
        }
        setTransform(matrix)
        postInvalidate()
    }
}