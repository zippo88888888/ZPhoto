package com.zp.zphoto_lib.content

/**
 * 配置信息
 */
class ZPhotoConfiguration {

    /**
     * 图片最多可选取数量
     */
    var maxPicSelect = ZPHOTO_DEFAULT_MAX_SELECT
    /**
     * 图片最大可选取大小
     */
    var maxPicSize = ZPHOTO_DEFAULT_MAX_SIZE
    /**
     * 是否显示Gif
     */
    var showGif = true
    /**
     * 是否需要裁剪
     */
    var needCrop = false
    /**
     * 是否需要压缩
     */
    var needCompress = false
    /**
     * 压缩比率 0 到 1
     */
    var compressRatio = ZPHOTO_DEFAULT_COMPACT_RATIO
    /**
     * 设置是否显示视频
     */
    var showVideo = true
    /**
     * 视频最多可选取数量
     */
    var maxVideoSelect = ZPHOTO_DEFAULT_MAX_VIDEO_SELECT
    /**
     * 设置视频最大可选取的size
     */
    var maxVideoSize = ZPHOTO_DEFAULT_MAX_VIDEO_SIZE

    var showLog = true


    /**
     * 供java调用
     */
    class Builder {

        private var maxPicSelect = ZPHOTO_DEFAULT_MAX_SELECT
        private var maxPicSize = ZPHOTO_DEFAULT_MAX_SIZE
        private var showGif = true
        private var needCrop = false
        private var needCompress = false
        private var compressRatio = ZPHOTO_DEFAULT_COMPACT_RATIO
        private var showVideo = true
        private var maxVideoSelect = ZPHOTO_DEFAULT_MAX_VIDEO_SELECT
        private var maxVideoSize = ZPHOTO_DEFAULT_MAX_VIDEO_SIZE
        private var showLog = true

        fun maxPicSelect(maxPicSelect: Int): Builder {
            this.maxPicSelect = maxPicSelect
            return this
        }

        fun maxPicSize(maxPicSize: Int): Builder {
            this.maxPicSize = maxPicSize
            return this
        }

        fun showGif(showGif: Boolean): Builder {
            this.showGif = showGif
            return this
        }

        fun needCrop(needCrop: Boolean): Builder {
            this.needCrop = needCrop
            return this
        }

        fun needCompress(needCompress: Boolean): Builder {
            this.needCompress = needCompress
            return this
        }

        fun compressRatio(compressRatio: Float): Builder {
            this.compressRatio = compressRatio
            return this
        }

        fun showVideo(showVideo: Boolean): Builder {
            this.showVideo = showVideo
            return this
        }

        fun maxVideoSelect(maxVideoSelect: Int): Builder {
            this.maxVideoSelect = maxVideoSelect
            return this
        }

        fun maxVideoSize(maxVideoSize: Int): Builder {
            this.maxVideoSize = maxVideoSize
            return this
        }

        fun showLog(showLog: Boolean): Builder {
            this.showLog = showLog
            return this
        }

        fun builder() = ZPhotoConfiguration().apply {
            this.maxPicSelect = this@Builder.maxPicSelect
            this.maxPicSize = this@Builder.maxPicSize
            this.showGif = this@Builder.showGif
            this.needCrop = this@Builder.needCrop
            this.needCompress = this@Builder.needCompress
            this.compressRatio = this@Builder.compressRatio
            this.showVideo = this@Builder.showVideo
            this.maxVideoSelect = this@Builder.maxVideoSelect
            this.maxVideoSize = this@Builder.maxVideoSize
            this.showLog = this@Builder.showLog
        }
    }

}