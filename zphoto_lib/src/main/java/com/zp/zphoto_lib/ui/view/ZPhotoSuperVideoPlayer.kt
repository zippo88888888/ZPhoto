package com.zp.zphoto_lib.ui.view

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.view.SurfaceHolder
import android.view.SurfaceView

/**
 * 加强版的 ZPhotoVideoPlayer ，引用自之前的项目，为后续视频剪辑功能做准备
 */
class ZPhotoSuperVideoPlayer : MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnVideoSizeChangedListener {

    /** 播放状态 */
    private var playerState = UNINITIALIZED
    private var mediaPlayer: MediaPlayer? = null
    /** 播放器是否已经准备好了  */
    private var isPrepared: Boolean = false
    /** 是否循环播放 */
    var isLooping: Boolean = false
    /** 播放选项 */
    private var moviePlayerOption: MoviePlayerOption? = null
    /** 是否正在播放 */
    private var isPlaying: Boolean = false
    /** 播放器监听 */
    var moviePlayerDelegateListener: MoviePlayerDelegateListener? = null

    private var handler: Handler? = null
    private var runnable: Runnable? = null
    /** 延迟加载时间，为了获取当前视频播放的进度 */
    private var delayMillis: Int = 0

    /** MediaPlay seek 监听 */
    var onSeekToPreviewListener: OnSeekToPreviewListener? = null

    /** 跳转的位置 */
    private var duration: Int = 0
    private var durationHandler: Handler? = null

    private var surfaceHolder: SurfaceHolder? = null
    private var callback: SurfaceHolder.Callback? = null
    /** surface是否销毁 */
    private var isSurfaceDestroyed: Boolean = false

    companion object {
        const val UNINITIALIZED = -0x10 /** 未初始化  */
        const val INITIALIZING = 0x10  /** 正在初始化 */
        const val INITIALIZED = 0x11   /** 已初始化  */
        const val PLAYING = 0x12       /** 播放中  */
        const val PAUSED = 0x13        /** 暂停  */
        const val STOPED = 0x14        /** 停止 */
        fun createMoviePlayer() = ZPhotoSuperVideoPlayer()
    }

    init { initPlayer() }

    /** 初始化 */
    private fun initPlayer() {
        isPrepared = false
        isLooping = true
        handler = Handler()
        delayMillis = 20
        durationHandler = Handler()
        callback = object : SurfaceHolder.Callback {
            override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
                this@ZPhotoSuperVideoPlayer.isSurfaceDestroyed = false
                this@ZPhotoSuperVideoPlayer.surfaceHolder = surfaceHolder
                this@ZPhotoSuperVideoPlayer.setSeekToPreviewListener(surfaceHolder)
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                this@ZPhotoSuperVideoPlayer.isSurfaceDestroyed = true
            }
        }
        runnable = Runnable {
            handler?.removeCallbacks(runnable)
            if (mediaPlayer != null && mediaPlayer!!.isPlaying && moviePlayerDelegateListener != null) {
                val progress = 100f * mediaPlayer!!.currentPosition.toFloat() / mediaPlayer!!.duration.toFloat()
                moviePlayerDelegateListener?.onProgress(progress)
            }
            handler?.postDelayed(runnable, delayMillis.toLong())
        }
    }

    fun initVideoPlayer(con: Context?, uri: Uri?, surfaceView: SurfaceView?) {
        if (con != null && uri != null && surfaceView != null) {
            this.setOption(MoviePlayerOption().apply {
                context = con
                movieUri = uri
                this.surfaceView = surfaceView
            })
        } else {
            throw NullPointerException("MoviePlay is not null")
        }
    }

    @Deprecated("Not recommended")
    fun initAudioPlayer(context: Context?, uri: Uri?) {
        if (context != null && uri != null) {
            setOption(MoviePlayerOption().apply {
                this.context = context
                movieUri = uri
            })
        } else {
            throw NullPointerException("MoviePlay is not null")
        }
    }

    private fun setSeekToPreviewListener() {
        this.setSeekToPreviewListener(UNINITIALIZED)
        isPlaying = false
        if (getSurfaceView() != null) {
            getSurfaceView()?.holder?.addCallback(callback)
        } else {
            setSeekToPreviewListener(surfaceHolder)
        }

    }

    /** 设置各种监听 */
    private fun setSeekToPreviewListener(surfaceHolder: SurfaceHolder?) {
        setSeekToPreviewListener(INITIALIZING)
        mediaPlayer = MediaPlayer().run {
            setScreenOnWhilePlaying(true)
            setOnPreparedListener(this@ZPhotoSuperVideoPlayer)
            setOnBufferingUpdateListener(this@ZPhotoSuperVideoPlayer)
            setOnCompletionListener(this@ZPhotoSuperVideoPlayer)
            setOnVideoSizeChangedListener(this@ZPhotoSuperVideoPlayer)
            setOnSeekCompleteListener(this@ZPhotoSuperVideoPlayer)
            setOnErrorListener(this@ZPhotoSuperVideoPlayer)
            this
        }
        isPrepared = false
        setMediaDisplay(surfaceHolder)
    }

    private fun setMediaDisplay(surfaceHolder: SurfaceHolder?) {
        if (mediaPlayer != null) {
            mediaPlayer?.reset()
            mediaPlayer?.setAudioStreamType(3)
            try {
                if (surfaceHolder != null) {
                    mediaPlayer?.setDisplay(surfaceHolder)
                }
                mediaPlayer?.setDataSource(moviePlayerOption?.context, getVideoUri())
                mediaPlayer?.prepareAsync()
                mediaPlayer?.isLooping = isLooping
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** 设置声音 */
    fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }
    /** 得到当前播放的进度 */
    fun getCurrentPosition() = mediaPlayer?.currentPosition ?: 0
    /** 得到视频的时间 */
    fun getDuration() = mediaPlayer?.duration ?: 0

    private fun setOption(moviePlayerOption: MoviePlayerOption?) {
        if (moviePlayerOption?.movieUri != null) {
            this.moviePlayerOption = moviePlayerOption
            setSeekToPreviewListener()
        }
    }

    private fun getSurfaceView() = moviePlayerOption?.surfaceView
    private fun getVideoUri() = moviePlayerOption?.movieUri

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        setSeekToPreviewListener(INITIALIZED)
        isPrepared = true
        if (isPlaying) {
            isPlaying = false
            start()
        }
    }

    override fun onSeekComplete(mediaPlayer: MediaPlayer) {
        onSeekToPreviewListener?.onSeekToComplete()
        moviePlayerDelegateListener?.onSeekComplete()
    }

    override fun onVideoSizeChanged(mediaPlayer: MediaPlayer, width: Int, height: Int) {
        moviePlayerDelegateListener?.onVideSizeChanged(mediaPlayer, width, height)
    }

    override fun onCompletion(mediaPlayer: MediaPlayer) {
        moviePlayerDelegateListener?.onCompletion()
    }

    override fun onBufferingUpdate(mediaPlayer: MediaPlayer, percent: Int) = Unit
    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean = false

    private fun setSeekToPreviewListener(playerState: Int) {
        this.playerState = playerState
        moviePlayerDelegateListener?.onStateChanged(playerState)
    }

    fun isUninitialized() = playerState == UNINITIALIZED
    fun isInitializing() = playerState == INITIALIZING
    fun isInitialized() = playerState == INITIALIZED
    fun isPlaying() = playerState == PLAYING
    fun isPaused() = playerState == PAUSED
    fun isStoped() = playerState == STOPED

    fun start() {
        if (!isPlaying()) {
            if (getSurfaceView() != null && isSurfaceDestroyed) {
                isPlaying = true
                getSurfaceView()?.holder?.removeCallback(callback)
                getSurfaceView()?.holder?.addCallback(callback)
            } else if (!isInitializing() && isPrepared) {
                if (!isStoped() && !isUninitialized()) {
                    if (mediaPlayer != null) {
                        if (mediaPlayer!!.isPlaying) {
                            mediaPlayer?.stop()
                        }
                        playerState = PLAYING
                        mediaPlayer?.start()
                        handler?.postDelayed(runnable, delayMillis.toLong())
                    }
                } else {
                    isPlaying = true
                    setSeekToPreviewListener(surfaceHolder)
                }
            } else {
                isPlaying = true
            }
        }
    }

    fun pause() {
        if (isPlaying()) {
            isPlaying = false
            mediaPlayer?.pause()
            setSeekToPreviewListener(PAUSED)
        }
    }

    fun resume() {
        if (isPaused()) {
            start()
        }
    }

    fun restart() {
        setMediaDisplay(surfaceHolder)
        start()
    }

    fun seekTo(time: Long) {
        mediaPlayer?.seekTo(time.toInt())
    }

    /** 视频跳转至指定位置 */
    fun seekToPreview(duration: Int, onSeekToPreviewListener: OnSeekToPreviewListener?) {
        this.duration = Math.min(duration, getDuration())
        durationHandler?.removeCallbacks(null)
        durationHandler?.postDelayed({
            if (mediaPlayer != null) {
                handler?.removeCallbacks(runnable)
                start()
                this@ZPhotoSuperVideoPlayer.onSeekToPreviewListener = object : OnSeekToPreviewListener {
                    override fun onSeekToComplete() {
                        pause()
                        onSeekToPreviewListener?.onSeekToComplete()
                        this@ZPhotoSuperVideoPlayer.onSeekToPreviewListener = null
                    }
                }
                seekTo(this@ZPhotoSuperVideoPlayer.duration.toLong())
            }
        }, 10L)
    }

    fun stop() {
        if (!isStoped() && !isUninitialized() && mediaPlayer != null) {
            handler?.removeCallbacks(runnable)
            isPlaying = false
            isPrepared = false
            mediaPlayer?.stop()
            release()
            setSeekToPreviewListener(STOPED)
        }
    }

    /**
     * 销毁播放器
     */
    fun destory() {
        stop()
        release()
        handler = null
        durationHandler = null
    }

    private fun release() {
        if (mediaPlayer != null) {
            mediaPlayer?.release()
            mediaPlayer = null
            setSeekToPreviewListener(UNINITIALIZED)
        }
    }


    class MoviePlayerOption {
        var context: Context? = null
        var movieUri: Uri? = null
        var surfaceView: SurfaceView? = null
    }

    interface MoviePlayerDelegateListener {
        /**
         * 当播放器状态改变时调用
         */
        fun onStateChanged(playerState: Int)

        /**
         * 当视频大小确定时调用
         */
        fun onVideSizeChanged(mediaPlayer: MediaPlayer, width: Int, height: Int)

        /**
         * 当前距离播放完成的值
         */
        fun onProgress(progress: Float)

        /**
         * 当前拖动结束时调用，目前没有什么用，请使用 OnSeekToPreviewListener
         */
        fun onSeekComplete()

        /**
         * 播放完成
         */
        fun onCompletion()
    }

    interface OnSeekToPreviewListener {
        fun onSeekToComplete()
    }

}