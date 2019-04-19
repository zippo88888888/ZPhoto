package com.zp.zphoto_lib.content

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


/**
 * 一级文件夹实体类
 * @param dirPath           图片文件夹的路径
 * @param firstImagePath    第一张图片的路径
 * @param folderName        文件夹的名称
 * @param childs            该文件夹包含的图片
 */
@Parcelize
data class ZPhotoFolder(
        var dirPath: String,
        var firstImagePath: String,
        var folderName: String,
        var childs: ArrayList<ZPhotoDetail>
) : Parcelable

/**
 * 二级图片、视频实体类
 * @param path                  路径
 * @param name                  名称
 * @param size                  大小
 * @param isGif                 是否是Gif
 * @param isVideo               是否是视频
 * @param duration              视频时长
 * @param parentPath            父类路径
 * @param date_modified         最后修改时间
 */
@Parcelize
data class ZPhotoDetail(
        var path: String,
        var name: String,
        var size: Double,
        var isGif: Boolean,
        var isVideo: Boolean,
        var duration: Int,
        var parentPath: String,
        var date_modified: Long
) : Parcelable
