package com.zp.zphoto_lib.content

import java.io.Serializable


/**
 * 一级图片实体类
 * @param dirPath           图片文件夹的路径
 * @param firstImagePath    第一张图片的路径
 * @param folderName        文件夹的名称
 * @param count             包含图片的数量
 * @param time              最后修改时间
 */
data class ImageFolder(
        var dirPath: String,
        var firstImagePath: String,
        var folderName: String,
        var count: Int,
        var time: Long
) : Serializable

/**
 * 二级图片实体类
 * @param path          图片路径
 * @param size          图片大小
 * @param isGif         是否是Gif
 * @param parentPath    父类路径
 * @param time          最后修改时间
 */
data class ImageDetail(
        var path: String,
        var size: Double,
        var isGif: Boolean,
        var parentPath: String,
        var time: Long
) : Serializable {
        constructor() : this("", 0.0, false, "", 0L)

}
