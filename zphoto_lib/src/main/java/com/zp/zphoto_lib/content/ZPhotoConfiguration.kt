package com.zp.zphoto_lib.content

import com.zp.zphoto_lib.util.ZFile

/**
 * 配置信息
 */
class ZPhotoConfiguration {

    /**
     * 图片最多可选取数量
     */
    var maxPicSelect = ZPHOTO_DEFAULT_MAX_PIC_SELECT
    /**
     * 图片最大可选取大小
     */
    var maxPicSize = ZPHOTO_DEFAULT_MAX_PIC_SIZE
    /**
     * 是否显示Gif
     */
    var showGif = false
    /**
     * 图片是否需要裁剪
     */
    var needCrop = false

    /**
     * 剪裁 输出路径
     */
    var cropUri = ZFile.getPathForPath(ZFile.CROP)

    /**
     * 图片是否需要压缩
     */
    var needCompress = false

    /**
     * 图片压缩 输出路径
     */
    @Deprecated("由于压缩为自己实现，现废弃")
    var compressUri = ZFile.getPathForPath(ZFile.COMPRESS)

    /**
     * 设置是否显示视频
     */
    var showVideo = false
    /**
     * 视频最多可选取数量
     */
    var maxVideoSelect = ZPHOTO_DEFAULT_MAX_VIDEO_SELECT
    /**
     * 设置视频最大可选取的size
     */
    var maxVideoSize = ZPHOTO_DEFAULT_MAX_VIDEO_SIZE
    /**
     * 图片和视频是否可以同时选择
     */
    var allSelect = false
    /**
     * 相册页面是否显示拍照按钮
     */
    var showCamera = false

    /**
     * Android 7.0以上 需要的 FileProvider，一般都是包名 + xxxFileProvider
     */
    var authority = "com.zp.zphoto.FileProvider"

    var showLog = true


    /**
     * 供java调用
     */
    class Builder {

        private var maxPicSelect = ZPHOTO_DEFAULT_MAX_PIC_SELECT
        private var maxPicSize = ZPHOTO_DEFAULT_MAX_PIC_SIZE
        private var showGif = true
        private var needCrop = false
        private var cropUri = ZFile.getPathForPath(ZFile.CROP)

        private var needCompress = false
        private var compressUri = ZFile.getPathForPath(ZFile.COMPRESS)

        private var showVideo = true
        private var maxVideoSelect = ZPHOTO_DEFAULT_MAX_VIDEO_SELECT
        private var maxVideoSize = ZPHOTO_DEFAULT_MAX_VIDEO_SIZE
        private var allSelect = false
        private var showCamera = false
        private var authority = "com.zp.zphoto.FileProvider"
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

        fun clippingUri(cropUri: String): Builder {
            this.cropUri = cropUri
            return this
        }

        fun needCompress(needCompress: Boolean): Builder {
            this.needCompress = needCompress
            return this
        }

        fun compressUri(compressUri: String): Builder {
            this.compressUri = compressUri
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

        fun allSelect(allSelect: Boolean): Builder {
            this.allSelect = allSelect
            return this
        }

        fun showCamera(showCamera: Boolean): Builder {
            this.showCamera = showCamera
            return this
        }

        fun authority(authority: String): Builder {
            this.authority = authority
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
            this.cropUri = this@Builder.cropUri
            this.needCompress = this@Builder.needCompress
            this.compressUri = this@Builder.compressUri
            this.showVideo = this@Builder.showVideo
            this.maxVideoSelect = this@Builder.maxVideoSelect
            this.maxVideoSize = this@Builder.maxVideoSize
            this.allSelect = this@Builder.allSelect
            this.showCamera = this@Builder.showCamera
            this.authority = this@Builder.authority
            this.showLog = this@Builder.showLog
        }
    }

}