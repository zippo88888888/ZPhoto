package com.zp.zphoto_lib.util

object ZPhotoUtil {

    /**
     * 视频时间格式化
     * @param duration 时间 单位：毫秒
     */
    fun videoDurationFormat(duration: Int) = StringBuffer().run {
        val tmpTime = duration / 1000
        var temp = tmpTime % 3600 / 60
        append(if (temp < 10) "0$temp:" else "$temp:")
        temp = tmpTime % 3600 % 60
        append(if (temp < 10) "0$temp" else temp.toString() + "")
        this.toString()
    }


}